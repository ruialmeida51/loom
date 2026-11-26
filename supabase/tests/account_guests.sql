begin;
insert into auth.users(id, email) values
    ('00000000-0000-4000-8000-000000000d01', 'guests-owner@example.test'),
    ('00000000-0000-4000-8000-000000000d02', 'guests-member@example.test'),
    ('00000000-0000-4000-8000-000000000d03', 'guests-outsider@example.test');
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000d01';
select set_config('loom.guests.home', public.create_household('Guest home', 'EUR', 'UTC')::text, true);
select set_config('loom.guests.invite', public.create_household_invite(current_setting('loom.guests.home')::uuid), true);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000d02';
select public.redeem_household_invitation(current_setting('loom.guests.invite'));
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000d01';
select public.save_expense_account(current_setting('loom.guests.home')::uuid,
    '00000000-0000-4000-8000-000000000d10', 'Lisbon weekend', 'EUR',
    array['00000000-0000-4000-8000-000000000d01'::uuid, '00000000-0000-4000-8000-000000000d02'::uuid], false, 0);

do $$
declare
    home uuid := current_setting('loom.guests.home')::uuid;
    trip uuid := '00000000-0000-4000-8000-000000000d10';
    owner_id uuid := '00000000-0000-4000-8000-000000000d01';
    marta uuid := '00000000-0000-4000-8000-000000000d20';
    guest public.expense_account_guests;
begin
    guest := public.save_account_guest(home, trip, marta, '  Marta ', 0);
    assert guest.name = 'Marta' and guest.version = 1, 'A guest is created with a trimmed name';
    assert (public.save_account_guest(home, trip, marta, 'Marta', 1)).version = 1, 'Saving the same name is a no-op';
    guest := public.save_account_guest(home, trip, marta, 'Marta S', 1);
    assert guest.version = 2, 'A guest can be renamed';
    assert (select guests->0->>'name' from public.expense_accounts_with_members where id = trip) = 'Marta S',
        'Accounts list their guests';

    begin
        perform public.save_account_guest(home, home, gen_random_uuid(), 'Not here', 0);
        raise exception 'Everyday accepted a guest';
    exception when raise_exception then if sqlerrm <> 'expenses.guests_not_on_everyday' then raise; end if; end;

    -- A guest pays and shares like a member.
    perform public.save_account_expense('00000000-0000-4000-8000-000000000d30', home, trip, 'Dinner', 9000, marta, 'Food',
        current_date, false, jsonb_build_object(owner_id::text, 3000, marta::text, 3000,
            '00000000-0000-4000-8000-000000000d02', 3000), 0, 'equal', '{}'::jsonb);
    assert (select sum(delta_minor) from public.account_ledger_entries where account_id = trip and user_id = marta) = 6000,
        'The guest is owed what others shared';

    begin
        perform public.save_account_expense(gen_random_uuid(), home, home, 'Wrong account', 1000, marta, 'Food',
            current_date, false, jsonb_build_object(owner_id::text, 1000), 0, 'amounts', '{}'::jsonb);
        raise exception 'A guest was used in another account';
    exception when raise_exception then if sqlerrm <> 'expenses.invalid_account_members' then raise; end if; end;

    begin
        perform public.remove_account_guest(home, trip, marta, 2);
        raise exception 'A guest with a balance was removed';
    exception when raise_exception then if sqlerrm <> 'expenses.guest_has_balance' then raise; end if; end;

    -- Any account member records the guest's repayments, in either direction.
    perform public.record_account_settlement('00000000-0000-4000-8000-000000000d40', home, trip, owner_id, marta, 3000, current_date);
    perform set_config('request.jwt.claim.sub', '00000000-0000-4000-8000-000000000d02', true);
    perform public.record_account_settlement('00000000-0000-4000-8000-000000000d41', home, trip,
        '00000000-0000-4000-8000-000000000d02', marta, 3000, current_date);
    assert coalesce((select sum(delta_minor) from public.account_ledger_entries where account_id = trip and user_id = marta), 0) = 0,
        'Repayments settle the guest';

    -- A recurring bill can include the guest; removing them pauses it.
    perform public.save_account_expense('00000000-0000-4000-8000-000000000d31', home, trip, 'Streaming', 1200, owner_id, 'Bills',
        current_date, true, jsonb_build_object(owner_id::text, 600, marta::text, 600), 0, 'equal', '{}'::jsonb);
    perform public.record_account_settlement('00000000-0000-4000-8000-000000000d42', home, trip, marta, owner_id, 600, current_date);
    assert (select count(*) from public.recurring_expenses where account_id = trip and active) = 1;
    perform public.remove_account_guest(home, trip, marta, 2);
    assert (select removed_at is not null from public.expense_account_guests where id = marta), 'A settled guest is removed';
    assert (select count(*) from public.recurring_expenses where account_id = trip and active) = 0,
        'Recurring bills with a removed guest pause';
    assert (select count(*) from public.account_expenses_with_shares where account_id = trip and paid_by = marta) = 1,
        'The removed guest stays on their past records';
    begin
        perform public.save_account_expense(gen_random_uuid(), home, trip, 'After leaving', 1000, owner_id, 'Food',
            current_date, false, jsonb_build_object(marta::text, 1000), 0, 'amounts', '{}'::jsonb);
        raise exception 'A removed guest joined a new expense';
    exception when raise_exception then if sqlerrm <> 'expenses.invalid_account_members' then raise; end if; end;

    -- Outsiders cannot manage guests.
    perform set_config('request.jwt.claim.sub', '00000000-0000-4000-8000-000000000d03', true);
    begin
        perform public.save_account_guest(home, trip, gen_random_uuid(), 'Sneaky', 0);
        raise exception 'An outsider added a guest';
    exception when raise_exception then if sqlerrm <> 'expenses.account_access_denied' then raise; end if; end;
    perform set_config('request.jwt.claim.sub', owner_id::text, true);

    -- With the feature switched off, existing guests keep working but new ones are refused.
    perform set_config('role', 'postgres', true);
    insert into public.household_features(household_id, feature, enabled) values (home, 'account_guests', false);
    perform set_config('role', 'authenticated', true);
    begin
        perform public.save_account_guest(home, trip, gen_random_uuid(), 'Later', 0);
        raise exception 'A switched-off feature added a guest';
    exception when raise_exception then if sqlerrm <> 'features.unavailable' then raise; end if; end;

    -- The participant guard replaces the old foreign keys for direct writes too.
    perform set_config('role', 'postgres', true);
    begin
        insert into public.settlements(id, household_id, account_id, from_user, to_user, amount_minor, occurred_on, created_by)
        values (gen_random_uuid(), home, trip, owner_id, gen_random_uuid(), 100, current_date, owner_id);
        set constraints all immediate;
        raise exception 'An unknown participant was stored';
    exception when raise_exception then if sqlerrm <> 'expenses.invalid_account_members' then raise; end if; end;
end; $$;
reset role;
rollback;
