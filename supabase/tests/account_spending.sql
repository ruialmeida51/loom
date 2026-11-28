begin;
insert into auth.users(id, email) values
    ('00000000-0000-4000-8000-000000000f01', 'spending-owner@example.test'),
    ('00000000-0000-4000-8000-000000000f02', 'spending-member@example.test'),
    ('00000000-0000-4000-8000-000000000f03', 'spending-outsider@example.test');
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000f01';
select set_config('loom.spending.home', public.create_household('Spending home', 'EUR', 'UTC')::text, true);
select set_config('loom.spending.invite', public.create_household_invite(current_setting('loom.spending.home')::uuid), true);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000f02';
select public.redeem_household_invitation(current_setting('loom.spending.invite'));
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000f01';
select public.save_expense_account(current_setting('loom.spending.home')::uuid,
    '00000000-0000-4000-8000-000000000f10', 'Porto trip', 'EUR',
    array['00000000-0000-4000-8000-000000000f01'::uuid, '00000000-0000-4000-8000-000000000f02'::uuid], false, 0);

do $$
declare
    home uuid := current_setting('loom.spending.home')::uuid;
    trip uuid := '00000000-0000-4000-8000-000000000f10';
    owner_id uuid := '00000000-0000-4000-8000-000000000f01';
    member_id uuid := '00000000-0000-4000-8000-000000000f02';
begin
    perform public.save_account_expense('00000000-0000-4000-8000-000000000f30', home, trip, 'Hostel', 12000, owner_id,
        'Other', current_date, false, jsonb_build_object(owner_id::text, 6000, member_id::text, 6000), 0, 'equal', '{}'::jsonb);
    perform public.save_account_expense('00000000-0000-4000-8000-000000000f31', home, trip, 'Dinner', 4000, owner_id,
        'Food', current_date, false, jsonb_build_object(owner_id::text, 2000, member_id::text, 2000), 0, 'equal', '{}'::jsonb);
    perform public.save_account_expense('00000000-0000-4000-8000-000000000f32', home, trip, 'Train', 3000, member_id,
        'Other', current_date, false, jsonb_build_object(owner_id::text, 1500, member_id::text, 1500), 0, 'equal', '{}'::jsonb);
    perform public.save_account_expense('00000000-0000-4000-8000-000000000f33', home, home, 'Groceries', 999, member_id,
        'Food', current_date, false, jsonb_build_object(owner_id::text, 999), 0, 'equal', '{}'::jsonb);

    assert (select paid_minor from public.account_spending where account_id = trip and user_id = owner_id) = 16000,
        'What the owner paid on the trip adds up';
    assert (select expense_count from public.account_spending where account_id = trip and user_id = owner_id) = 2;
    assert (select sum(paid_minor) from public.account_spending where account_id = trip) = 19000,
        'The trip total counts only its own expenses';

    perform public.delete_account_expense(home, trip, '00000000-0000-4000-8000-000000000f31',
        (select version from public.expenses where id = '00000000-0000-4000-8000-000000000f31'));
    assert (select paid_minor from public.account_spending where account_id = trip and user_id = owner_id) = 12000,
        'Deleted expenses no longer count';

    perform set_config('request.jwt.claim.sub', '00000000-0000-4000-8000-000000000f03', true);
    assert not exists(select 1 from public.account_spending where household_id = home),
        'Outsiders see nothing';
end $$;
rollback;
