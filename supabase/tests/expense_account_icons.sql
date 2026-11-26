begin;
insert into auth.users(id, email) values
    ('00000000-0000-4000-8000-000000000a71', 'icons-owner@example.test'),
    ('00000000-0000-4000-8000-000000000a72', 'icons-outsider@example.test');
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000a71';
select set_config('loom.icons.home', public.create_household('Icon home', 'EUR', 'UTC')::text, true);
select public.save_expense_account(current_setting('loom.icons.home')::uuid,
    '00000000-0000-4000-8000-000000000a80', 'Porto', 'EUR',
    array['00000000-0000-4000-8000-000000000a71'::uuid], false, 0);
do $$
declare home uuid := current_setting('loom.icons.home')::uuid; trip uuid := '00000000-0000-4000-8000-000000000a80';
begin
    assert (select icon from public.expense_accounts_with_members where id = trip) is null, 'Accounts start without an icon';
    perform public.set_expense_account_icon(home, trip, 'gift');
    assert (select icon from public.expense_accounts_with_members where id = trip) = 'gift', 'A member sets the icon';
    perform public.set_expense_account_icon(home, home, 'nature');
    assert (select icon from public.expense_accounts where id = home) = 'nature', 'Everyday takes an icon too';
    perform public.set_expense_account_icon(home, trip, null);
    assert (select icon from public.expense_accounts where id = trip) is null, 'Clearing restores the default look';
    begin
        perform public.set_expense_account_icon(home, trip, 'rocket');
        raise exception 'An unknown icon was saved';
    exception when raise_exception then if sqlerrm <> 'expenses.invalid_account_icon' then raise; end if; end;
end;
$$;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000a72';
do $$
begin
    perform public.set_expense_account_icon(current_setting('loom.icons.home')::uuid,
        '00000000-0000-4000-8000-000000000a80', 'pets');
    raise exception 'An outsider changed an icon';
exception when raise_exception then if sqlerrm <> 'expenses.account_access_denied' then raise; end if;
end;
$$;
rollback;
