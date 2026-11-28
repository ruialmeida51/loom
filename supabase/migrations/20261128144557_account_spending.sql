begin;

-- What was spent in an account and who paid it, for sharing a trip's summary. Like
-- account_balances it is a security-invoker view, so callers keep the expenses read policy (their
-- own households only), and voided expenses are left out. Guests can pay, so this groups by
-- whoever paid rather than by household member.
create view public.account_spending with (security_invoker = true) as
select household_id, account_id, paid_by as user_id,
       count(*)::bigint as expense_count, sum(amount_minor)::bigint as paid_minor
from public.expenses
where voided_at is null and account_id is not null
group by household_id, account_id, paid_by;
revoke all on public.account_spending from public, anon;
grant select on public.account_spending to authenticated, service_role;

commit;
