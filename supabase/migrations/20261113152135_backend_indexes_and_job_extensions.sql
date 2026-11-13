-- Cover the exact foreign-key prefixes used by membership checks and related-row maintenance.
create index shopping_household_creator_idx on public.shopping_items(household_id,created_by);
create index reminders_household_creator_idx on public.reminders(household_id,created_by);
create index expenses_household_payer_idx on public.expenses(household_id,paid_by);
create index expenses_household_creator_idx on public.expenses(household_id,created_by);
create index shares_expense_household_idx on public.expense_shares(expense_id,household_id);
create index recurring_household_payer_idx on public.recurring_expenses(household_id,paid_by);
create index recurring_household_creator_idx on public.recurring_expenses(household_id,created_by);
create index settlements_household_from_idx on public.settlements(household_id,from_user);
create index settlements_household_to_idx on public.settlements(household_id,to_user);
create index feed_household_creator_idx on public.feed_subscriptions(household_id,created_by);
create index article_subscription_household_idx on public.feed_articles(subscription_id,household_id);
create index invite_household_idx on loom_private.household_invites(household_id);
create index invite_creator_idx on loom_private.household_invites(created_by);
create index invite_used_by_idx on loom_private.household_invites(used_by);
create index delivery_device_idx on loom_private.notification_deliveries(device_id);

-- An explicit deny policy documents that clients have no row access to server-only tables.
create policy server_only_invites on loom_private.household_invites to authenticated using(false) with check(false);
create policy server_only_deliveries on loom_private.notification_deliveries to authenticated using(false) with check(false);
create policy server_only_statements on loom_private.statement_runs to authenticated using(false) with check(false);
create policy server_only_credentials on loom_private.worker_auth to authenticated using(false) with check(false);

-- Supabase provides these extensions. The lightweight PostgreSQL test image intentionally does not.
do $$begin
    if exists(select 1 from pg_available_extensions where name='pg_cron') then create extension if not exists pg_cron; end if;
    if exists(select 1 from pg_available_extensions where name='pg_net') then create extension if not exists pg_net with schema extensions; end if;
end;$$;
