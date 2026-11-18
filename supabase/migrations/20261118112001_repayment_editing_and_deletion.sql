begin;

alter table public.settlements add column version bigint not null default 1;
alter table public.settlements add column voided_at timestamptz;

-- Tombstones preserve optimistic concurrency and prevent a late retry resurrecting a payment.
alter policy settlements_read on public.settlements
using (voided_at is null and loom_private.is_member(household_id));

create or replace view public.account_ledger_entries with (security_invoker = true) as
select account_id, household_id, occurred_on, paid_by as user_id, amount_minor as delta_minor
from public.expenses where voided_at is null
union all
select e.account_id, s.household_id, e.occurred_on, s.user_id, -s.amount_minor
from public.expense_shares s join public.expenses e on e.id = s.expense_id where e.voided_at is null
union all select account_id, household_id, occurred_on, from_user, amount_minor from public.settlements where voided_at is null
union all select account_id, household_id, occurred_on, to_user, -amount_minor from public.settlements where voided_at is null;

create function loom_private.save_account_settlement(
    p_household_id uuid, p_account_id uuid, p_id uuid, p_from uuid, p_to uuid,
    p_amount_minor bigint, p_occurred_on date, p_version bigint
) returns public.settlements language plpgsql security definer set search_path = '' as $$
declare existing public.settlements; result public.settlements;
begin
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
    if (select count(*) from public.expense_account_members a join public.household_members m
        on m.household_id = a.household_id and m.user_id = a.user_id
        where a.account_id = p_account_id and a.user_id in (p_from, p_to) and m.active) <> 2 then
        raise exception 'expenses.invalid_account_members';
    end if;
    if (existing.from_user, existing.to_user, existing.amount_minor, existing.occurred_on) is not distinct from
       (p_from, p_to, p_amount_minor, p_occurred_on) then return existing; end if;
    if existing.version is distinct from p_version then raise exception 'expenses.payment_changed'; end if;
    update public.settlements set from_user = p_from, to_user = p_to, amount_minor = p_amount_minor,
        occurred_on = p_occurred_on, version = version + 1 where id = p_id returning * into result;
    return result;
end;
$$;

create function loom_private.delete_account_settlement(p_household_id uuid, p_account_id uuid, p_id uuid, p_version bigint)
returns void language plpgsql security definer set search_path = '' as $$
declare existing public.settlements;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'expenses.payment_permission_denied'; end if;
    perform 1 from public.expense_accounts where id = p_account_id and household_id = p_household_id and not archived for no key update;
    if not found then raise exception 'expenses.account_archived'; end if;
    select * into existing from public.settlements where id = p_id and household_id = p_household_id and account_id = p_account_id for update;
    if not found then raise exception 'expenses.payment_changed'; end if;
    if auth.uid() not in (existing.from_user, existing.to_user, existing.created_by) and not loom_private.is_owner(p_household_id) then
        raise exception 'expenses.payment_permission_denied';
    end if;
    if existing.voided_at is not null and existing.version = p_version + 1 then return; end if;
    if existing.voided_at is not null or existing.version is distinct from p_version then raise exception 'expenses.payment_changed'; end if;
    update public.settlements set voided_at = now(), version = version + 1 where id = p_id;
end;
$$;

create function public.save_account_settlement(p_household_id uuid, p_account_id uuid, p_id uuid, p_from uuid, p_to uuid,
    p_amount_minor bigint, p_occurred_on date, p_version bigint)
returns public.settlements language sql security invoker set search_path = '' as $$
    select loom_private.save_account_settlement(p_household_id,p_account_id,p_id,p_from,p_to,p_amount_minor,p_occurred_on,p_version);
$$;
create function public.delete_account_settlement(p_household_id uuid, p_account_id uuid, p_id uuid, p_version bigint)
returns void language sql security invoker set search_path = '' as $$
    select loom_private.delete_account_settlement(p_household_id,p_account_id,p_id,p_version);
$$;
revoke all on function loom_private.save_account_settlement(uuid,uuid,uuid,uuid,uuid,bigint,date,bigint),
    public.save_account_settlement(uuid,uuid,uuid,uuid,uuid,bigint,date,bigint),
    loom_private.delete_account_settlement(uuid,uuid,uuid,bigint), public.delete_account_settlement(uuid,uuid,uuid,bigint)
    from public,anon;
grant execute on function loom_private.save_account_settlement(uuid,uuid,uuid,uuid,uuid,bigint,date,bigint),
    public.save_account_settlement(uuid,uuid,uuid,uuid,uuid,bigint,date,bigint),
    loom_private.delete_account_settlement(uuid,uuid,uuid,bigint), public.delete_account_settlement(uuid,uuid,uuid,bigint)
    to authenticated,service_role;

commit;
