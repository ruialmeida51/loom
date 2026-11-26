begin;
insert into auth.users(id, email) values
    ('00000000-0000-4000-8000-000000000e01', 'balances-owner@example.test'),
    ('00000000-0000-4000-8000-000000000e02', 'balances-member@example.test'),
    ('00000000-0000-4000-8000-000000000e03', 'balances-outsider@example.test');
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000e01';
select set_config('loom.balances.home', public.create_household('Balance home', 'EUR', 'UTC')::text, true);
select set_config('loom.balances.invite', public.create_household_invite(current_setting('loom.balances.home')::uuid), true);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000e02';
select public.redeem_household_invitation(current_setting('loom.balances.invite'));
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000e01';
select public.save_expense_account(current_setting('loom.balances.home')::uuid,
    '00000000-0000-4000-8000-000000000e10', 'Porto trip', 'EUR',
    array['00000000-0000-4000-8000-000000000e01'::uuid, '00000000-0000-4000-8000-000000000e02'::uuid], false, 0);

do $$
declare
    home uuid := current_setting('loom.balances.home')::uuid;
    trip uuid := '00000000-0000-4000-8000-000000000e10';
    owner_id uuid := '00000000-0000-4000-8000-000000000e01';
    member_id uuid := '00000000-0000-4000-8000-000000000e02';
    seen integer;
    cursor_at timestamptz;
    cursor_id uuid;
    page_ids uuid[];
begin
    -- Everyday: the owner pays 90.00 split evenly; the member pays 30.00 for the owner alone.
    perform public.save_account_expense('00000000-0000-4000-8000-000000000e30', home, home, 'Groceries', 9000, owner_id,
        'Food', current_date, false, jsonb_build_object(owner_id::text, 4500, member_id::text, 4500), 0, 'equal', '{}'::jsonb);
    perform public.save_account_expense('00000000-0000-4000-8000-000000000e31', home, home, 'Cinema', 3000, member_id,
        'Fun', current_date, false, jsonb_build_object(owner_id::text, 3000), 0, 'equal', '{}'::jsonb);
    assert (select balance_minor from public.account_balances
        where household_id = home and account_id = home and user_id = owner_id) = 1500, 'The owner is owed 15.00';
    assert (select balance_minor from public.account_balances
        where household_id = home and account_id = home and user_id = member_id) = -1500, 'The member owes 15.00';
    assert (select sum(balance_minor) from public.account_balances where account_id = home) = 0, 'Balances sum to zero';

    -- A repayment settles; deleting it and voiding an expense take them out of the balance again.
    perform public.record_account_settlement('00000000-0000-4000-8000-000000000e40', home, home, member_id, owner_id,
        1500, current_date);
    assert (select bool_and(balance_minor = 0) from public.account_balances where account_id = home),
        'A repayment settles everyone';
    perform public.delete_account_settlement(home, home, '00000000-0000-4000-8000-000000000e40',
        (select version from public.settlements where id = '00000000-0000-4000-8000-000000000e40'));
    perform public.delete_account_expense(home, home, '00000000-0000-4000-8000-000000000e31',
        (select version from public.expenses where id = '00000000-0000-4000-8000-000000000e31'));
    assert (select balance_minor from public.account_balances where account_id = home and user_id = owner_id) = 4500,
        'Deleted repayments and expenses no longer count';

    -- Other accounts keep their own balances.
    perform public.save_account_expense('00000000-0000-4000-8000-000000000e32', home, trip, 'Hostel', 2000, member_id,
        'Other', current_date, false, jsonb_build_object(owner_id::text, 1000, member_id::text, 1000), 0, 'equal', '{}'::jsonb);
    assert (select balance_minor from public.account_balances where account_id = trip and user_id = member_id) = 1000,
        'A trip has its own balance';
    assert (select balance_minor from public.account_balances where account_id = home and user_id = member_id) = -4500,
        'The trip does not change Everyday';

    -- Long histories: well over PostgREST's 1000-row response cap still add up on the server.
    perform set_config('role', 'postgres', true);
    insert into public.expenses(id, household_id, account_id, title, amount_minor, paid_by, category, occurred_on,
        created_by, created_at)
    select gen_random_uuid(), home, trip, 'Coffee ' || n, 300, owner_id, 'Food', current_date, owner_id,
        -- Pairs share a timestamp so the cursor's id tie-break is exercised.
        now() - make_interval(secs => n / 2)
    from generate_series(1, 1200) n;
    insert into public.expense_shares(expense_id, household_id, user_id, amount_minor)
    select e.id, home, member_id, 300 from public.expenses e where e.account_id = trip and e.title like 'Coffee %';
    insert into public.settlements(id, household_id, account_id, from_user, to_user, amount_minor, occurred_on,
        created_by, created_at)
    select gen_random_uuid(), home, trip, member_id, owner_id, 100, current_date, owner_id,
        now() - make_interval(secs => n)
    from generate_series(1, 300) n;
    perform set_config('role', 'authenticated', true);
    assert (select balance_minor from public.account_balances where account_id = trip and user_id = owner_id)
        = -1000 + 1200 * 300 - 300 * 100, 'Every one of 1500 records counts toward the balance';
    assert (select sum(balance_minor) from public.account_balances where account_id = trip) = 0,
        'A long history still sums to zero';

    -- The client's keyset (created_at desc, id) visits every expense and repayment exactly once.
    seen := 0;
    loop
        select array_agg(r.id order by r.created_at desc, r.id), (array_agg(r.created_at order by r.created_at desc, r.id))[50],
            (array_agg(r.id order by r.created_at desc, r.id))[50]
        into page_ids, cursor_at, cursor_id
        from (
            (select e.id, e.created_at from public.account_expenses_with_shares e
             where e.household_id = home and e.account_id = trip
               and (cursor_at is null or e.created_at < cursor_at or (e.created_at = cursor_at and e.id > cursor_id))
             order by e.created_at desc, e.id limit 51)
            union all
            (select s.id, s.created_at from public.settlements s
             where s.household_id = home and s.account_id = trip
               and (cursor_at is null or s.created_at < cursor_at or (s.created_at = cursor_at and s.id > cursor_id))
             order by s.created_at desc, s.id limit 51)
            order by 2 desc, 1 limit 50
        ) r;
        exit when page_ids is null;
        seen := seen + cardinality(page_ids);
        exit when cardinality(page_ids) < 50;
    end loop;
    assert seen = 1501, format('Paging saw %s of 1501 trip records', seen);

    -- Only household members read balances.
    perform set_config('request.jwt.claim.sub', '00000000-0000-4000-8000-000000000e03', true);
    assert not exists(select 1 from public.account_balances where household_id = home),
        'An outsider sees no balances';
    perform set_config('request.jwt.claim.sub', owner_id::text, true);
end; $$;

reset role;
set role anon;
do $$
begin
    perform 1 from public.account_balances;
    raise exception 'Anonymous callers read balances';
exception when insufficient_privilege then null;
end; $$;
reset role;
rollback;
