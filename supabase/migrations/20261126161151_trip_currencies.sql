begin;

-- Trips abroad: an expense can be entered in the currency it was paid in. It keeps that amount and
-- the rate it was converted at, while amount_minor, shares and balances stay in the account's
-- currency. Older clients send none of the new arguments and keep saving as before.
alter table public.expenses
    add column original_currency text check (original_currency ~ '^[A-Z]{3}$'),
    add column original_amount_minor bigint check (original_amount_minor between 1 and 100000000000),
    add column exchange_rate numeric(20, 10) check (exchange_rate > 0),
    add constraint expenses_original_amount_complete check (
        (original_currency is null and original_amount_minor is null and exchange_rate is null) or
        (original_currency is not null and original_amount_minor is not null and exchange_rate is not null));

-- The other currency must differ from the account's, and the converted amount must be the
-- original times the rate, rounded to the nearest cent. Recurring bills stay in one currency,
-- since a rate would go stale month to month.
create function loom_private.validate_original_amount(p_account_id uuid, p_amount_minor bigint,
    p_original_currency text, p_original_amount_minor bigint, p_exchange_rate numeric, p_recurring boolean)
returns void language plpgsql stable security definer set search_path = '' as $$
begin
    if p_original_currency is null and p_original_amount_minor is null and p_exchange_rate is null then return; end if;
    if p_original_currency is null or p_original_currency !~ '^[A-Z]{3}$' or p_original_amount_minor is null
        or p_original_amount_minor not between 1 and 100000000000 or p_exchange_rate is null or p_exchange_rate <= 0
        or p_exchange_rate > 1000000 then
        raise exception 'expenses.invalid_conversion';
    end if;
    if p_recurring then raise exception 'expenses.recurring_single_currency'; end if;
    if p_original_currency = (select currency from public.expense_accounts where id = p_account_id) then
        raise exception 'expenses.invalid_conversion';
    end if;
    if p_amount_minor <> round(p_original_amount_minor * p_exchange_rate) then
        raise exception 'expenses.invalid_conversion';
    end if;
end;
$$;
revoke all on function loom_private.validate_original_amount(uuid, bigint, text, bigint, numeric, boolean)
    from public, anon, authenticated;

drop function public.save_account_expense(uuid, uuid, uuid, text, bigint, uuid, text, date, boolean, jsonb, bigint, text, jsonb);
drop function loom_private.save_account_expense(uuid, uuid, uuid, text, bigint, uuid, text, date, boolean, jsonb, bigint, text, jsonb);

CREATE FUNCTION loom_private.save_account_expense(p_id uuid, p_household_id uuid, p_account_id uuid, p_title text, p_amount_minor bigint, p_paid_by uuid, p_category text, p_occurred_on date, p_recurring boolean, p_shares jsonb, p_version bigint, p_split_method text, p_split_values jsonb,
    p_original_currency text DEFAULT NULL, p_original_amount_minor bigint DEFAULT NULL, p_exchange_rate numeric DEFAULT NULL)
 RETURNS uuid
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO ''
AS $function$
declare existing public.expenses; rule_id uuid;
begin
    perform 1 from public.households where id = p_household_id for update;
    if auth.uid() is null or not loom_private.is_member(p_household_id) then
        raise exception 'expenses.account_access_denied';
    end if;
    perform 1 from public.expense_accounts where id = p_account_id and household_id = p_household_id for no key update;
    perform loom_private.validate_retained_account_expense(p_household_id, p_account_id, p_title, p_amount_minor, p_paid_by, p_occurred_on, p_shares, case when p_version > 0 then p_id else null end);
    if p_version is null or p_version < 0 or p_split_method is null or
       p_split_method not in ('equal', 'shares', 'amounts', 'percentages') or
       p_split_values is null or jsonb_typeof(p_split_values) <> 'object' then raise exception 'expenses.invalid_split'; end if;
    perform loom_private.validate_expense_split(p_amount_minor, p_shares, p_split_method, p_split_values);
    -- An expense paid in another currency keeps what was paid and the rate used; everything that
    -- moves balances stays in the account's currency.
    perform loom_private.validate_original_amount(p_account_id, p_amount_minor, p_original_currency,
        p_original_amount_minor, p_exchange_rate, p_recurring);
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
           existing.original_currency is not distinct from p_original_currency and
           existing.original_amount_minor is not distinct from p_original_amount_minor and
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
            split_values = p_split_values, original_currency = p_original_currency,
            original_amount_minor = p_original_amount_minor, exchange_rate = p_exchange_rate,
            version = version + 1 where id = p_id;
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
            occurred_on, recurring_rule_id, created_by, split_method, split_values,
            original_currency, original_amount_minor, exchange_rate)
        values (p_id, p_household_id, p_account_id, trim(p_title), p_amount_minor, p_paid_by, p_category,
            p_occurred_on, rule_id, auth.uid(), p_split_method, p_split_values,
            p_original_currency, p_original_amount_minor, p_exchange_rate);
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
$function$;

create function public.save_account_expense(p_id uuid, p_household_id uuid, p_account_id uuid, p_title text,
    p_amount_minor bigint, p_paid_by uuid, p_category text, p_occurred_on date, p_recurring boolean,
    p_shares jsonb, p_version bigint, p_split_method text, p_split_values jsonb,
    p_original_currency text default null, p_original_amount_minor bigint default null,
    p_exchange_rate numeric default null)
returns uuid language sql security invoker set search_path = '' as $$
    select loom_private.save_account_expense(p_id, p_household_id, p_account_id, p_title, p_amount_minor,
        p_paid_by, p_category, p_occurred_on, p_recurring, p_shares, p_version, p_split_method, p_split_values,
        p_original_currency, p_original_amount_minor, p_exchange_rate);
$$;
revoke all on function loom_private.save_account_expense(uuid, uuid, uuid, text, bigint, uuid, text, date, boolean, jsonb, bigint, text, jsonb, text, bigint, numeric),
    public.save_account_expense(uuid, uuid, uuid, text, bigint, uuid, text, date, boolean, jsonb, bigint, text, jsonb, text, bigint, numeric)
    from public, anon;
grant execute on function loom_private.save_account_expense(uuid, uuid, uuid, text, bigint, uuid, text, date, boolean, jsonb, bigint, text, jsonb, text, bigint, numeric),
    public.save_account_expense(uuid, uuid, uuid, text, bigint, uuid, text, date, boolean, jsonb, bigint, text, jsonb, text, bigint, numeric)
    to authenticated, service_role;

-- The new columns are appended, so older clients keep reading the ones they know.
create or replace view public.account_expenses_with_shares with (security_invoker = true) as
select id, household_id, account_id, title, amount_minor, paid_by, category, occurred_on, created_by, version,
    split_method, split_values, (recurring_rule_id is not null) as recurring,
    coalesce((select jsonb_object_agg(s.user_id::text, s.amount_minor)
        from public.expense_shares s where s.expense_id = e.id), '{}'::jsonb) as shares,
    created_at, original_currency, original_amount_minor, exchange_rate
from public.expenses e
where voided_at is null;

commit;
