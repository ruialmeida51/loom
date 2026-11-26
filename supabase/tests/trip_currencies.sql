begin;
insert into auth.users(id, email) values
    ('00000000-0000-4000-8000-000000000c01', 'fx-owner@example.test'),
    ('00000000-0000-4000-8000-000000000c02', 'fx-alex@example.test');
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000c01';
select set_config('loom.fx.home', public.create_household('FX home', 'EUR', 'UTC')::text, true);
select set_config('loom.fx.invite', public.create_household_invite(current_setting('loom.fx.home')::uuid), true);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000c02';
select public.redeem_household_invitation(current_setting('loom.fx.invite'));
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000c01';
select public.save_expense_account(current_setting('loom.fx.home')::uuid,
    '00000000-0000-4000-8000-000000000c10', 'London', 'EUR',
    array['00000000-0000-4000-8000-000000000c01'::uuid, '00000000-0000-4000-8000-000000000c02'::uuid], false, 0);

do $$
declare
    home uuid := current_setting('loom.fx.home')::uuid;
    trip uuid := '00000000-0000-4000-8000-000000000c10';
    you uuid := '00000000-0000-4000-8000-000000000c01';
    alex uuid := '00000000-0000-4000-8000-000000000c02';
    dinner uuid := '00000000-0000-4000-8000-000000000c20';
    row record;
begin
    -- £48.00 at 1.1546 is €55.42; balances move in euros.
    perform public.save_account_expense(p_id => dinner, p_household_id => home, p_account_id => trip,
        p_title => 'Fish market', p_amount_minor => 5542, p_paid_by => you, p_category => 'Food',
        p_occurred_on => current_date, p_recurring => false,
        p_shares => jsonb_build_object(you::text, 2771, alex::text, 2771), p_version => 0,
        p_split_method => 'equal', p_split_values => '{}'::jsonb,
        p_original_currency => 'GBP', p_original_amount_minor => 4800, p_exchange_rate => 1.1546);
    select * into row from public.account_expenses_with_shares where id = dinner;
    assert row.amount_minor = 5542 and row.original_currency = 'GBP' and row.original_amount_minor = 4800
        and row.exchange_rate = 1.1546, 'The original amount and rate are kept';
    assert (select sum(delta_minor) from public.account_ledger_entries where account_id = trip and user_id = alex) = -2771,
        'Balances use the converted amount';

    -- Saving the same request again is a no-op, and an edit can go back to euros.
    perform public.save_account_expense(p_id => dinner, p_household_id => home, p_account_id => trip,
        p_title => 'Fish market', p_amount_minor => 5542, p_paid_by => you, p_category => 'Food',
        p_occurred_on => current_date, p_recurring => false,
        p_shares => jsonb_build_object(you::text, 2771, alex::text, 2771), p_version => 0,
        p_split_method => 'equal', p_split_values => '{}'::jsonb,
        p_original_currency => 'GBP', p_original_amount_minor => 4800, p_exchange_rate => 1.1546);
    assert (select version from public.expenses where id = dinner) = 1, 'A retried save changes nothing';
    perform public.save_account_expense(p_id => dinner, p_household_id => home, p_account_id => trip,
        p_title => 'Fish market', p_amount_minor => 5500, p_paid_by => you, p_category => 'Food',
        p_occurred_on => current_date, p_recurring => false,
        p_shares => jsonb_build_object(you::text, 2750, alex::text, 2750), p_version => 1,
        p_split_method => 'equal', p_split_values => '{}'::jsonb);
    assert (select original_currency from public.expenses where id = dinner) is null, 'An edit can drop the conversion';

    -- Older clients that never send the new arguments still save.
    perform public.save_account_expense(p_id => gen_random_uuid(), p_household_id => home, p_account_id => trip,
        p_title => 'Taxi', p_amount_minor => 1200, p_paid_by => you, p_category => 'Other',
        p_occurred_on => current_date, p_recurring => false, p_shares => jsonb_build_object(you::text, 1200),
        p_version => 0, p_split_method => 'equal', p_split_values => '{}'::jsonb);

    begin
        perform public.save_account_expense(p_id => gen_random_uuid(), p_household_id => home, p_account_id => trip,
            p_title => 'Wrong maths', p_amount_minor => 6000, p_paid_by => you, p_category => 'Food',
            p_occurred_on => current_date, p_recurring => false, p_shares => jsonb_build_object(you::text, 6000),
            p_version => 0, p_split_method => 'equal', p_split_values => '{}'::jsonb,
            p_original_currency => 'GBP', p_original_amount_minor => 4800, p_exchange_rate => 1.1546);
        raise exception 'A conversion that does not add up was saved';
    exception when raise_exception then if sqlerrm <> 'expenses.invalid_conversion' then raise; end if; end;
    begin
        perform public.save_account_expense(p_id => gen_random_uuid(), p_household_id => home, p_account_id => trip,
            p_title => 'Same currency', p_amount_minor => 1000, p_paid_by => you, p_category => 'Food',
            p_occurred_on => current_date, p_recurring => false, p_shares => jsonb_build_object(you::text, 1000),
            p_version => 0, p_split_method => 'equal', p_split_values => '{}'::jsonb,
            p_original_currency => 'EUR', p_original_amount_minor => 1000, p_exchange_rate => 1);
        raise exception 'The account currency was saved as a conversion';
    exception when raise_exception then if sqlerrm <> 'expenses.invalid_conversion' then raise; end if; end;
    begin
        perform public.save_account_expense(p_id => gen_random_uuid(), p_household_id => home, p_account_id => trip,
            p_title => 'Netflix US', p_amount_minor => 1154, p_paid_by => you, p_category => 'Bills',
            p_occurred_on => current_date, p_recurring => true, p_shares => jsonb_build_object(you::text, 1154),
            p_version => 0, p_split_method => 'equal', p_split_values => '{}'::jsonb,
            p_original_currency => 'USD', p_original_amount_minor => 1000, p_exchange_rate => 1.154);
        raise exception 'A recurring bill took another currency';
    exception when raise_exception then if sqlerrm <> 'expenses.recurring_single_currency' then raise; end if; end;
end;
$$;
rollback;
