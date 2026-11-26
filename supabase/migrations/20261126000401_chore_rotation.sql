begin;

-- Chores can belong to someone, and repeating ones can take turns: completing an occurrence hands
-- the next one to the following person in the rotation, skipping anyone who has left.
alter table public.tasks add column assignee_id uuid;
alter table public.tasks add column rotation uuid[] not null default '{}';
alter table public.tasks add constraint tasks_rotation_requires_repeat
    check (cardinality(rotation) = 0 or recurrence <> 'none');
alter table public.tasks add constraint tasks_rotation_size check (cardinality(rotation) <= 20);
alter table public.tasks add constraint tasks_rotation_includes_assignee
    check (cardinality(rotation) = 0 or assignee_id = any(rotation));
create index tasks_assignee_idx on public.tasks(assignee_id);

-- People on a task must be current members when they are set; leaving later keeps the record.
create function loom_private.guard_task_people() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
    if new.assignee_id is not null and (tg_op = 'INSERT' or new.assignee_id is distinct from old.assignee_id)
        and not exists(select 1 from public.household_members
            where household_id = new.household_id and user_id = new.assignee_id and active) then
        raise exception 'tasks.invalid_assignee';
    end if;
    if (tg_op = 'INSERT' or new.rotation is distinct from old.rotation) and (
        exists(select 1 from unnest(new.rotation) p(user_id) where not exists(
            select 1 from public.household_members m
            where m.household_id = new.household_id and m.user_id = p.user_id and m.active))
        or cardinality(new.rotation) <> (select count(distinct x) from unnest(new.rotation) x)) then
        raise exception 'tasks.invalid_assignee';
    end if;
    return new;
end;
$$;
revoke all on function loom_private.guard_task_people() from public, anon, authenticated;
create trigger tasks_people before insert or update of assignee_id, rotation on public.tasks
    for each row execute function loom_private.guard_task_people();

-- Who finished what, and when: the basis for whose turn is next and for chore history later.
create table public.task_completions (
    id uuid primary key default gen_random_uuid(),
    task_id uuid not null references public.tasks(id) on delete cascade,
    household_id uuid not null references public.households(id) on delete cascade,
    user_id uuid not null,
    assignee_id uuid,
    due_at timestamptz,
    completed_at timestamptz not null default now()
);
create index task_completions_task_idx on public.task_completions(task_id, completed_at desc);
create index task_completions_household_idx on public.task_completions(household_id, completed_at desc);
alter table public.task_completions enable row level security;
revoke all on public.task_completions from public, anon, authenticated;
grant select on public.task_completions to authenticated;
grant all on public.task_completions to service_role;
create policy task_completions_read on public.task_completions for select to authenticated
    using (loom_private.is_member(household_id));

-- The person after the current one in a task's rotation who is still in the household.
create function loom_private.next_in_rotation(p_household_id uuid, p_rotation uuid[], p_current uuid)
returns uuid language sql stable security definer set search_path = '' as $$
    with ordered as (
        select r.user_id, r.position, coalesce(array_position(p_rotation, p_current), 0) as current_position
        from unnest(p_rotation) with ordinality r(user_id, position)
        join public.household_members m on m.household_id = p_household_id and m.user_id = r.user_id and m.active
    )
    select user_id from ordered
    order by (position <= current_position), position
    limit 1;
$$;
revoke all on function loom_private.next_in_rotation(uuid, uuid[], uuid) from public, anon, authenticated;

CREATE OR REPLACE FUNCTION loom_private.complete_task(p_task_id uuid, p_completed boolean, p_expected_due_at timestamp with time zone)
 RETURNS void
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO ''
AS $function$
declare v_task public.tasks; v_zone text;
begin
    select * into v_task from public.tasks where id=p_task_id for update;
    if auth.uid() is null or not found or not loom_private.is_member(v_task.household_id) then
        raise exception 'Task access denied.';
    end if;
    -- A stale completion cannot advance another occurrence, including a task that gained a date.
    if v_task.due_at is distinct from p_expected_due_at then return; end if;
    if p_completed and not v_task.completed then
        insert into public.task_completions(task_id, household_id, user_id, assignee_id, due_at)
        values (v_task.id, v_task.household_id, auth.uid(), v_task.assignee_id, v_task.due_at);
    end if;
    if p_completed and not v_task.completed and v_task.recurrence<>'none' then
        select time_zone into v_zone from public.households where id=v_task.household_id;
        update public.tasks
        set due_at=((due_at at time zone v_zone)+case when recurrence='weekly' then interval '7 days' else interval '1 day' end) at time zone v_zone,
            completed=false,
            assignee_id=case when cardinality(rotation) = 0 then assignee_id
                else coalesce(loom_private.next_in_rotation(household_id, rotation, assignee_id), assignee_id) end
        where id=p_task_id;
    else
        update public.tasks set completed=p_completed where id=p_task_id;
    end if;
end;
$function$;

CREATE OR REPLACE FUNCTION loom_private.queue_due_work()
 RETURNS void
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO ''
AS $function$
declare rule public.recurring_expenses; household public.households; v_today date; v_expense uuid; v_month date; v_loops integer;
begin
    -- An assigned task reminds only the person whose turn it is, unless they have left.
    insert into public.notifications(household_id,user_id,event_key,title,body,data)
    select r.household_id,m.user_id,'reminder:'||r.id||':'||r.due_at,
           case when r.assignee_id=m.user_id then 'It’s your turn' else 'A gentle reminder' end,
           'Something needs a little attention at home.',
           jsonb_build_object('route','reminders','household_id',r.household_id,'reminder_id',r.id,'due_at',r.due_at)
    from public.tasks r join public.household_members m on m.household_id=r.household_id and m.active
    where r.due_at is not null and r.notification_enabled
        and not r.completed and r.due_at<=now() and r.due_at>now()-interval '24 hours'
        and (r.assignee_id is null or r.assignee_id=m.user_id or not exists(
            select 1 from public.household_members a
            where a.household_id=r.household_id and a.user_id=r.assignee_id and a.active))
    on conflict(user_id,event_key) do nothing;

    for rule in select r.* from public.recurring_expenses r where r.active and r.deleted_at is null order by r.household_id, r.next_on loop
        perform 1 from public.households where id = rule.household_id for update skip locked;
        if not found then continue; end if;
        select * into rule from public.recurring_expenses where id = rule.id for update skip locked;
        if not found or not rule.active or rule.deleted_at is not null then continue; end if;
        select * into household from public.households where id=rule.household_id;
        v_today:=(now() at time zone household.time_zone)::date;
        if exists(select 1 from public.expense_accounts where id=rule.account_id and archived) then
            update public.recurring_expenses set active=false where id=rule.id;
            continue;
        end if;
        v_loops:=0;
        while rule.next_on<=v_today and v_loops<24 loop
            v_expense:=gen_random_uuid();
            insert into public.expenses(id,household_id,account_id,title,amount_minor,paid_by,category,occurred_on,recurring_rule_id,created_by,split_method,split_values)
            values(v_expense,rule.household_id,rule.account_id,rule.title,rule.amount_minor,rule.paid_by,rule.category,rule.next_on,rule.id,rule.created_by,rule.split_method,rule.split_values)
            on conflict(recurring_rule_id,occurred_on) do nothing;
            if found then
                insert into public.expense_shares(expense_id,household_id,user_id,amount_minor)
                select v_expense,rule.household_id,key::uuid,value::text::bigint from jsonb_each(rule.shares);
                insert into public.notifications(household_id,user_id,event_key,title,body,data)
                select rule.household_id,m.user_id,'expense:'||v_expense,'A recurring bill was added','Your household’s recurring bill is ready to review.',jsonb_build_object('route','money','household_id',rule.household_id,'account_id',rule.account_id)
                from public.expense_account_members am
                join public.household_members m on m.household_id=am.household_id and m.user_id=am.user_id and m.active
                where am.account_id=rule.account_id
                on conflict(user_id,event_key) do nothing;
            end if;
            rule.next_on:=loom_private.next_month_date(rule.next_on,rule.anchor_day);
            v_loops:=v_loops+1;
        end loop;
        update public.recurring_expenses set next_on=rule.next_on where id=rule.id;
    end loop;

    for household in select * from public.households loop
        v_month:=(date_trunc('month',now() at time zone household.time_zone)-interval '1 month')::date;
        if household.created_at::date>=(v_month+interval '1 month')::date then continue; end if;
        insert into loom_private.statement_runs(household_id,month) values(household.id,v_month) on conflict do nothing;
        if found then
            perform loom_private.snapshot_month(household.id,v_month);
            insert into public.notifications(household_id,user_id,event_key,title,body,data)
            select household.id,m.user_id,'statement:'||household.id||':'||v_month,'Your monthly statement is ready','A little perspective on the month you shared.',jsonb_build_object('route','insights','household_id',household.id,'account_id',household.id)
            from public.household_members m where m.household_id=household.id and m.active on conflict(user_id,event_key) do nothing;
        end if;
    end loop;
end;
$function$;

commit;
