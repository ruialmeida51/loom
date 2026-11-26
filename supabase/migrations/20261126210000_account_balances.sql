begin;

-- Balances come from the server so they stay right however long an account's history grows. The
-- client used to download every expense and repayment and add them up, which PostgREST silently
-- caps at its response row limit. A security-invoker view keeps the callers' existing policies:
-- a person sees balances only for households they belong to, and repayments that were deleted or
-- expenses that were voided are already excluded by public.account_ledger_entries.
create view public.account_balances with (security_invoker = true) as
select household_id, account_id, user_id, sum(delta_minor)::bigint as balance_minor
from public.account_ledger_entries
group by household_id, account_id, user_id;
revoke all on public.account_balances from public, anon;
grant select on public.account_balances to authenticated, service_role;

-- The ledger lists the newest records first and pages with a (created_at, id) keyset cursor.
-- These indexes serve each page from the account's newest rows instead of sorting its history.
create index expenses_account_created_cursor_idx
    on public.expenses(account_id, created_at desc, id) where voided_at is null;
create index settlements_account_created_cursor_idx
    on public.settlements(account_id, created_at desc, id) where voided_at is null;

commit;
