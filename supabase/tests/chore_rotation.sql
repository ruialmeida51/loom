begin;
insert into auth.users(id, email) values
    ('00000000-0000-4000-8000-000000000e01', 'chores-owner@example.test'),
    ('00000000-0000-4000-8000-000000000e02', 'chores-alex@example.test'),
    ('00000000-0000-4000-8000-000000000e03', 'chores-sam@example.test'),
    ('00000000-0000-4000-8000-000000000e04', 'chores-outsider@example.test');
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000e01';
select set_config('loom.chores.home', public.create_household('Chore home', 'EUR', 'UTC')::text, true);
select set_config('loom.chores.invite', public.create_household_invite(current_setting('loom.chores.home')::uuid), true);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000e02';
select public.redeem_household_invitation(current_setting('loom.chores.invite'));
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000e01';
select set_config('loom.chores.invite2', public.create_household_invite(current_setting('loom.chores.home')::uuid), true);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000e03';
select public.redeem_household_invitation(current_setting('loom.chores.invite2'));
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000e01';

do $$
declare
    home uuid := current_setting('loom.chores.home')::uuid;
    you uuid := '00000000-0000-4000-8000-000000000e01';
    alex uuid := '00000000-0000-4000-8000-000000000e02';
    sam uuid := '00000000-0000-4000-8000-000000000e03';
    outsider uuid := '00000000-0000-4000-8000-000000000e04';
    bins uuid := '00000000-0000-4000-8000-000000000e10';
    task public.tasks;
begin
    insert into public.tasks(id, household_id, title, due_at, recurrence, created_by, notification_enabled, assignee_id, rotation)
    values (bins, home, 'Bins out', now() - interval '1 hour', 'weekly', you, true, alex, array[you, alex, sam]);

    -- Completing hands the next occurrence to the next person, wrapping round.
    select * into task from public.tasks where id = bins;
    perform public.complete_task(bins, true, task.due_at);
    select * into task from public.tasks where id = bins;
    assert task.assignee_id = sam and not task.completed, 'Alex''s turn passes to Sam';
    assert (select user_id from public.task_completions where task_id = bins) = you, 'Who completed it is recorded';
    assert (select assignee_id from public.task_completions where task_id = bins) = alex, 'Whose turn it was is recorded';
    perform public.complete_task(bins, true, task.due_at);
    select * into task from public.tasks where id = bins;
    assert task.assignee_id = you, 'Sam''s turn wraps round to the start';

    -- A stale completion does not hand the turn on twice.
    perform public.complete_task(bins, true, task.due_at - interval '7 days');
    assert (select assignee_id from public.tasks where id = bins) = you, 'A stale completion changes nothing';

    -- Assignees and rotations must be current members, listed once.
    begin
        update public.tasks set assignee_id = outsider where id = bins;
        raise exception 'An outsider was assigned';
    exception when raise_exception then if sqlerrm <> 'tasks.invalid_assignee' then raise; end if; end;
    begin
        update public.tasks set rotation = array[you, you] where id = bins;
        raise exception 'A rotation repeated someone';
    exception when raise_exception then if sqlerrm <> 'tasks.invalid_assignee' then raise; end if; end;
    begin
        update public.tasks set assignee_id = sam, rotation = array[you, alex] where id = bins;
        raise exception 'The assignee was outside the rotation';
    exception when check_violation then null; end;
    begin
        insert into public.tasks(id, household_id, title, recurrence, created_by, notification_enabled, assignee_id, rotation)
        values (gen_random_uuid(), home, 'Once', 'none', you, false, you, array[you, alex]);
        raise exception 'A one-off task took turns';
    exception when check_violation then null; end;

    -- A plain assignment without turns keeps its person.
    insert into public.tasks(id, household_id, title, due_at, recurrence, created_by, notification_enabled, assignee_id)
    values ('00000000-0000-4000-8000-000000000e11', home, 'Water plants', now() - interval '1 hour', 'daily', you, true, sam);
    perform public.complete_task('00000000-0000-4000-8000-000000000e11', true,
        (select due_at from public.tasks where id = '00000000-0000-4000-8000-000000000e11'));
    assert (select assignee_id from public.tasks where id = '00000000-0000-4000-8000-000000000e11') = sam,
        'Without a rotation the assignee stays';
end;
$$;

-- Someone who left is skipped in the rotation.
reset role;
update public.household_members set active = false
where household_id = current_setting('loom.chores.home')::uuid and user_id = '00000000-0000-4000-8000-000000000e02';
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000e01';
do $$
declare bins uuid := '00000000-0000-4000-8000-000000000e10'; task public.tasks;
begin
    select * into task from public.tasks where id = bins;
    perform public.complete_task(bins, true, task.due_at);
    assert (select assignee_id from public.tasks where id = bins) = '00000000-0000-4000-8000-000000000e03',
        'Alex left, so the turn skips from you to Sam';
end;
$$;

-- Reminders go to the person whose turn it is; unassigned tasks remind everyone.
reset role;
update public.tasks set due_at = now() - interval '1 minute'
where household_id = current_setting('loom.chores.home')::uuid;
insert into public.tasks(id, household_id, title, due_at, recurrence, created_by, notification_enabled)
values ('00000000-0000-4000-8000-000000000e12', current_setting('loom.chores.home')::uuid, 'Anyone',
    now() - interval '1 minute', 'none', '00000000-0000-4000-8000-000000000e01', true);
select loom_private.queue_due_work();
do $$
begin
    assert (select array_agg(user_id order by user_id) from public.notifications
        where data->>'reminder_id' = '00000000-0000-4000-8000-000000000e10')
        = array['00000000-0000-4000-8000-000000000e03'::uuid], 'Only Sam is reminded about the bins';
    assert (select title from public.notifications where data->>'reminder_id' = '00000000-0000-4000-8000-000000000e10')
        = 'It’s your turn', 'The assignee hears it is their turn';
    assert (select count(*) from public.notifications where data->>'reminder_id' = '00000000-0000-4000-8000-000000000e12') = 2,
        'An unassigned task reminds every current member';
end;
$$;
rollback;
