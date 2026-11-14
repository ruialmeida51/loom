begin;
insert into auth.users(id,email) values
('00000000-0000-4000-8000-000000000001','alice@example.test'),
('00000000-0000-4000-8000-000000000002','bob@example.test'),
('00000000-0000-4000-8000-000000000003','casey@example.test');

set role authenticated;
set request.jwt.claim.sub='00000000-0000-4000-8000-000000000001';
select set_config('loom.test.home',public.create_household('Our home','EUR','Europe/Lisbon')::text,true);
select set_config('loom.test.other_home',public.create_household('Another home','GBP','Europe/London')::text,true);
select set_config('loom.test.invite',public.create_household_invite(current_setting('loom.test.home')::uuid),true);
insert into public.shopping_items(id,household_id,name,created_by) values
('00000000-0000-4000-8000-000000000100',current_setting('loom.test.home')::uuid,'Milk',auth.uid()),
('00000000-0000-4000-8000-000000000101',current_setting('loom.test.other_home')::uuid,'Secret groceries',auth.uid());

do $$begin
    assert (select count(*) from public.households)=2,'Owner sees own households';
    begin
        update public.shopping_items set household_id=current_setting('loom.test.other_home')::uuid where id='00000000-0000-4000-8000-000000000100';
        raise exception 'Scope mutation was accepted';
    exception when raise_exception then
        if sqlerrm not like '%cannot be moved%' then raise; end if;
    end;
    begin
        insert into public.household_members values(current_setting('loom.test.home')::uuid,'00000000-0000-4000-8000-000000000003','owner',true,now());
        raise exception 'Direct membership write was accepted';
    exception when insufficient_privilege then null;end;
end;$$;

set request.jwt.claim.sub='00000000-0000-4000-8000-000000000002';
select public.join_household(current_setting('loom.test.invite'));
select public.register_push_device('00000000-0000-4000-8000-000000000900','test-device-token-for-bob','fcm');
do $$begin
    assert (select count(*) from public.households)=1,'Member cannot see other household';
    assert (select count(*) from public.shopping_items)=1,'Shopping is household scoped';
    assert (select count(*) from public.profiles)=2,'Profiles are limited to self and household';
    assert (select count(*) from public.household_members_with_names(current_setting('loom.test.home')::uuid))=2;
    begin perform public.join_household(current_setting('loom.test.invite'));raise exception 'Invitation reused';
    exception when raise_exception then if sqlerrm not like '%invalid or has expired%' then raise;end if;end;
    begin update public.household_members set role='owner' where user_id=auth.uid();raise exception 'Role escalation accepted';
    exception when insufficient_privilege then null;end;
    begin
        insert into public.shopping_items(household_id,name,created_by) values(current_setting('loom.test.other_home')::uuid,'Forged',auth.uid());
        raise exception 'Cross-household insertion accepted';
    exception when insufficient_privilege then null;end;
    begin
        insert into public.shopping_items(household_id,name,created_by) values(current_setting('loom.test.home')::uuid,'Forged author','00000000-0000-4000-8000-000000000001');
        raise exception 'Forged author accepted';
    exception when insufficient_privilege then null;end;
    update public.shopping_items set checked=true where id='00000000-0000-4000-8000-000000000100';
    assert (select checked from public.shopping_items where id='00000000-0000-4000-8000-000000000100');
end;$$;

-- Nicknames are global to the member profile; membership never grants permission to rename someone else.
update public.profiles set display_name='Bobby' where id=auth.uid();
do $$declare changed integer;begin
    assert (select display_name from public.profiles where id=auth.uid())='Bobby','A member can rename themselves';
    assert (select display_name from public.household_members_with_names(current_setting('loom.test.home')::uuid) where user_id=auth.uid())='Bobby','Membership reads use the saved nickname';
    update public.profiles set display_name='Forged name' where id='00000000-0000-4000-8000-000000000001';
    get diagnostics changed=row_count;
    assert changed=0,'A member cannot rename a housemate';
    begin
        update public.profiles set display_name='' where id=auth.uid();
        raise exception 'An empty nickname was accepted';
    exception when check_violation then null;end;
    begin
        update public.profiles set display_name=repeat('x',81) where id=auth.uid();
        raise exception 'An oversized nickname was accepted';
    exception when check_violation then null;end;
end;$$;

set request.jwt.claim.sub='00000000-0000-4000-8000-000000000001';
select public.create_expense('00000000-0000-4000-8000-000000000200',current_setting('loom.test.home')::uuid,'Food',6000,auth.uid(),'Groceries','2026-11-01',false,
    '{"00000000-0000-4000-8000-000000000001":3000,"00000000-0000-4000-8000-000000000002":3000}');
select public.create_expense('00000000-0000-4000-8000-000000000200',current_setting('loom.test.home')::uuid,'Food',6000,auth.uid(),'Groceries','2026-11-01',false,
    '{"00000000-0000-4000-8000-000000000001":3000,"00000000-0000-4000-8000-000000000002":3000}');
do $$begin
    assert (select count(*) from public.expenses)=1,'Retry must not create a second expense';
    assert (select sum(delta_minor) from public.household_ledger_entries)=0,'Ledger must balance';
    assert (select sum(delta_minor) from public.household_ledger_entries where user_id=auth.uid())=3000,'Payer is owed other shares';
    begin
        perform public.create_expense('00000000-0000-4000-8000-000000000201',current_setting('loom.test.home')::uuid,'Bad total',100,auth.uid(),'Other','2026-11-01',false,'{"00000000-0000-4000-8000-000000000001":99}');
        raise exception 'Invalid total accepted';
    exception when raise_exception then if sqlerrm not like '%Shares must equal%' then raise;end if;end;
    begin
        perform public.create_expense('00000000-0000-4000-8000-000000000202',current_setting('loom.test.home')::uuid,'Outsider share',100,auth.uid(),'Other','2026-11-01',false,'{"00000000-0000-4000-8000-000000000003":100}');
        raise exception 'Outsider share accepted';
    exception when raise_exception then if sqlerrm not like '%current household members%' then raise;end if;end;
    begin
        insert into public.expenses(id,household_id,title,amount_minor,paid_by,category,occurred_on,created_by) values(gen_random_uuid(),current_setting('loom.test.home')::uuid,'Bypass',100,auth.uid(),'Other','2026-11-01',auth.uid());
        raise exception 'Direct financial write accepted';
    exception when insufficient_privilege then null;end;
    begin
        perform public.create_expense('00000000-0000-4000-8000-000000000200',current_setting('loom.test.home')::uuid,'Changed',6000,auth.uid(),'Groceries','2026-11-01',false,'{"00000000-0000-4000-8000-000000000001":6000}');
        raise exception 'Conflicting request ID accepted';
    exception when raise_exception then if sqlerrm not like '%ID has already been used%' then raise;end if;end;
end;$$;

select set_config('loom.test.snapshot',public.save_monthly_statement(current_setting('loom.test.home')::uuid,'2026-11-01')::text,true);
select public.save_monthly_statement(current_setting('loom.test.home')::uuid,'2026-11-01');
do $$begin assert (select count(*) from public.monthly_statements)=1,'Unchanged snapshot must be idempotent';end;$$;

set request.jwt.claim.sub='00000000-0000-4000-8000-000000000002';
select public.record_settlement('00000000-0000-4000-8000-000000000300',current_setting('loom.test.home')::uuid,auth.uid(),'00000000-0000-4000-8000-000000000001',1000,'2026-11-02');
select public.record_settlement('00000000-0000-4000-8000-000000000300',current_setting('loom.test.home')::uuid,auth.uid(),'00000000-0000-4000-8000-000000000001',1000,'2026-11-02');
do $$begin
    assert (select count(*) from public.settlements)=1,'Repayment retry must be idempotent';
    assert (select sum(delta_minor) from public.household_ledger_entries where user_id=auth.uid())=-2000,'Partial repayment must reduce debt';
    assert (select sum(delta_minor) from public.household_ledger_entries)=0;
end;$$;
select public.save_monthly_statement(current_setting('loom.test.home')::uuid,'2026-11-01');
do $$begin
    assert (select count(*) from public.monthly_statements)=2,'Late changes create a new revision';
    assert (select (payload->>'total_minor')::bigint from public.monthly_statements where id=current_setting('loom.test.snapshot')::uuid)=6000,'Old statement remains unchanged';
end;$$;

set request.jwt.claim.sub='00000000-0000-4000-8000-000000000003';
do $$begin
    assert (select count(*) from public.expenses_with_shares)=0,'Reporting view must not bypass RLS';
    assert (select count(*) from public.monthly_statements)=0;
    assert (select count(*) from public.household_ledger_entries)=0;
    assert (select count(*) from public.profiles)=1;
    begin perform public.create_household_invite(current_setting('loom.test.home')::uuid);raise exception 'Outsider invitation accepted';
    exception when raise_exception then if sqlerrm not like '%Only the household owner%' then raise;end if;end;
    begin perform public.record_settlement(gen_random_uuid(),current_setting('loom.test.home')::uuid,'00000000-0000-4000-8000-000000000001','00000000-0000-4000-8000-000000000002',100,'2026-11-03');raise exception 'Outsider repayment accepted';
    exception when raise_exception then if sqlerrm not like '%only record repayments%' then raise;end if;end;
    begin perform public.claim_notification_deliveries();raise exception 'Worker accepted user token';
    exception when insufficient_privilege then null;end;
    begin perform public.verify_loom_worker_secret(repeat('x',40));raise exception 'Worker verifier accepted user token';
    exception when insufficient_privilege then null;end;
end;$$;

set role anon;
do $$begin
    begin perform public.create_household('No','EUR','UTC');raise exception 'Anonymous household creation accepted';
    exception when insufficient_privilege then null;end;
    begin perform public.claim_notification_deliveries();raise exception 'Anonymous worker access accepted';
    exception when insufficient_privilege then null;end;
end;$$;

reset role;
do $$begin
    assert not exists(select 1 from pg_class c join pg_namespace n on n.oid=c.relnamespace where n.nspname='public' and c.relkind='r' and not c.relrowsecurity),'All exposed tables must use RLS';
    assert not exists(select 1 from pg_proc p join pg_namespace n on n.oid=p.pronamespace where n.nspname='public' and p.prosecdef),'Privileged implementations must stay outside public';
    assert loom_private.next_month_date('2026-01-31',31)='2026-02-28';
    assert loom_private.next_month_date('2026-02-28',31)='2026-03-31','Month-end anchor must not drift';
end;$$;
insert into loom_private.worker_auth(singleton,secret_hash) values(true,extensions.digest(repeat('x',40),'sha256'));
set role service_role;
do $$begin assert public.verify_loom_worker_secret(repeat('x',40));assert not public.verify_loom_worker_secret(repeat('y',40));end;$$;
select public.claim_notification_deliveries();
reset role;
update public.household_members set active=false where household_id=current_setting('loom.test.home')::uuid and user_id='00000000-0000-4000-8000-000000000002';
set role service_role;
do $$begin assert (select count(*) from public.claim_notification_deliveries())=0,'Revoked member must not receive pending deliveries';end;$$;
set role authenticated;
set request.jwt.claim.sub='00000000-0000-4000-8000-000000000002';
do $$begin assert (select count(*) from public.expenses)=0,'Revocation takes effect without refreshing JWT claims';end;$$;
reset role;
update public.household_members set active=true where household_id=current_setting('loom.test.home')::uuid and user_id='00000000-0000-4000-8000-000000000002';

set role authenticated;
set request.jwt.claim.sub='00000000-0000-4000-8000-000000000001';
insert into public.reminders(id,household_id,title,due_at,recurrence,created_by) values('00000000-0000-4000-8000-000000000400',current_setting('loom.test.home')::uuid,'Plants','2026-03-28T09:00:00Z','daily',auth.uid());
select public.complete_reminder('00000000-0000-4000-8000-000000000400',true,'2026-03-28T09:00:00Z');
select public.complete_reminder('00000000-0000-4000-8000-000000000400',true,'2026-03-28T09:00:00Z');
do $$begin assert (select due_at from public.reminders where id='00000000-0000-4000-8000-000000000400')='2026-03-29T08:00:00Z','Duplicate completion must advance only once and preserve local time';end;$$;
select public.create_expense('00000000-0000-4000-8000-000000000500',current_setting('loom.test.home')::uuid,'Monthly bill',100,auth.uid(),'Bills','2026-08-31',true,'{"00000000-0000-4000-8000-000000000001":50,"00000000-0000-4000-8000-000000000002":50}');
reset role;
select loom_private.queue_due_work();
select set_config('loom.test.expense_count',(select count(*)::text from public.expenses),true);
select loom_private.queue_due_work();
do $$begin
    assert (select count(*)::text from public.expenses)=current_setting('loom.test.expense_count'),'Repeated cron run must not duplicate bills';
    assert not exists(select recurring_rule_id,occurred_on from public.expenses where recurring_rule_id is not null group by recurring_rule_id,occurred_on having count(*)>1);
    assert not exists(select e.id from public.expenses e left join public.expense_shares s on s.expense_id=e.id group by e.id,e.amount_minor having sum(s.amount_minor)<>e.amount_minor),'Generated shares must equal total';
end;$$;
rollback;
