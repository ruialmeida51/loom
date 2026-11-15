begin;

insert into auth.users(id, email) values
    ('00000000-0000-4000-8000-000000000041', 'accounts-owner@example.test'),
    ('00000000-0000-4000-8000-000000000042', 'accounts-member@example.test'),
    ('00000000-0000-4000-8000-000000000043', 'accounts-outsider@example.test');
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000041';
select set_config('loom.accounts.home', public.create_household('Accounts home', 'EUR', 'UTC')::text, true);
select set_config('loom.accounts.invite', public.create_household_invite(current_setting('loom.accounts.home')::uuid), true);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000042';
select public.join_household(current_setting('loom.accounts.invite'));
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000041';

select public.save_expense_account(current_setting('loom.accounts.home')::uuid,
    '00000000-0000-4000-8000-000000000440', 'Weekend away', 'GBP',
    array['00000000-0000-4000-8000-000000000041'::uuid, '00000000-0000-4000-8000-000000000042'::uuid], false, 0);
select public.save_expense_account(current_setting('loom.accounts.home')::uuid,
    '00000000-0000-4000-8000-000000000440', 'Weekend away', 'GBP',
    array['00000000-0000-4000-8000-000000000041'::uuid, '00000000-0000-4000-8000-000000000042'::uuid], false, 0);

select public.create_expense('00000000-0000-4000-8000-000000000441', current_setting('loom.accounts.home')::uuid,
    'Everyday groceries', 6000, '00000000-0000-4000-8000-000000000041', 'Groceries', current_date, false,
    '{"00000000-0000-4000-8000-000000000041":3000,"00000000-0000-4000-8000-000000000042":3000}');
select public.save_account_expense('00000000-0000-4000-8000-000000000442', current_setting('loom.accounts.home')::uuid,
    '00000000-0000-4000-8000-000000000440', 'Train', 3000, '00000000-0000-4000-8000-000000000042', 'Other', current_date, false,
    '{"00000000-0000-4000-8000-000000000041":2000,"00000000-0000-4000-8000-000000000042":1000}', 0, 'shares',
    '{"00000000-0000-4000-8000-000000000041":2,"00000000-0000-4000-8000-000000000042":1}');
select public.save_account_expense('00000000-0000-4000-8000-000000000442', current_setting('loom.accounts.home')::uuid,
    '00000000-0000-4000-8000-000000000440', 'Train', 3000, '00000000-0000-4000-8000-000000000042', 'Other', current_date, false,
    '{"00000000-0000-4000-8000-000000000041":2000,"00000000-0000-4000-8000-000000000042":1000}', 0, 'shares',
    '{"00000000-0000-4000-8000-000000000041":2,"00000000-0000-4000-8000-000000000042":1}');

do $$ begin
    assert (select count(*) from public.expense_accounts) = 2, 'Account creation retries are idempotent';
    assert (select count(*) from public.expense_account_members where account_id = current_setting('loom.accounts.home')::uuid) = 2,
        'Joining a household also joins its Everyday account';
    assert (select count(*) from public.expenses_with_shares) = 1, 'Legacy expense reads stay in Everyday';
    assert (select count(*) from public.account_expenses_with_shares) = 2, 'Expense retries do not duplicate records';
    assert (select sum(delta_minor) from public.account_ledger_entries
        where account_id = current_setting('loom.accounts.home')::uuid and user_id = '00000000-0000-4000-8000-000000000041') = 3000;
    assert (select sum(delta_minor) from public.account_ledger_entries
        where account_id = '00000000-0000-4000-8000-000000000440' and user_id = '00000000-0000-4000-8000-000000000041') = -2000,
        'Trip debts are separate from household debts and currency';
    begin
        perform public.save_expense_account(current_setting('loom.accounts.home')::uuid, gen_random_uuid(), 'Outside participant', 'EUR',
            array['00000000-0000-4000-8000-000000000041'::uuid, '00000000-0000-4000-8000-000000000043'::uuid], false, 0);
        raise exception 'Accepted a non-household participant';
    exception when raise_exception then
        if sqlerrm <> 'expenses.invalid_account_members' then raise; end if;
    end;
    begin
        perform public.save_expense_account(current_setting('loom.accounts.home')::uuid, current_setting('loom.accounts.home')::uuid,
            'Everyday', 'EUR', array['00000000-0000-4000-8000-000000000041'::uuid], true, 1);
        raise exception 'Archived Everyday';
    exception when raise_exception then
        if sqlerrm <> 'expenses.invalid_account' then raise; end if;
    end;
end; $$;

do $$ begin
    begin
        perform public.save_account_expense(gen_random_uuid(), current_setting('loom.accounts.home')::uuid,
            '00000000-0000-4000-8000-000000000440', 'Tampered split', 3000, '00000000-0000-4000-8000-000000000041', 'Other', current_date, false,
            '{"00000000-0000-4000-8000-000000000041":1500,"00000000-0000-4000-8000-000000000042":1500}', 0, 'shares',
            '{"00000000-0000-4000-8000-000000000041":2,"00000000-0000-4000-8000-000000000042":1}');
        raise exception 'Accepted split metadata that disagrees with its allocations';
    exception when raise_exception then if sqlerrm <> 'expenses.invalid_split' then raise; end if; end;
end; $$;

select public.save_account_expense('00000000-0000-4000-8000-000000000442', current_setting('loom.accounts.home')::uuid,
    '00000000-0000-4000-8000-000000000440', 'Train tickets', 3000, '00000000-0000-4000-8000-000000000042', 'Other', current_date, false,
    '{"00000000-0000-4000-8000-000000000041":1500,"00000000-0000-4000-8000-000000000042":1500}', 1, 'equal', '{}');
do $$ begin
    assert (select version from public.account_expenses_with_shares where id = '00000000-0000-4000-8000-000000000442') = 2;
    begin
        perform public.save_account_expense('00000000-0000-4000-8000-000000000442', current_setting('loom.accounts.home')::uuid,
            '00000000-0000-4000-8000-000000000440', 'Stale edit', 3000, '00000000-0000-4000-8000-000000000042', 'Other', current_date, false,
            '{"00000000-0000-4000-8000-000000000041":1500,"00000000-0000-4000-8000-000000000042":1500}', 1, 'equal', '{}');
        raise exception 'Accepted a stale expense edit';
    exception when raise_exception then if sqlerrm <> 'expenses.record_changed' then raise; end if; end;
end; $$;

select public.save_account_expense('00000000-0000-4000-8000-000000000443', current_setting('loom.accounts.home')::uuid,
    '00000000-0000-4000-8000-000000000440', 'Monthly travel pass', 5000, '00000000-0000-4000-8000-000000000041', 'Bills', current_date - 1, true,
    '{"00000000-0000-4000-8000-000000000041":2500,"00000000-0000-4000-8000-000000000042":2500}', 0, 'equal', '{}');
reset role;
update public.recurring_expenses set next_on = current_date where account_id = '00000000-0000-4000-8000-000000000440';
select loom_private.queue_due_work();
select loom_private.queue_due_work();
do $$ begin
    assert (select count(*) from public.expenses where recurring_rule_id is not null
        and account_id = '00000000-0000-4000-8000-000000000440') = 2,
        'Recurring occurrences inherit their trip account and repeated jobs do not duplicate them';
    assert (select count(*) from public.notifications where data ->> 'account_id' = '00000000-0000-4000-8000-000000000440'
        and event_key like 'expense:%' and title = 'A recurring bill was added') = 2,
        'Each trip participant receives one notification with the account destination';
end; $$;
set role authenticated;

select public.save_account_statement(current_setting('loom.accounts.home')::uuid, '00000000-0000-4000-8000-000000000440', date_trunc('month', current_date)::date);
select public.save_monthly_statement(current_setting('loom.accounts.home')::uuid, date_trunc('month', current_date)::date);
do $$ begin
    assert (select payload ->> 'currency' from public.monthly_statements
        where account_id = '00000000-0000-4000-8000-000000000440') = 'GBP';
    assert (select (payload ->> 'total_minor')::bigint from public.monthly_statements
        where account_id = current_setting('loom.accounts.home')::uuid) = 6000,
        'Everyday statements do not mix in trip expenses';
end; $$;

select public.save_expense_account(current_setting('loom.accounts.home')::uuid, '00000000-0000-4000-8000-000000000440',
    'Weekend away', 'GBP', array['00000000-0000-4000-8000-000000000041'::uuid, '00000000-0000-4000-8000-000000000042'::uuid], true, 1);
do $$ begin
    assert not (select active from public.recurring_expenses where account_id = '00000000-0000-4000-8000-000000000440');
    begin
        perform public.control_recurring_expense(current_setting('loom.accounts.home')::uuid,
            (select id from public.recurring_expenses where account_id = '00000000-0000-4000-8000-000000000440'), 'resume',
            (select version from public.recurring_expenses where account_id = '00000000-0000-4000-8000-000000000440'));
        raise exception 'Resumed an archived account schedule';
    exception when raise_exception then if sqlerrm <> 'expenses.account_archived' then raise; end if; end;
end; $$;
select public.record_account_settlement('00000000-0000-4000-8000-000000000444', current_setting('loom.accounts.home')::uuid,
    '00000000-0000-4000-8000-000000000440', '00000000-0000-4000-8000-000000000041', '00000000-0000-4000-8000-000000000042', 1500, current_date);
do $$ begin
    assert (select sum(delta_minor) from public.account_ledger_entries
        where account_id = current_setting('loom.accounts.home')::uuid and user_id = '00000000-0000-4000-8000-000000000041') = 3000,
        'A repayment in an archived trip never changes Everyday balances';
end; $$;

set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000043';
do $$ begin
    assert (select count(*) from public.expense_accounts) = 0, 'Outsiders cannot read household accounts';
    assert (select count(*) from public.account_expenses_with_shares) = 0, 'Outsiders cannot read account expenses';
    begin
        perform public.delete_account_expense(current_setting('loom.accounts.home')::uuid,
            '00000000-0000-4000-8000-000000000440', '00000000-0000-4000-8000-000000000442', 2);
        raise exception 'Outsider deleted a household expense';
    exception when raise_exception then if sqlerrm <> 'expenses.account_access_denied' then raise; end if; end;
end; $$;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000041';
select public.save_expense_account(current_setting('loom.accounts.home')::uuid, '00000000-0000-4000-8000-000000000440',
    'Weekend away', 'GBP', array['00000000-0000-4000-8000-000000000041'::uuid, '00000000-0000-4000-8000-000000000042'::uuid], false, 2);
select public.delete_account_expense(current_setting('loom.accounts.home')::uuid, '00000000-0000-4000-8000-000000000440',
    '00000000-0000-4000-8000-000000000442', 2);
do $$ begin
    assert not exists (select 1 from public.account_expenses_with_shares where id = '00000000-0000-4000-8000-000000000442'),
        'Deleted expenses leave live balances without rewriting stored statements';
    assert (select payload ->> 'currency' from public.monthly_statements where account_id = '00000000-0000-4000-8000-000000000440') = 'GBP';
end; $$;

rollback;
