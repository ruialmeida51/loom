begin;

-- Guests: named people outside the household who take part in one trip or event account. They
-- pay, share and settle there like members, recorded by the account's members, and never appear
-- anywhere else. See docs/plans/account-guests.md.

-- Feature switches per household. No row means on, so every feature ships enabled and can be
-- switched off later without a client release.
create table public.household_features (
    household_id uuid not null references public.households(id) on delete cascade,
    feature text not null check (feature ~ '^[a-z_]{2,40}$'),
    enabled boolean not null,
    primary key (household_id, feature)
);
alter table public.household_features enable row level security;
revoke all on public.household_features from public, anon, authenticated;
grant select on public.household_features to authenticated;
grant all on public.household_features to service_role;
create policy household_features_read on public.household_features for select to authenticated
    using (loom_private.is_member(household_id));

create function loom_private.feature_enabled(p_household_id uuid, p_feature text) returns boolean
language sql stable security definer set search_path = '' as $$
    select coalesce((select enabled from public.household_features
        where household_id = p_household_id and feature = p_feature), true);
$$;
revoke all on function loom_private.feature_enabled(uuid, text) from public, anon;
grant execute on function loom_private.feature_enabled(uuid, text) to authenticated, service_role;

create table public.expense_account_guests (
    id uuid primary key,
    account_id uuid not null,
    household_id uuid not null,
    name text not null check (length(trim(name)) between 1 and 40),
    removed_at timestamptz,
    version bigint not null default 1,
    created_by uuid not null references public.profiles(id),
    created_at timestamptz not null default now(),
    unique (id, account_id),
    foreign key (account_id, household_id) references public.expense_accounts(id, household_id) on delete cascade
);
create index expense_account_guests_account_idx on public.expense_account_guests(account_id);
create index expense_account_guests_household_idx on public.expense_account_guests(household_id);
create index expense_account_guests_creator_idx on public.expense_account_guests(created_by);
alter table public.expense_account_guests enable row level security;
revoke all on public.expense_account_guests from public, anon, authenticated;
grant select on public.expense_account_guests to authenticated;
grant all on public.expense_account_guests to service_role;
create policy expense_account_guests_read on public.expense_account_guests for select to authenticated
    using (loom_private.is_member(household_id));

-- Everyone who can take part in an account's money: its household members and its guests.
-- `active` is false for former members and removed guests, who stay valid on their old records.
create view loom_private.account_participants as
select am.account_id, am.household_id, am.user_id, m.active
from public.expense_account_members am
join public.household_members m on m.household_id = am.household_id and m.user_id = am.user_id
union all
select g.account_id, g.household_id, g.id as user_id, g.removed_at is null as active
from public.expense_account_guests g;
revoke all on loom_private.account_participants from public, anon, authenticated;
grant select on loom_private.account_participants to service_role;

-- Participants were foreign keys to household_members, which guests are not. A constraint trigger
-- per table keeps the same guarantee: every participant belongs to the record's account.
alter table public.expenses drop constraint expenses_household_id_paid_by_fkey;
alter table public.expense_shares drop constraint expense_shares_household_id_user_id_fkey;
alter table public.settlements drop constraint settlements_household_id_from_user_fkey;
alter table public.settlements drop constraint settlements_household_id_to_user_fkey;
alter table public.recurring_expenses drop constraint recurring_expenses_household_id_paid_by_fkey;

create function loom_private.is_account_participant(p_account_id uuid, p_user_id uuid) returns boolean
language sql stable security definer set search_path = '' as $$
    select exists(select 1 from loom_private.account_participants
        where account_id = p_account_id and user_id = p_user_id);
$$;
revoke all on function loom_private.is_account_participant(uuid, uuid) from public, anon, authenticated;

create function loom_private.guard_account_participants() returns trigger
language plpgsql security definer set search_path = '' as $$
declare v_account uuid;
begin
    if tg_table_name = 'expense_shares' then
        select account_id into v_account from public.expenses where id = new.expense_id;
        if not loom_private.is_account_participant(v_account, new.user_id) then
            raise exception 'expenses.invalid_account_members';
        end if;
    elsif tg_table_name = 'settlements' then
        if not loom_private.is_account_participant(new.account_id, new.from_user)
            or not loom_private.is_account_participant(new.account_id, new.to_user) then
            raise exception 'expenses.invalid_account_members';
        end if;
    elsif not loom_private.is_account_participant(new.account_id, new.paid_by) then
        raise exception 'expenses.invalid_account_members';
    end if;
    return new;
end;
$$;
revoke all on function loom_private.guard_account_participants() from public, anon, authenticated;
create constraint trigger expenses_participants after insert or update of paid_by, account_id on public.expenses
    deferrable initially deferred for each row execute function loom_private.guard_account_participants();
create constraint trigger expense_shares_participants after insert or update of user_id on public.expense_shares
    deferrable initially deferred for each row execute function loom_private.guard_account_participants();
create constraint trigger settlements_participants after insert or update of from_user, to_user, account_id on public.settlements
    deferrable initially deferred for each row execute function loom_private.guard_account_participants();
create constraint trigger recurring_participants after insert or update of paid_by, account_id on public.recurring_expenses
    deferrable initially deferred for each row execute function loom_private.guard_account_participants();

-- Existing validations now accept the account's guests wherever they accepted its members.
CREATE OR REPLACE FUNCTION loom_private.validate_account_expense(p_household_id uuid, p_account_id uuid, p_title text, p_amount_minor bigint, p_paid_by uuid, p_occurred_on date, p_shares jsonb)
 RETURNS void
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO ''
AS $function$
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
    if not exists (select 1 from loom_private.account_participants m
        where m.account_id = p_account_id and m.user_id = p_paid_by and m.active) or
       exists (select 1 from jsonb_each(p_shares) s where not exists (
           select 1 from loom_private.account_participants m
           where m.account_id = p_account_id and m.user_id = s.key::uuid and m.active
       )) then raise exception 'expenses.invalid_account_members'; end if;
end;
$function$;

CREATE OR REPLACE FUNCTION loom_private.validate_retained_account_expense(p_household_id uuid, p_account_id uuid, p_title text, p_amount_minor bigint, p_paid_by uuid, p_occurred_on date, p_shares jsonb, p_existing_id uuid)
 RETURNS void
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO ''
AS $function$
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
    if not exists (select 1 from loom_private.account_participants m
        where m.account_id = p_account_id and m.user_id = p_paid_by and (m.active or exists(
            select 1 from public.expenses original where original.id = p_existing_id and original.household_id = p_household_id
                and original.account_id = p_account_id and original.paid_by = p_paid_by))) or
       exists (select 1 from jsonb_each(p_shares) s where not exists (
           select 1 from loom_private.account_participants m
           where m.account_id = p_account_id and m.user_id = s.key::uuid and (m.active or exists(
               select 1 from public.expense_shares old_share join public.expenses original on original.id = old_share.expense_id
               where original.id = p_existing_id and original.household_id = p_household_id and original.account_id = p_account_id
                   and old_share.user_id = s.key::uuid))
       )) then raise exception 'expenses.invalid_account_members'; end if;
end;
$function$;

CREATE OR REPLACE FUNCTION loom_private.record_account_settlement(p_id uuid, p_household_id uuid, p_account_id uuid, p_from uuid, p_to uuid, p_amount_minor bigint, p_occurred_on date)
 RETURNS uuid
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO ''
AS $function$
declare existing public.settlements;
begin
    perform 1 from public.households where id = p_household_id for update;
    -- Members record their own repayments; any account member records a guest's.
    if auth.uid() is null or not loom_private.is_member(p_household_id) or (auth.uid() not in (p_from, p_to) and not (
        exists(select 1 from public.expense_account_guests g where g.account_id = p_account_id and g.id in (p_from, p_to))
        and exists(select 1 from public.expense_account_members am where am.account_id = p_account_id and am.user_id = auth.uid())
    )) or not exists (
        select 1 from public.expense_accounts where id = p_account_id and household_id = p_household_id
    ) then raise exception 'You can only record repayments you send or receive.'; end if;
    if p_from is null or p_to is null or p_from = p_to or p_amount_minor is null or p_amount_minor not between 1 and 100000000000 or
       p_occurred_on is null or p_occurred_on > (select (now() at time zone time_zone)::date from public.households where id = p_household_id) then
        raise exception 'Choose two people and a positive amount.';
    end if;
    if (select count(*) from loom_private.account_participants m
        where m.account_id = p_account_id and m.user_id in (p_from, p_to) and (m.active or exists(select 1 from public.account_ledger_entries history
            where history.account_id = p_account_id and history.household_id = p_household_id and history.user_id = m.user_id))) <> 2 then
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
$function$;

CREATE OR REPLACE FUNCTION loom_private.save_account_settlement(p_household_id uuid, p_account_id uuid, p_id uuid, p_from uuid, p_to uuid, p_amount_minor bigint, p_occurred_on date, p_version bigint)
 RETURNS settlements
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO ''
AS $function$
declare existing public.settlements; result public.settlements;
begin
    perform 1 from public.households where id = p_household_id for update;
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'expenses.payment_permission_denied'; end if;
    perform 1 from public.expense_accounts where id = p_account_id and household_id = p_household_id and not archived for no key update;
    if not found then raise exception 'expenses.account_archived'; end if;
    select * into existing from public.settlements where id = p_id and household_id = p_household_id and account_id = p_account_id for update;
    if not found or existing.voided_at is not null then raise exception 'expenses.payment_changed'; end if;
    if auth.uid() not in (existing.from_user, existing.to_user, existing.created_by) and not loom_private.is_owner(p_household_id) then
        raise exception 'expenses.payment_permission_denied';
    end if;
    if p_from is null or p_to is null or p_from = p_to or p_amount_minor is null or p_amount_minor not between 1 and 100000000000 or
       p_occurred_on is null or p_occurred_on > (select (now() at time zone time_zone)::date from public.households where id = p_household_id) then
        raise exception 'Choose two people and a positive amount.';
    end if;
    if (select count(*) from loom_private.account_participants m
        where m.account_id = p_account_id and m.user_id in (p_from, p_to) and (m.active or exists(select 1 from public.account_ledger_entries history
            where history.account_id = p_account_id and history.household_id = p_household_id and history.user_id = m.user_id))) <> 2 then
        raise exception 'expenses.invalid_account_members';
    end if;
    if (existing.from_user, existing.to_user, existing.amount_minor, existing.occurred_on) is not distinct from
       (p_from, p_to, p_amount_minor, p_occurred_on) then return existing; end if;
    if existing.version is distinct from p_version then raise exception 'expenses.payment_changed'; end if;
    update public.settlements set from_user = p_from, to_user = p_to, amount_minor = p_amount_minor,
        occurred_on = p_occurred_on, version = version + 1 where id = p_id returning * into result;
    return result;
end;
$function$;

CREATE OR REPLACE FUNCTION loom_private.save_recurring_expense(p_household_id uuid, p_id uuid, p_version bigint, p_title text, p_amount_minor bigint, p_paid_by uuid, p_next_on date, p_shares jsonb, p_split_method text, p_split_values jsonb)
 RETURNS recurring_expenses
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO ''
AS $function$
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
        select 1 from loom_private.account_participants m
        where m.account_id = schedule.account_id and m.user_id = p_paid_by and m.active)) or
        exists(select 1 from jsonb_each(p_shares) s where not schedule.shares ? s.key and not exists(
            select 1 from loom_private.account_participants m
            where m.account_id = schedule.account_id and m.user_id = s.key::uuid and m.active)) then
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
$function$;


-- Creates a guest or renames one. Only trip/event accounts take guests, at most 20 active each.
create function loom_private.save_account_guest(p_household_id uuid, p_account_id uuid, p_id uuid, p_name text, p_version bigint)
returns public.expense_account_guests language plpgsql security definer set search_path = '' as $$
declare existing public.expense_account_guests; account public.expense_accounts; result public.expense_account_guests;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) or not exists(
        select 1 from public.expense_account_members where account_id = p_account_id and user_id = auth.uid()
    ) then raise exception 'expenses.account_access_denied'; end if;
    select * into account from public.expense_accounts where id = p_account_id and household_id = p_household_id for update;
    if not found then raise exception 'expenses.account_access_denied'; end if;
    if account.archived then raise exception 'expenses.account_archived'; end if;
    if account.is_default then raise exception 'expenses.guests_not_on_everyday'; end if;
    if p_name is null or length(trim(p_name)) not between 1 and 40 then raise exception 'expenses.invalid_guest_name'; end if;
    select * into existing from public.expense_account_guests where id = p_id for update;
    if found then
        if existing.account_id <> p_account_id or existing.removed_at is not null then raise exception 'expenses.record_changed'; end if;
        if existing.name = trim(p_name) then return existing; end if;
        if existing.version <> p_version then raise exception 'expenses.record_changed'; end if;
        update public.expense_account_guests set name = trim(p_name), version = version + 1 where id = p_id returning * into result;
        return result;
    end if;
    if p_version <> 0 then raise exception 'expenses.record_changed'; end if;
    if not loom_private.feature_enabled(p_household_id, 'account_guests') then raise exception 'features.unavailable'; end if;
    if (select count(*) from public.expense_account_guests where account_id = p_account_id and removed_at is null) >= 20 then
        raise exception 'expenses.guest_limit';
    end if;
    insert into public.expense_account_guests(id, account_id, household_id, name, created_by)
    values (p_id, p_account_id, p_household_id, trim(p_name), auth.uid()) returning * into result;
    return result;
end;
$$;

-- Removes a settled guest. Their records stay, like a former member's; recurring bills that
-- include them pause so they never charge someone who left.
create function loom_private.remove_account_guest(p_household_id uuid, p_account_id uuid, p_id uuid, p_version bigint)
returns void language plpgsql security definer set search_path = '' as $$
declare existing public.expense_account_guests;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) or not exists(
        select 1 from public.expense_account_members where account_id = p_account_id and user_id = auth.uid()
    ) then raise exception 'expenses.account_access_denied'; end if;
    perform 1 from public.expense_accounts where id = p_account_id and household_id = p_household_id for update;
    select * into existing from public.expense_account_guests where id = p_id and account_id = p_account_id for update;
    if not found then raise exception 'expenses.record_changed'; end if;
    if existing.removed_at is not null then return; end if;
    if existing.version <> p_version then raise exception 'expenses.record_changed'; end if;
    if coalesce((select sum(delta_minor) from public.account_ledger_entries
        where account_id = p_account_id and user_id = p_id), 0) <> 0 then
        raise exception 'expenses.guest_has_balance';
    end if;
    update public.expense_account_guests set removed_at = now(), version = version + 1 where id = p_id;
    update public.recurring_expenses set active = false, version = version + 1
        where account_id = p_account_id and active and (paid_by = p_id or shares ? p_id::text);
end;
$$;
create function public.save_account_guest(p_household_id uuid, p_account_id uuid, p_id uuid, p_name text, p_version bigint)
returns public.expense_account_guests language sql security invoker set search_path = '' as $$
    select loom_private.save_account_guest(p_household_id, p_account_id, p_id, p_name, p_version);
$$;
create function public.remove_account_guest(p_household_id uuid, p_account_id uuid, p_id uuid, p_version bigint)
returns void language sql security invoker set search_path = '' as $$
    select loom_private.remove_account_guest(p_household_id, p_account_id, p_id, p_version);
$$;
revoke all on function loom_private.save_account_guest(uuid, uuid, uuid, text, bigint), public.save_account_guest(uuid, uuid, uuid, text, bigint),
    loom_private.remove_account_guest(uuid, uuid, uuid, bigint), public.remove_account_guest(uuid, uuid, uuid, bigint) from public, anon;
grant execute on function loom_private.save_account_guest(uuid, uuid, uuid, text, bigint), public.save_account_guest(uuid, uuid, uuid, text, bigint),
    loom_private.remove_account_guest(uuid, uuid, uuid, bigint), public.remove_account_guest(uuid, uuid, uuid, bigint) to authenticated, service_role;

-- Accounts carry their guests; the appended column is ignored by older clients.
create or replace view public.expense_accounts_with_members with (security_invoker = true) as
select a.id, a.household_id, a.name, a.currency, a.is_default, a.archived, a.version, a.created_by, a.created_at,
    coalesce((select jsonb_agg(m.user_id order by m.user_id)
        from public.expense_account_members m where m.account_id = a.id), '[]'::jsonb) as member_ids,
    coalesce((select jsonb_agg(jsonb_build_object('id', g.id, 'name', g.name, 'removed', g.removed_at is not null,
        'version', g.version) order by g.created_at, g.id)
        from public.expense_account_guests g where g.account_id = a.id), '[]'::jsonb) as guests
from public.expense_accounts a;

commit;
