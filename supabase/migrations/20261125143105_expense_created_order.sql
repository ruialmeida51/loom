begin;

-- Expose when each expense was added so the ledger lists records in the order they were entered.
-- Appending a column keeps the view compatible with clients that ignore unknown fields.
create or replace view public.account_expenses_with_shares with (security_invoker = true) as
select e.id, e.household_id, e.account_id, e.title, e.amount_minor, e.paid_by, e.category,
       e.occurred_on, e.created_by, e.version, e.split_method, e.split_values,
       e.recurring_rule_id is not null as recurring,
       coalesce((select jsonb_object_agg(s.user_id::text, s.amount_minor)
           from public.expense_shares s where s.expense_id = e.id), '{}'::jsonb) as shares,
       e.created_at
from public.expenses e where e.voided_at is null;

commit;
