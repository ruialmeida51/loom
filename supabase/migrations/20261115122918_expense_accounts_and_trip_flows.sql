begin;

create table public.expense_accounts (
    id uuid primary key,
    household_id uuid not null references public.households(id),
    name text not null check (length(trim(name)) between 1 and 80),
    currency text not null check (currency in ('EUR', 'GBP', 'USD', 'CAD', 'AUD')),
    is_default boolean not null default false,
    archived boolean not null default false,
    version bigint not null default 1,
    created_by uuid not null references public.profiles(id),
    created_at timestamptz not null default now(),
    unique (id, household_id),
    check (not is_default or not archived)
);
create unique index expense_accounts_default_idx on public.expense_accounts(household_id) where is_default;
create index expense_accounts_household_idx on public.expense_accounts(household_id);
create index expense_accounts_creator_idx on public.expense_accounts(created_by);

create table public.expense_account_members (
    account_id uuid not null,
    household_id uuid not null,
    user_id uuid not null,
    primary key (account_id, user_id),
    foreign key (account_id, household_id) references public.expense_accounts(id, household_id),
    foreign key (household_id, user_id) references public.household_members(household_id, user_id)
);
create index expense_account_members_household_user_idx on public.expense_account_members(household_id, user_id);
create index expense_account_members_user_idx on public.expense_account_members(user_id);

insert into public.expense_accounts(id, household_id, name, currency, is_default, created_by)
select id, id, 'Everyday', currency, true, created_by from public.households;
insert into public.expense_account_members(account_id, household_id, user_id)
select household_id, household_id, user_id from public.household_members;

alter table public.expense_accounts enable row level security;
alter table public.expense_account_members enable row level security;
create policy expense_accounts_read on public.expense_accounts for select to authenticated
using (loom_private.is_member(household_id));
create policy expense_account_members_read on public.expense_account_members for select to authenticated
using (loom_private.is_member(household_id));
revoke all on public.expense_accounts, public.expense_account_members from public, anon, authenticated;
grant select on public.expense_accounts, public.expense_account_members to authenticated;
grant all on public.expense_accounts, public.expense_account_members to service_role;

create view public.expense_accounts_with_members with (security_invoker = true) as
select a.*, coalesce((select jsonb_agg(m.user_id order by m.user_id)
    from public.expense_account_members m where m.account_id = a.id), '[]'::jsonb) as member_ids
from public.expense_accounts a;
grant select on public.expense_accounts_with_members to authenticated, service_role;

create function loom_private.seed_expense_account() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
    insert into public.expense_accounts(id, household_id, name, currency, is_default, created_by)
    values (new.id, new.id, 'Everyday', new.currency, true, new.created_by);
    return new;
end;
$$;
create trigger seed_expense_account after insert on public.households
for each row execute function loom_private.seed_expense_account();

create function loom_private.join_everyday_account() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
    insert into public.expense_account_members(account_id, household_id, user_id)
    values (new.household_id, new.household_id, new.user_id) on conflict do nothing;
    return new;
end;
$$;
create trigger join_everyday_account after insert on public.household_members
for each row execute function loom_private.join_everyday_account();
revoke all on function loom_private.seed_expense_account(), loom_private.join_everyday_account()
from public, anon, authenticated, service_role;

alter table public.expenses add column account_id uuid;
alter table public.expenses add column version bigint not null default 1;
alter table public.expenses add column voided_at timestamptz;
alter table public.expenses add column split_method text not null default 'amounts'
check (split_method in ('equal', 'shares', 'amounts', 'percentages'));
alter table public.expenses add column split_values jsonb not null default '{}'::jsonb
check (jsonb_typeof(split_values) = 'object');
alter table public.settlements add column account_id uuid;
alter table public.recurring_expenses add column account_id uuid;
alter table public.monthly_statements add column account_id uuid;

update public.expenses set account_id = household_id;
update public.settlements set account_id = household_id;
update public.recurring_expenses set account_id = household_id;
update public.monthly_statements set account_id = household_id;

alter table public.expenses alter column account_id set not null;
alter table public.settlements alter column account_id set not null;
alter table public.recurring_expenses alter column account_id set not null;
alter table public.monthly_statements alter column account_id set not null;
alter table public.expenses add foreign key (account_id, household_id) references public.expense_accounts(id, household_id);
alter table public.settlements add foreign key (account_id, household_id) references public.expense_accounts(id, household_id);
alter table public.recurring_expenses add foreign key (account_id, household_id) references public.expense_accounts(id, household_id);
alter table public.monthly_statements add foreign key (account_id, household_id) references public.expense_accounts(id, household_id);
create index expenses_account_date_idx on public.expenses(account_id, occurred_on);
create index settlements_account_date_idx on public.settlements(account_id, occurred_on);
create index recurring_expenses_account_idx on public.recurring_expenses(account_id);
create index monthly_statements_account_idx on public.monthly_statements(account_id, month);

-- Old write functions keep targeting Everyday; scheduled occurrences inherit their rule's account.
create function loom_private.assign_expense_account() returns trigger
language plpgsql set search_path = '' as $$
begin
    if tg_op = 'UPDATE' and new.account_id is distinct from old.account_id then
        raise exception 'expenses.account_scope_changed';
    end if;
    if new.account_id is null then
        if tg_table_name = 'expenses' then
            if new.recurring_rule_id is not null then
                select account_id into new.account_id from public.recurring_expenses where id = new.recurring_rule_id;
            end if;
        end if;
        new.account_id := coalesce(new.account_id, new.household_id);
    end if;
    return new;
end;
$$;
create trigger expense_account_scope before insert or update on public.expenses
for each row execute function loom_private.assign_expense_account();
create trigger settlement_account_scope before insert or update on public.settlements
for each row execute function loom_private.assign_expense_account();
create trigger recurring_account_scope before insert or update on public.recurring_expenses
for each row execute function loom_private.assign_expense_account();
create trigger statement_account_scope before insert or update on public.monthly_statements
for each row execute function loom_private.assign_expense_account();
revoke all on function loom_private.assign_expense_account() from public, anon, authenticated;

create view public.account_expenses_with_shares with (security_invoker = true) as
select e.id, e.household_id, e.account_id, e.title, e.amount_minor, e.paid_by, e.category,
       e.occurred_on, e.created_by, e.version, e.split_method, e.split_values,
       e.recurring_rule_id is not null as recurring,
       coalesce((select jsonb_object_agg(s.user_id::text, s.amount_minor)
           from public.expense_shares s where s.expense_id = e.id), '{}'::jsonb) as shares
from public.expenses e where e.voided_at is null;
grant select on public.account_expenses_with_shares to authenticated, service_role;

create view public.account_ledger_entries with (security_invoker = true) as
select account_id, household_id, occurred_on, paid_by as user_id, amount_minor as delta_minor
from public.expenses where voided_at is null
union all
select e.account_id, s.household_id, e.occurred_on, s.user_id, -s.amount_minor
from public.expense_shares s join public.expenses e on e.id = s.expense_id where e.voided_at is null
union all select account_id, household_id, occurred_on, from_user, amount_minor from public.settlements
union all select account_id, household_id, occurred_on, to_user, -amount_minor from public.settlements;
grant select on public.account_ledger_entries to authenticated, service_role;

create or replace view public.expenses_with_shares with (security_invoker = true) as
select e.id, e.household_id, e.title, e.amount_minor, e.paid_by, e.category, e.occurred_on,
       e.recurring_rule_id is not null as recurring,
       coalesce((select jsonb_object_agg(s.user_id::text, s.amount_minor)
           from public.expense_shares s where s.expense_id = e.id), '{}'::jsonb) as shares
from public.expenses e where e.voided_at is null and e.account_id = e.household_id;
create or replace view public.household_ledger_entries with (security_invoker = true) as
select household_id, occurred_on, user_id, delta_minor from public.account_ledger_entries
where account_id = household_id;

create function loom_private.save_expense_account(
    p_household_id uuid, p_id uuid, p_name text, p_currency text,
    p_member_ids uuid[], p_archived boolean, p_version bigint
) returns uuid language plpgsql security definer set search_path = '' as $$
declare account public.expense_accounts;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then
        raise exception 'expenses.account_access_denied';
    end if;
    if p_name is null or length(trim(p_name)) not between 1 and 80 or
       p_currency is null or p_currency not in ('EUR', 'GBP', 'USD', 'CAD', 'AUD') then
        raise exception 'expenses.invalid_account';
    end if;
    perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(p_id::text, 0));
    select * into account from public.expense_accounts where id = p_id for no key update;
    if found then
        if account.household_id <> p_household_id or
           (account.created_by <> auth.uid() and not loom_private.is_owner(p_household_id)) then
            raise exception 'expenses.account_access_denied';
        end if;
        if p_version = 0 and account.created_by = auth.uid() and account.name = trim(p_name) and
           account.currency = p_currency and not account.archived and not p_archived and
           (select array_agg(user_id order by user_id) from public.expense_account_members where account_id = p_id) =
           (select array_agg(member order by member) from unnest(p_member_ids) as member) then return p_id; end if;
        if account.is_default or account.currency <> p_currency then raise exception 'expenses.invalid_account'; end if;
        if account.version <> p_version then raise exception 'expenses.record_changed'; end if;
        if (select array_agg(user_id order by user_id) from public.expense_account_members where account_id = p_id)
            is distinct from (select array_agg(member order by member) from unnest(p_member_ids) as member) then
            raise exception 'expenses.invalid_account_members';
        end if;
        update public.expense_accounts set name = trim(p_name), archived = p_archived, version = version + 1 where id = p_id;
        if p_archived then update public.recurring_expenses set active = false where account_id = p_id and active; end if;
        return p_id;
    end if;
    if p_version <> 0 or p_archived or p_member_ids is null or cardinality(p_member_ids) not between 1 and 100 or
       not (auth.uid() = any(p_member_ids)) or
       (select count(distinct member) from unnest(p_member_ids) as member) <> cardinality(p_member_ids) or
       exists (select 1 from unnest(p_member_ids) as member where not exists (
           select 1 from public.household_members m where m.household_id = p_household_id and m.user_id = member and m.active
       )) then raise exception 'expenses.invalid_account_members'; end if;
    insert into public.expense_accounts(id, household_id, name, currency, created_by)
    values (p_id, p_household_id, trim(p_name), p_currency, auth.uid());
    insert into public.expense_account_members(account_id, household_id, user_id)
    select p_id, p_household_id, member from unnest(p_member_ids) as member;
    return p_id;
end;
$$;

create function loom_private.validate_account_expense(
    p_household_id uuid, p_account_id uuid, p_title text, p_amount_minor bigint,
    p_paid_by uuid, p_occurred_on date, p_shares jsonb
) returns void language plpgsql security definer set search_path = '' as $$
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) or not exists (
        select 1 from public.expense_account_members where account_id = p_account_id and user_id = auth.uid()
    ) then raise exception 'expenses.account_access_denied'; end if;
    if not exists (select 1 from public.expense_accounts where id = p_account_id and household_id = p_household_id and not archived) then
        raise exception 'expenses.account_archived';
    end if;
    if p_title is null or length(trim(p_title)) not between 1 and 160 then raise exception 'expenses.invalid_title'; end if;
    if p_amount_minor is null or p_amount_minor not between 1 and 100000000000 or
       p_shares is null or jsonb_typeof(p_shares) <> 'object' then raise exception 'Invalid expense amount or shares.'; end if;
    if p_occurred_on is null or p_occurred_on > (select (now() at time zone time_zone)::date from public.households where id = p_household_id) then
        raise exception 'expenses.message_payment_date_future';
    end if;
    if (select count(*) from jsonb_each(p_shares)) not between 1 and 100 then raise exception 'Choose who to split with.'; end if;
    if exists (select 1 from jsonb_each(p_shares) s where jsonb_typeof(s.value) <> 'number' or s.value::text !~ '^[0-9]+$') then
        raise exception 'Shares must be integer minor units.';
    end if;
    if (select sum(value::text::numeric) from jsonb_each(p_shares)) <> p_amount_minor or
       exists (select 1 from jsonb_each(p_shares) where value::text::numeric > p_amount_minor) then
        raise exception 'Shares must equal the expense total.';
    end if;
    if not exists (select 1 from public.expense_account_members am join public.household_members m
        on m.household_id = am.household_id and m.user_id = am.user_id
        where am.account_id = p_account_id and am.user_id = p_paid_by and m.active) or
       exists (select 1 from jsonb_each(p_shares) s where not exists (
           select 1 from public.expense_account_members am join public.household_members m
           on m.household_id = am.household_id and m.user_id = am.user_id
           where am.account_id = p_account_id and am.user_id = s.key::uuid and m.active
       )) then raise exception 'expenses.invalid_account_members'; end if;
end;
$$;

-- Saved split metadata must reproduce the authoritative integer shares shown to other members.
create function loom_private.validate_expense_split(p_amount_minor bigint, p_shares jsonb, p_method text, p_values jsonb)
returns void language plpgsql immutable set search_path = '' as $$
declare expected jsonb; denominator numeric;
begin
    if p_method = 'amounts' then
        if p_values <> p_shares then raise exception 'expenses.invalid_split'; end if;
        return;
    end if;
    if p_method = 'equal' then
        with participants as (
            select key, row_number() over(order by key) as rank, count(*) over() as total from jsonb_each(p_shares)
        ) select jsonb_object_agg(key, p_amount_minor / total + case when rank <= p_amount_minor % total then 1 else 0 end)
            into expected from participants;
    else
        if (select count(*) from jsonb_each(p_values)) <> (select count(*) from jsonb_each(p_shares)) or
           exists(select 1 from jsonb_each(p_shares) where not p_values ? key) or
           exists(select 1 from jsonb_each(p_values) where jsonb_typeof(value) <> 'number' or value::text !~ '^[0-9]+$') then
            raise exception 'expenses.invalid_split';
        end if;
        select sum(value::text::numeric) into denominator from jsonb_each(p_values);
        if denominator <= 0 or (p_method = 'percentages' and denominator <> 100) or
           exists(select 1 from jsonb_each(p_values) where value::text::numeric > case when p_method = 'percentages' then 100 else 10000 end) then
            raise exception 'expenses.invalid_split';
        end if;
        with weights as (
            select key, floor(p_amount_minor * value::text::numeric / denominator)::bigint as base,
                mod(p_amount_minor * value::text::numeric, denominator) as remainder from jsonb_each(p_values)
        ), ranked as (
            select *, row_number() over(order by remainder desc, key) as rank, sum(base) over() as allocated from weights
        ) select jsonb_object_agg(key, base + case when rank <= p_amount_minor - allocated then 1 else 0 end)
            into expected from ranked;
    end if;
    if expected is distinct from p_shares then raise exception 'expenses.invalid_split'; end if;
end;
$$;
revoke all on function loom_private.validate_expense_split(bigint, jsonb, text, jsonb) from public, anon, authenticated, service_role;

create function loom_private.save_account_expense(
    p_id uuid, p_household_id uuid, p_account_id uuid, p_title text, p_amount_minor bigint,
    p_paid_by uuid, p_category text, p_occurred_on date, p_recurring boolean, p_shares jsonb,
    p_version bigint, p_split_method text, p_split_values jsonb
) returns uuid language plpgsql security definer set search_path = '' as $$
declare existing public.expenses; rule_id uuid;
begin
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
                shares, anchor_day, next_on, created_by)
            values (p_household_id, p_account_id, trim(p_title), p_amount_minor, p_paid_by, p_category, p_shares,
                extract(day from p_occurred_on)::integer,
                loom_private.next_month_date(p_occurred_on, extract(day from p_occurred_on)::integer), auth.uid())
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

create function loom_private.delete_account_expense(p_household_id uuid, p_account_id uuid, p_id uuid, p_version bigint)
returns void language plpgsql security definer set search_path = '' as $$
declare existing public.expenses;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'expenses.account_access_denied'; end if;
    perform 1 from public.expense_accounts where id = p_account_id and household_id = p_household_id and not archived for no key update;
    if not found then raise exception 'expenses.account_archived'; end if;
    select * into existing from public.expenses where id = p_id and household_id = p_household_id and account_id = p_account_id for update;
    if not found then raise exception 'expenses.record_changed'; end if;
    if auth.uid() not in (existing.created_by, existing.paid_by) and not loom_private.is_owner(p_household_id) then
        raise exception 'expenses.account_access_denied';
    end if;
    if existing.voided_at is not null then return; end if;
    if existing.version <> p_version then raise exception 'expenses.record_changed'; end if;
    update public.expenses set voided_at = now(), version = version + 1 where id = p_id;
end;
$$;

create function loom_private.record_account_settlement(
    p_id uuid, p_household_id uuid, p_account_id uuid, p_from uuid, p_to uuid, p_amount_minor bigint, p_occurred_on date
) returns uuid language plpgsql security definer set search_path = '' as $$
declare existing public.settlements;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) or auth.uid() not in (p_from, p_to) or not exists (
        select 1 from public.expense_accounts where id = p_account_id and household_id = p_household_id
    ) then raise exception 'You can only record repayments you send or receive.'; end if;
    if p_from is null or p_to is null or p_from = p_to or p_amount_minor is null or p_amount_minor not between 1 and 100000000000 or
       p_occurred_on is null or p_occurred_on > (select (now() at time zone time_zone)::date from public.households where id = p_household_id) then
        raise exception 'Choose two people and a positive amount.';
    end if;
    if (select count(*) from public.expense_account_members am join public.household_members m
        on m.household_id = am.household_id and m.user_id = am.user_id
        where am.account_id = p_account_id and am.user_id in (p_from, p_to) and m.active) <> 2 then
        raise exception 'expenses.invalid_account_members';
    end if;
    perform 1 from public.expense_accounts where id = p_account_id for no key update;
    perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(p_id::text, 0));
    select * into existing from public.settlements where id = p_id;
    if found then
        if existing.household_id = p_household_id and existing.account_id = p_account_id and existing.created_by = auth.uid() and
           existing.from_user = p_from and existing.to_user = p_to and existing.amount_minor = p_amount_minor and existing.occurred_on = p_occurred_on then return p_id; end if;
        raise exception 'This repayment ID has already been used.';
    end if;
    insert into public.settlements(id, household_id, account_id, from_user, to_user, amount_minor, occurred_on, created_by)
    values (p_id, p_household_id, p_account_id, p_from, p_to, p_amount_minor, p_occurred_on, auth.uid());
    return p_id;
end;
$$;

create function public.save_expense_account(p_household_id uuid, p_id uuid, p_name text, p_currency text, p_member_ids uuid[], p_archived boolean, p_version bigint)
returns uuid language sql security invoker set search_path = '' as $$
    select loom_private.save_expense_account(p_household_id, p_id, p_name, p_currency, p_member_ids, p_archived, p_version);
$$;
create function public.save_account_expense(p_id uuid, p_household_id uuid, p_account_id uuid, p_title text, p_amount_minor bigint,
    p_paid_by uuid, p_category text, p_occurred_on date, p_recurring boolean, p_shares jsonb, p_version bigint, p_split_method text, p_split_values jsonb)
returns uuid language sql security invoker set search_path = '' as $$
    select loom_private.save_account_expense(p_id, p_household_id, p_account_id, p_title, p_amount_minor,
        p_paid_by, p_category, p_occurred_on, p_recurring, p_shares, p_version, p_split_method, p_split_values);
$$;
create function public.delete_account_expense(p_household_id uuid, p_account_id uuid, p_id uuid, p_version bigint)
returns void language sql security invoker set search_path = '' as $$
    select loom_private.delete_account_expense(p_household_id, p_account_id, p_id, p_version);
$$;
create function public.record_account_settlement(p_id uuid, p_household_id uuid, p_account_id uuid, p_from uuid, p_to uuid, p_amount_minor bigint, p_occurred_on date)
returns uuid language sql security invoker set search_path = '' as $$
    select loom_private.record_account_settlement(p_id, p_household_id, p_account_id, p_from, p_to, p_amount_minor, p_occurred_on);
$$;

do $$
declare fn record;
begin
    for fn in select p.oid::regprocedure as signature from pg_proc p join pg_namespace n on n.oid = p.pronamespace
        where n.nspname in ('public', 'loom_private') and p.proname in
            ('save_expense_account', 'save_account_expense', 'delete_account_expense', 'record_account_settlement')
    loop
        execute format('revoke all on function %s from public, anon', fn.signature);
        execute format('grant execute on function %s to authenticated, service_role', fn.signature);
    end loop;
end;
$$;
revoke all on function loom_private.validate_account_expense(uuid, uuid, text, bigint, uuid, date, jsonb)
from public, anon, authenticated, service_role;


alter table public.monthly_statements drop constraint monthly_statements_household_id_month_revision_key;
alter table public.monthly_statements add unique (account_id, month, revision);

create function loom_private.snapshot_account_month(p_household_id uuid, p_account_id uuid, p_month date)
returns uuid language plpgsql security definer set search_path = '' as $$
declare payload jsonb; statement_hash text; statement_id uuid; next_revision integer;
begin
    if p_month is null or extract(day from p_month) <> 1 then raise exception 'Use the first day of the month.'; end if;
    if auth.uid() is not null and not loom_private.is_member(p_household_id) then raise exception 'Household access denied.'; end if;
    perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(p_account_id::text || p_month::text, 0));
    select jsonb_build_object(
        'month', p_month, 'account_id', a.id, 'currency', a.currency,
        'total_minor', coalesce((select sum(e.amount_minor) from public.expenses e where e.account_id = a.id and e.voided_at is null
            and e.occurred_on >= p_month and e.occurred_on < (p_month + interval '1 month')::date), 0),
        'categories', coalesce((select jsonb_object_agg(category, total) from (
            select e.category, sum(e.amount_minor) total from public.expenses e where e.account_id = a.id and e.voided_at is null
                and e.occurred_on >= p_month and e.occurred_on < (p_month + interval '1 month')::date group by e.category
        ) totals), '{}'::jsonb),
        'members', coalesce((select jsonb_agg(jsonb_build_object(
            'user_id', m.user_id, 'name', p.display_name,
            'paid_minor', coalesce((select sum(e.amount_minor) from public.expenses e where e.account_id = a.id and e.voided_at is null
                and e.paid_by = m.user_id and e.occurred_on >= p_month and e.occurred_on < (p_month + interval '1 month')::date), 0),
            'share_minor', coalesce((select sum(s.amount_minor) from public.expense_shares s join public.expenses e on e.id = s.expense_id
                where e.account_id = a.id and e.voided_at is null and s.user_id = m.user_id
                and e.occurred_on >= p_month and e.occurred_on < (p_month + interval '1 month')::date), 0),
            'opening_balance_minor', coalesce((select sum(l.delta_minor) from public.account_ledger_entries l
                where l.account_id = a.id and l.user_id = m.user_id and l.occurred_on < p_month), 0),
            'closing_balance_minor', coalesce((select sum(l.delta_minor) from public.account_ledger_entries l
                where l.account_id = a.id and l.user_id = m.user_id and l.occurred_on < (p_month + interval '1 month')::date), 0)
        ) order by m.user_id) from public.expense_account_members m join public.profiles p on p.id = m.user_id
            where m.account_id = a.id), '[]'::jsonb)
    ) into payload from public.expense_accounts a where a.id = p_account_id and a.household_id = p_household_id;
    if payload is null then raise exception 'expenses.invalid_account'; end if;
    statement_hash := encode(extensions.digest(payload::text, 'sha256'), 'hex');
    select s.id into statement_id from public.monthly_statements s
        where s.account_id = p_account_id and s.month = p_month and s.content_hash = statement_hash;
    if found then return statement_id; end if;
    select coalesce(max(s.revision), 0) + 1 into next_revision from public.monthly_statements s
        where s.account_id = p_account_id and s.month = p_month;
    insert into public.monthly_statements(household_id, account_id, month, revision, payload, content_hash)
    values (p_household_id, p_account_id, p_month, next_revision, payload, statement_hash) returning id into statement_id;
    return statement_id;
end;
$$;
revoke all on function loom_private.snapshot_account_month(uuid, uuid, date) from public, anon, authenticated;
grant execute on function loom_private.snapshot_account_month(uuid, uuid, date) to service_role;

create or replace function loom_private.snapshot_month(p_household_id uuid, p_month date)
returns uuid language sql security definer set search_path = '' as $$
    select loom_private.snapshot_account_month(p_household_id, p_household_id, p_month);
$$;

create function loom_private.save_account_statement(p_household_id uuid, p_account_id uuid, p_month date)
returns uuid language plpgsql security definer set search_path = '' as $$
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'Household access denied.'; end if;
    return loom_private.snapshot_account_month(p_household_id, p_account_id, p_month);
end;
$$;
create function public.save_account_statement(p_household_id uuid, p_account_id uuid, p_month date)
returns uuid language sql security invoker set search_path = '' as $$
    select loom_private.save_account_statement(p_household_id, p_account_id, p_month);
$$;
revoke all on function loom_private.save_account_statement(uuid, uuid, date), public.save_account_statement(uuid, uuid, date) from public, anon;
grant execute on function loom_private.save_account_statement(uuid, uuid, date), public.save_account_statement(uuid, uuid, date) to authenticated, service_role;

create or replace function loom_private.control_recurring_expense(p_household_id uuid,p_id uuid,p_action text,p_version bigint) returns public.recurring_expenses
language plpgsql security definer set search_path='' as $$
declare row public.recurring_expenses; v_today date; v_candidate date;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'expenses.message_household_access_denied'; end if;
    select * into row from public.recurring_expenses where id=p_id and household_id=p_household_id for update;
    if not found then raise exception 'expenses.message_this_recurring_expense_no_longer_exists'; end if;
    if auth.uid() not in(row.created_by,row.paid_by) and not loom_private.is_owner(p_household_id) then raise exception 'expenses.message_only_the_payer_creator_or_household_owner_can_manage'; end if;
    if p_action is null or p_action not in('pause','resume','cancel') or p_version is null then raise exception 'expenses.message_choose_a_valid_schedule_action'; end if;
    if p_action='cancel' and row.cancelled_at is not null or p_action='pause' and not row.active and row.cancelled_at is null or p_action='resume' and row.active then return row; end if;
    if row.cancelled_at is not null then raise exception 'expenses.message_a_cancelled_schedule_cannot_be_resumed'; end if;
    if row.version<>p_version then raise exception 'expenses.message_this_schedule_changed_refresh_and_try_again'; end if;
    if p_action='resume' and exists(select 1 from public.expense_accounts where id=row.account_id and archived) then raise exception 'expenses.account_archived'; end if;
    if p_action='resume' then
        if not exists(select 1 from public.household_members where household_id=p_household_id and user_id=row.paid_by and active) or
           not exists(select 1 from public.household_members where household_id=p_household_id and user_id=row.created_by and active) or
           exists(select 1 from jsonb_each(row.shares) s where not exists(select 1 from public.household_members m where m.household_id=p_household_id and m.user_id=s.key::uuid and m.active)) then raise exception 'expenses.message_all_participants_must_be_current_household_members'; end if;
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


-- Route scheduled financial work to its account rather than whichever account is open.
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

    for rule in select r.* from public.recurring_expenses r where r.active order by r.next_on for update skip locked loop
        select * into household from public.households where id=rule.household_id;
        v_today:=(now() at time zone household.time_zone)::date;
        if exists(select 1 from public.expense_accounts where id=rule.account_id and archived) or
           not exists(select 1 from public.household_members where household_id=rule.household_id and user_id=rule.paid_by and active) or
           not exists(select 1 from public.household_members where household_id=rule.household_id and user_id=rule.created_by and active) or
           exists(select 1 from jsonb_each(rule.shares) s where not exists(select 1 from public.household_members m where m.household_id=rule.household_id and m.user_id=s.key::uuid and m.active)) then
            update public.recurring_expenses set active=false where id=rule.id;
            continue;
        end if;
        v_loops:=0;
        while rule.next_on<=v_today and v_loops<24 loop
            v_expense:=gen_random_uuid();
            insert into public.expenses(id,household_id,title,amount_minor,paid_by,category,occurred_on,recurring_rule_id,created_by)
            values(v_expense,rule.household_id,rule.title,rule.amount_minor,rule.paid_by,rule.category,rule.next_on,rule.id,rule.created_by)
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


commit;
