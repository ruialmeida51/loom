begin;

-- Keep referenced schedules as tombstones so old expenses and occurrence uniqueness remain intact.
alter table public.recurring_expenses
    add column deleted_at timestamptz,
    add column split_method text not null default 'amounts'
        check (split_method in ('equal', 'shares', 'amounts', 'percentages')),
    add column split_values jsonb not null default '{}'::jsonb;
update public.recurring_expenses set split_values = shares;
-- Recover the user's original method only when that record reproduces this schedule's allocation.
update public.recurring_expenses r set split_method = e.split_method, split_values = e.split_values
from public.expenses e
where e.id = (select first.id from public.expenses first where first.recurring_rule_id = r.id
    and first.amount_minor = r.amount_minor and (select jsonb_object_agg(user_id::text, amount_minor)
        from public.expense_shares where expense_id = first.id) = r.shares
    order by first.created_at, first.id limit 1);

alter policy recurring_read on public.recurring_expenses
    using (deleted_at is null and loom_private.is_member(household_id));

create function loom_private.save_recurring_expense(
    p_household_id uuid, p_id uuid, p_version bigint, p_title text, p_amount_minor bigint,
    p_paid_by uuid, p_next_on date, p_shares jsonb, p_split_method text, p_split_values jsonb
) returns public.recurring_expenses
language plpgsql security definer set search_path = '' as $$
declare schedule public.recurring_expenses;
begin
    perform 1 from public.households where id = p_household_id for update;
    if auth.uid() is null or not loom_private.is_member(p_household_id) then
        raise exception 'expenses.message_household_access_denied';
    end if;
    select * into schedule from public.recurring_expenses
        where id = p_id and household_id = p_household_id for update;
    if not found or schedule.deleted_at is not null then
        raise exception 'expenses.message_this_recurring_expense_no_longer_exists';
    end if;
    if auth.uid() not in (schedule.created_by, schedule.paid_by) and not loom_private.is_owner(p_household_id) then
        raise exception 'expenses.message_only_the_payer_creator_or_household_owner_can_manage';
    end if;
    if schedule.cancelled_at is not null then raise exception 'expenses.message_a_cancelled_schedule_cannot_be_resumed'; end if;
    if p_version is null or schedule.version <> p_version then
        -- A lost successful response can be retried without overwriting a subsequent edit.
        if p_version = schedule.version - 1 and schedule.title = trim(p_title)
            and schedule.amount_minor = p_amount_minor and schedule.paid_by = p_paid_by
            and schedule.next_on = p_next_on and schedule.shares = p_shares
            and schedule.split_method = p_split_method and schedule.split_values = p_split_values then return schedule; end if;
        raise exception 'expenses.message_this_schedule_changed_refresh_and_try_again';
    end if;
    if not exists(select 1 from public.expense_accounts where id = schedule.account_id and not archived) then
        raise exception 'expenses.account_archived';
    end if;
    if p_title is null or length(trim(p_title)) not between 1 and 160 then raise exception 'expenses.invalid_title'; end if;
    if p_next_on is null or (p_next_on <> schedule.next_on and p_next_on <=
        (select (now() at time zone time_zone)::date from public.households where id = p_household_id)) then
        raise exception 'expenses.recurring_invalid_date';
    end if;
    if p_amount_minor is null or p_amount_minor not between 1 and 100000000000
        or p_shares is null or jsonb_typeof(p_shares) <> 'object' then raise exception 'Invalid expense amount or shares.'; end if;
    if (select count(*) from jsonb_each(p_shares)) not between 1 and 100 then raise exception 'Choose who to split with.'; end if;
    if exists(select 1 from jsonb_each(p_shares) where jsonb_typeof(value) <> 'number' or value::text !~ '^[0-9]+$') then
        raise exception 'Shares must be integer minor units.';
    end if;
    if (select sum(value::text::numeric) from jsonb_each(p_shares)) <> p_amount_minor
        or exists(select 1 from jsonb_each(p_shares) where value::text::numeric > p_amount_minor) then
        raise exception 'Shares must equal the expense total.';
    end if;
    -- Former participants can be retained. Newly assigned people must be current account members.
    if p_paid_by is null or (p_paid_by <> schedule.paid_by and not exists(
        select 1 from public.expense_account_members am join public.household_members m
            on m.household_id = am.household_id and m.user_id = am.user_id
        where am.account_id = schedule.account_id and am.user_id = p_paid_by and m.active)) or
        exists(select 1 from jsonb_each(p_shares) s where not schedule.shares ? s.key and not exists(
            select 1 from public.expense_account_members am join public.household_members m
                on m.household_id = am.household_id and m.user_id = am.user_id
            where am.account_id = schedule.account_id and am.user_id = s.key::uuid and m.active)) then
        raise exception 'expenses.message_all_participants_must_be_current_household_members';
    end if;
    if p_split_method is null or p_split_method not in ('equal', 'shares', 'amounts', 'percentages')
        or p_split_values is null or jsonb_typeof(p_split_values) <> 'object' then raise exception 'expenses.invalid_split'; end if;
    perform loom_private.validate_expense_split(p_amount_minor, p_shares, p_split_method, p_split_values);
    update public.recurring_expenses set title = trim(p_title), amount_minor = p_amount_minor,
        paid_by = p_paid_by, next_on = p_next_on,
        anchor_day = case when p_next_on = schedule.next_on then schedule.anchor_day else extract(day from p_next_on)::integer end,
        shares = p_shares, split_method = p_split_method, split_values = p_split_values
        where id = p_id returning * into schedule;
    return schedule;
end;
$$;

create function loom_private.delete_recurring_expense(p_household_id uuid, p_id uuid, p_version bigint)
returns void language plpgsql security definer set search_path = '' as $$
declare schedule public.recurring_expenses;
begin
    perform 1 from public.households where id = p_household_id for update;
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'expenses.message_household_access_denied'; end if;
    select * into schedule from public.recurring_expenses where id = p_id and household_id = p_household_id for update;
    if not found then raise exception 'expenses.message_this_recurring_expense_no_longer_exists'; end if;
    if auth.uid() not in (schedule.created_by, schedule.paid_by) and not loom_private.is_owner(p_household_id) then
        raise exception 'expenses.message_only_the_payer_creator_or_household_owner_can_manage';
    end if;
    if schedule.deleted_at is not null and schedule.version = p_version + 1 then return; end if;
    if p_version is null or schedule.version <> p_version or schedule.deleted_at is not null then
        raise exception 'expenses.message_this_schedule_changed_refresh_and_try_again';
    end if;
    update public.recurring_expenses set deleted_at = now(), active = false where id = p_id;
end;
$$;

create function public.save_recurring_expense(p_household_id uuid, p_id uuid, p_version bigint,
    p_title text, p_amount_minor bigint, p_paid_by uuid, p_next_on date, p_shares jsonb,
    p_split_method text, p_split_values jsonb) returns public.recurring_expenses
language sql security invoker set search_path = '' as $$
    select loom_private.save_recurring_expense(p_household_id, p_id, p_version, p_title, p_amount_minor,
        p_paid_by, p_next_on, p_shares, p_split_method, p_split_values);
$$;
create function public.delete_recurring_expense(p_household_id uuid, p_id uuid, p_version bigint)
returns void language sql security invoker set search_path = '' as $$
    select loom_private.delete_recurring_expense(p_household_id, p_id, p_version);
$$;
revoke all on function loom_private.save_recurring_expense(uuid,uuid,bigint,text,bigint,uuid,date,jsonb,text,jsonb),
    loom_private.delete_recurring_expense(uuid,uuid,bigint),
    public.save_recurring_expense(uuid,uuid,bigint,text,bigint,uuid,date,jsonb,text,jsonb),
    public.delete_recurring_expense(uuid,uuid,bigint) from public, anon, service_role;
grant execute on function loom_private.save_recurring_expense(uuid,uuid,bigint,text,bigint,uuid,date,jsonb,text,jsonb),
    loom_private.delete_recurring_expense(uuid,uuid,bigint),
    public.save_recurring_expense(uuid,uuid,bigint,text,bigint,uuid,date,jsonb,text,jsonb),
    public.delete_recurring_expense(uuid,uuid,bigint) to authenticated;

-- Preserve allocations and attribution across household departures.
create or replace function loom_private.control_recurring_expense(p_household_id uuid,p_id uuid,p_action text,p_version bigint) returns public.recurring_expenses
language plpgsql security definer set search_path='' as $$
declare row public.recurring_expenses; v_today date; v_candidate date;
begin
    perform 1 from public.households where id = p_household_id for update;
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'expenses.message_household_access_denied'; end if;
    select * into row from public.recurring_expenses where id=p_id and household_id=p_household_id for update;
    if not found or row.deleted_at is not null then raise exception 'expenses.message_this_recurring_expense_no_longer_exists'; end if;
    if auth.uid() not in(row.created_by,row.paid_by) and not loom_private.is_owner(p_household_id) then raise exception 'expenses.message_only_the_payer_creator_or_household_owner_can_manage'; end if;
    if p_action is null or p_action not in('pause','resume','cancel') or p_version is null then raise exception 'expenses.message_choose_a_valid_schedule_action'; end if;
    if p_action='cancel' and row.cancelled_at is not null or p_action='pause' and not row.active and row.cancelled_at is null or p_action='resume' and row.active then return row; end if;
    if row.cancelled_at is not null then raise exception 'expenses.message_a_cancelled_schedule_cannot_be_resumed'; end if;
    if row.version<>p_version then raise exception 'expenses.message_this_schedule_changed_refresh_and_try_again'; end if;
    if p_action='resume' and exists(select 1 from public.expense_accounts where id=row.account_id and archived) then raise exception 'expenses.account_archived'; end if;
    if p_action='resume' then
        select (now() at time zone time_zone)::date into v_today from public.households where id=p_household_id;
        if row.next_on<=v_today then
            v_candidate:=date_trunc('month',v_today)::date + least(row.anchor_day,extract(day from date_trunc('month',v_today)+interval '1 month - 1 day')::integer)-1;
            row.next_on:=case when v_candidate>v_today then v_candidate else loom_private.next_month_date(v_today,row.anchor_day) end;
        end if;
    end if;
    update public.recurring_expenses set active=(p_action='resume'),cancelled_at=case when p_action='cancel' then now() else null end,next_on=row.next_on where id=p_id returning * into row;
    return row;
end;
$$;

create or replace function loom_private.queue_due_work() returns void
language plpgsql security definer set search_path='' as $$
declare rule public.recurring_expenses; household public.households; v_today date; v_expense uuid; v_month date; v_loops integer;
begin
    insert into public.notifications(household_id,user_id,event_key,title,body,data)
    select r.household_id,m.user_id,'reminder:'||r.id||':'||r.due_at,'A gentle reminder','Something needs a little attention at home.',
           jsonb_build_object('route','reminders','household_id',r.household_id,'reminder_id',r.id,'due_at',r.due_at)
    from public.reminders r join public.household_members m on m.household_id=r.household_id and m.active
    where not r.completed and r.due_at<=now() and r.due_at>now()-interval '24 hours'
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
$$;

create or replace function loom_private.save_account_expense(
    p_id uuid, p_household_id uuid, p_account_id uuid, p_title text, p_amount_minor bigint,
    p_paid_by uuid, p_category text, p_occurred_on date, p_recurring boolean, p_shares jsonb,
    p_version bigint, p_split_method text, p_split_values jsonb
) returns uuid language plpgsql security definer set search_path = '' as $$
declare existing public.expenses; rule_id uuid;
begin
    perform 1 from public.households where id = p_household_id for update;
    if auth.uid() is null or not loom_private.is_member(p_household_id) then
        raise exception 'expenses.account_access_denied';
    end if;
    perform 1 from public.expense_accounts where id = p_account_id and household_id = p_household_id for no key update;
    perform loom_private.validate_account_expense(p_household_id, p_account_id, p_title, p_amount_minor, p_paid_by, p_occurred_on, p_shares);
    if p_version is null or p_version < 0 or p_split_method is null or
       p_split_method not in ('equal', 'shares', 'amounts', 'percentages') or
       p_split_values is null or jsonb_typeof(p_split_values) <> 'object' then raise exception 'expenses.invalid_split'; end if;
    perform loom_private.validate_expense_split(p_amount_minor, p_shares, p_split_method, p_split_values);
    perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(p_id::text, 0));
    select * into existing from public.expenses where id = p_id for update;
    if found then
        if existing.household_id <> p_household_id or existing.account_id <> p_account_id or existing.voided_at is not null then
            raise exception 'expenses.record_changed';
        end if;
        if p_version = 0 and existing.created_by = auth.uid() and existing.title = trim(p_title) and
           existing.amount_minor = p_amount_minor and existing.paid_by = p_paid_by and existing.category = p_category and
           existing.occurred_on = p_occurred_on and (existing.recurring_rule_id is not null) = p_recurring and
           existing.split_method = p_split_method and existing.split_values = p_split_values and
           (select jsonb_object_agg(user_id::text, amount_minor) from public.expense_shares where expense_id = p_id) = p_shares then
            return p_id;
        end if;
        if auth.uid() not in (existing.created_by, existing.paid_by) and not loom_private.is_owner(p_household_id) then
            raise exception 'expenses.account_access_denied';
        end if;
        if existing.version <> p_version or (existing.recurring_rule_id is not null) <> p_recurring then
            raise exception 'expenses.record_changed';
        end if;
        update public.expenses set title = trim(p_title), amount_minor = p_amount_minor, paid_by = p_paid_by,
            category = p_category, occurred_on = p_occurred_on, split_method = p_split_method,
            split_values = p_split_values, version = version + 1 where id = p_id;
        delete from public.expense_shares where expense_id = p_id;
    else
        if p_version <> 0 then raise exception 'expenses.record_changed'; end if;
        if p_recurring then
            insert into public.recurring_expenses(household_id, account_id, title, amount_minor, paid_by, category,
                shares, anchor_day, next_on, created_by, split_method, split_values)
            values (p_household_id, p_account_id, trim(p_title), p_amount_minor, p_paid_by, p_category, p_shares,
                extract(day from p_occurred_on)::integer,
                loom_private.next_month_date(p_occurred_on, extract(day from p_occurred_on)::integer), auth.uid(), p_split_method, p_split_values)
            returning id into rule_id;
        end if;
        insert into public.expenses(id, household_id, account_id, title, amount_minor, paid_by, category,
            occurred_on, recurring_rule_id, created_by, split_method, split_values)
        values (p_id, p_household_id, p_account_id, trim(p_title), p_amount_minor, p_paid_by, p_category,
            p_occurred_on, rule_id, auth.uid(), p_split_method, p_split_values);
        insert into public.notifications(household_id, user_id, event_key, title, body, data)
        select p_household_id, m.user_id, 'expense:' || p_id, 'A new shared expense',
            'There’s a new expense in your household.',
            jsonb_build_object('route', 'money', 'household_id', p_household_id, 'account_id', p_account_id)
        from public.expense_account_members m join public.household_members hm
            on hm.household_id = m.household_id and hm.user_id = m.user_id
        where m.account_id = p_account_id and hm.active and m.user_id <> auth.uid()
        on conflict(user_id, event_key) do nothing;
    end if;
    insert into public.expense_shares(expense_id, household_id, user_id, amount_minor)
    select p_id, p_household_id, key::uuid, value::text::bigint from jsonb_each(p_shares);
    return p_id;
end;
$$;

commit;
