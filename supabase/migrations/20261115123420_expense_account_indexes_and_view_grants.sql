begin;

-- Cover both columns of the scoped foreign keys without replacing date-range indexes.
create index expense_account_members_account_scope_idx on public.expense_account_members(account_id, household_id);
create index expenses_account_scope_idx on public.expenses(account_id, household_id);
create index settlements_account_scope_idx on public.settlements(account_id, household_id);
create index recurring_expenses_account_scope_idx on public.recurring_expenses(account_id, household_id);
create index monthly_statements_account_scope_idx on public.monthly_statements(account_id, household_id);

-- Supabase's default view grants are broader than these read-only client APIs need.
revoke all on public.expense_accounts_with_members, public.account_expenses_with_shares,
    public.account_ledger_entries from public, anon, authenticated;
grant select on public.expense_accounts_with_members, public.account_expenses_with_shares,
    public.account_ledger_entries to authenticated, service_role;

commit;
