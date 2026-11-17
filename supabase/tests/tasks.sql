begin;
insert into auth.users(id,email) values
('00000000-0000-4000-8000-000000008001','task-owner@example.test'),
('00000000-0000-4000-8000-000000008002','task-outsider@example.test');
set role authenticated;
set request.jwt.claim.sub='00000000-0000-4000-8000-000000008001';
select set_config('loom.test.task_home',public.create_household('Tasks test','EUR','Europe/Lisbon')::text,true);
insert into public.tasks(id,household_id,title,due_at,recurrence,notification_enabled,created_by) values
('00000000-0000-4000-8000-000000008011',current_setting('loom.test.task_home')::uuid,'Fix shelf',null,'none',false,auth.uid()),
('00000000-0000-4000-8000-000000008012',current_setting('loom.test.task_home')::uuid,'Parcel',now(),'none',false,auth.uid());
insert into public.reminders(id,household_id,title,due_at,recurrence,created_by) values
('00000000-0000-4000-8000-000000008013',current_setting('loom.test.task_home')::uuid,'Water plants','2026-03-28T09:00:00Z','daily',auth.uid());
do $$begin
    assert (select count(*) from public.tasks)=3,'Tasks include undated and non-notifying records';
    assert (select count(*) from public.reminders)=1,'Legacy workers only see dated notification-enabled records';
    assert (select notification_enabled from public.tasks where id='00000000-0000-4000-8000-000000008013'),'Legacy reminder intent is preserved';
    begin
        insert into public.tasks(household_id,title,due_at,notification_enabled,created_by)
        values(current_setting('loom.test.task_home')::uuid,'Invalid alert',null,true,auth.uid());
        raise exception 'Undated notification accepted';
    exception when check_violation then null;end;
    begin
        insert into public.tasks(household_id,title,due_at,notification_enabled,recurrence,created_by)
        values(current_setting('loom.test.task_home')::uuid,'Invalid repeat',null,false,'daily',auth.uid());
        raise exception 'Undated recurrence accepted';
    exception when check_violation then null;end;
end;$$;
select public.complete_task('00000000-0000-4000-8000-000000008011',true);
do $$begin
    assert (select completed and due_at is null and not notification_enabled from public.tasks where id='00000000-0000-4000-8000-000000008011'),'Undated completion never invents a date or alert';
end;$$;
select public.complete_task('00000000-0000-4000-8000-000000008011',false,null);
update public.tasks set due_at=now() where id='00000000-0000-4000-8000-000000008011';
select public.complete_task('00000000-0000-4000-8000-000000008011',true,null);
do $$begin
    assert not (select completed from public.tasks where id='00000000-0000-4000-8000-000000008011'),'A stale null-date completion cannot change a newly scheduled task';
end;$$;
select public.complete_reminder('00000000-0000-4000-8000-000000008013',true,'2026-03-28T09:00:00Z');
select public.complete_reminder('00000000-0000-4000-8000-000000008013',true,'2026-03-28T09:00:00Z');
do $$begin
    assert (select due_at='2026-03-29T08:00:00Z'::timestamptz and not completed from public.tasks where id='00000000-0000-4000-8000-000000008013'),'Repeat keeps wall-clock time and duplicate completion is idempotent';
end;$$;
-- Scheduled workers must ignore tasks without notification consent.
insert into public.tasks(id,household_id,title,due_at,recurrence,notification_enabled,created_by) values
('00000000-0000-4000-8000-000000008014',current_setting('loom.test.task_home')::uuid,'Notify me',now()-interval '5 minutes','none',true,auth.uid());
reset role;
select loom_private.queue_due_work();
do $$begin
    assert (select count(*) from public.notifications where household_id=current_setting('loom.test.task_home')::uuid and data ? 'reminder_id')=1,'Only the explicitly notifying task enters the outbox';
    assert exists(select 1 from public.notifications where data->>'reminder_id'='00000000-0000-4000-8000-000000008014'),'The legacy worker keeps delivering enabled tasks';
end;$$;
insert into public.notification_preferences(user_id,enabled) values('00000000-0000-4000-8000-000000008001',true)
on conflict(user_id) do update set enabled=true;
insert into public.push_devices(id,user_id,token,platform,updated_at) values
('00000000-0000-4000-8000-000000008021','00000000-0000-4000-8000-000000008001','task-test-device-token','fcm',now()-interval '1 hour');
do $$begin
    assert (select count(*) from loom_private.claim_notification_deliveries(50))=1,'Enabled task delivery is claimable';
end;$$;
update public.tasks set notification_enabled=false where id='00000000-0000-4000-8000-000000008014';
do $$begin
    assert (select count(*) from loom_private.claim_notification_deliveries(50))=0,'Turning notifications off suppresses queued delivery';
    assert (select status='dead' from loom_private.notification_deliveries where device_id='00000000-0000-4000-8000-000000008021'),'Queued work is invalidated before delivery';
end;$$;
set role authenticated;
set request.jwt.claim.sub='00000000-0000-4000-8000-000000008002';
do $$begin
    assert (select count(*) from public.tasks)=0,'Task membership is enforced';
    assert (select count(*) from public.reminders)=0,'Legacy view cannot bypass RLS';
    begin
        perform public.complete_task('00000000-0000-4000-8000-000000008011',true,null);
        raise exception 'Outsider completed a task';
    exception when raise_exception then if sqlerrm<>'Task access denied.' then raise;end if;end;
end;$$;
reset role;
do $$begin
    assert (select relrowsecurity from pg_class where oid='public.tasks'::regclass),'Canonical tasks keep RLS';
    assert (select 'security_invoker=true'=any(reloptions) from pg_class where oid='public.reminders'::regclass),'Legacy view uses invoker security';
    assert not has_function_privilege('anon','public.complete_task(uuid,boolean,timestamptz)','execute'),'No anonymous completion';
    assert not has_function_privilege('anon','loom_private.complete_task(uuid,boolean,timestamptz)','execute'),'No anonymous private completion';
end;$$;
rollback;
