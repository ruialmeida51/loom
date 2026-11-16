begin;
insert into auth.users(id, email) values
    ('00000000-0000-4000-8000-000000000060', 'ux-owner@example.test'),
    ('00000000-0000-4000-8000-000000000061', 'ux-creator@example.test'),
    ('00000000-0000-4000-8000-000000000062', 'ux-member@example.test'),
    ('00000000-0000-4000-8000-000000000063', 'ux-outsider@example.test');
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000060';
select set_config('loom.ux.home', public.create_household('Usability home', 'EUR', 'UTC')::text, true);
select set_config('loom.ux.invite', public.create_household_invite(current_setting('loom.ux.home')::uuid), true);
select set_config('loom.ux.list', (select id::text from public.shopping_lists
    where household_id = current_setting('loom.ux.home')::uuid), true);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000061';
select public.redeem_household_invitation(current_setting('loom.ux.invite'));
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000060';
select set_config('loom.ux.invite', public.create_household_invite(current_setting('loom.ux.home')::uuid), true);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000062';
select public.redeem_household_invitation(current_setting('loom.ux.invite'));

-- Reversed UUID order must not change insertion order after a paginated refresh.
select public.add_shopping_items(current_setting('loom.ux.home')::uuid, jsonb_build_array(
    jsonb_build_object('id', '00000000-0000-4000-8000-000000000699', 'list_id', current_setting('loom.ux.list'), 'name', 'First'),
    jsonb_build_object('id', '00000000-0000-4000-8000-000000000601', 'list_id', current_setting('loom.ux.list'), 'name', 'Second')));
select set_config('loom.ux.first_position', (select position::text from public.shopping_items
    where id = '00000000-0000-4000-8000-000000000699'), true);
select public.add_shopping_items(current_setting('loom.ux.home')::uuid, jsonb_build_array(
    jsonb_build_object('id', '00000000-0000-4000-8000-000000000699', 'list_id', current_setting('loom.ux.list'), 'name', 'First')));
do $$ begin
    assert (select array_agg(name order by position) from public.shopping_items) = array['First', 'Second'];
    assert (select position from public.shopping_items where id = '00000000-0000-4000-8000-000000000699')
        = current_setting('loom.ux.first_position')::bigint, 'A retry does not move an existing item';
end; $$;
select public.save_shopping_item(current_setting('loom.ux.home')::uuid, '00000000-0000-4000-8000-000000000699',
    current_setting('loom.ux.list')::uuid, 'First', '', 'Other', true, false, false, 1);
select public.save_shopping_item(current_setting('loom.ux.home')::uuid, '00000000-0000-4000-8000-000000000699',
    current_setting('loom.ux.list')::uuid, 'First', '', 'Other', false, false, false, 2);
do $$ begin
    assert (select array_agg(name order by position) from public.shopping_items) = array['Second', 'First'],
        'Restoring a completed item appends it at the bottom';
    begin
        perform public.delete_shopping_list(current_setting('loom.ux.home')::uuid, current_setting('loom.ux.list')::uuid, 0);
        raise exception 'Accepted stale list version';
    exception when raise_exception then
        if sqlerrm <> 'shopping.message_this_list_changed_refresh_and_try_again' then raise; end if;
    end;
end; $$;
-- A member who did not create the list may delete it, including when it is the last list.
select public.delete_shopping_list(current_setting('loom.ux.home')::uuid, current_setting('loom.ux.list')::uuid, 1);
select public.delete_shopping_list(current_setting('loom.ux.home')::uuid, current_setting('loom.ux.list')::uuid, 1);
do $$ begin
    assert not exists(select 1 from public.shopping_lists), 'Deleting the final list is supported';
    assert not exists(select 1 from public.shopping_items), 'Contained items are removed together';
end; $$;
select public.save_shopping_list(current_setting('loom.ux.home')::uuid, '00000000-0000-4000-8000-000000000602', 'Fresh start', false, 0);

-- A household member creates an account with a recurring expense, repayment and saved statement.
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000061';
select public.save_expense_account(current_setting('loom.ux.home')::uuid, '00000000-0000-4000-8000-000000000640',
    'Finished trip', 'GBP', array['00000000-0000-4000-8000-000000000061'::uuid, '00000000-0000-4000-8000-000000000062'::uuid], false, 0);
select public.save_account_expense('00000000-0000-4000-8000-000000000641', current_setting('loom.ux.home')::uuid,
    '00000000-0000-4000-8000-000000000640', 'Rental', 1000, auth.uid(), 'Other', current_date, true,
    '{"00000000-0000-4000-8000-000000000061":500,"00000000-0000-4000-8000-000000000062":500}', 0, 'equal', '{}');
select public.record_account_settlement('00000000-0000-4000-8000-000000000642', current_setting('loom.ux.home')::uuid,
    '00000000-0000-4000-8000-000000000640', '00000000-0000-4000-8000-000000000062', auth.uid(), 500, current_date);
select public.save_account_statement(current_setting('loom.ux.home')::uuid, '00000000-0000-4000-8000-000000000640', date_trunc('month', current_date)::date);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000062';
do $$ begin
    begin
        perform public.delete_expense_account(current_setting('loom.ux.home')::uuid, '00000000-0000-4000-8000-000000000640', 1);
        raise exception 'A regular member deleted another creator account';
    exception when raise_exception then if sqlerrm <> 'expenses.account_access_denied' then raise; end if; end;
end; $$;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000063';
do $$ begin
    begin
        perform public.delete_shopping_list(current_setting('loom.ux.home')::uuid, '00000000-0000-4000-8000-000000000602', 1);
        raise exception 'An outsider deleted a shopping list';
    exception when raise_exception then if sqlerrm <> 'shopping.message_household_access_denied' then raise; end if; end;
    begin
        perform public.delete_expense_account(current_setting('loom.ux.home')::uuid, '00000000-0000-4000-8000-000000000640', 1);
        raise exception 'An outsider deleted an account';
    exception when raise_exception then if sqlerrm <> 'expenses.account_access_denied' then raise; end if; end;
end; $$;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000060';
do $$ begin
    begin
        perform public.delete_expense_account(current_setting('loom.ux.home')::uuid, '00000000-0000-4000-8000-000000000640', 0);
        raise exception 'Accepted stale account version';
    exception when raise_exception then if sqlerrm <> 'expenses.record_changed' then raise; end if; end;
    begin
        perform public.delete_expense_account(current_setting('loom.ux.home')::uuid, current_setting('loom.ux.home')::uuid, 1);
        raise exception 'Deleted the built-in Everyday account';
    exception when raise_exception then if sqlerrm <> 'expenses.account_access_denied' then raise; end if; end;
end; $$;
select public.delete_expense_account(current_setting('loom.ux.home')::uuid, '00000000-0000-4000-8000-000000000640', 1);
select public.delete_expense_account(current_setting('loom.ux.home')::uuid, '00000000-0000-4000-8000-000000000640', 1);
do $$ begin
    assert (select count(*) from public.expense_accounts) = 1, 'The household owner can delete another creator trip';
    assert not exists(select 1 from public.expenses);
    assert not exists(select 1 from public.expense_shares);
    assert not exists(select 1 from public.settlements);
    assert not exists(select 1 from public.recurring_expenses);
    assert not exists(select 1 from public.monthly_statements);
    assert not has_function_privilege('anon', 'public.delete_expense_account(uuid,uuid,bigint)', 'execute');
    assert not has_function_privilege('anon', 'public.delete_shopping_list(uuid,uuid,bigint)', 'execute');
    assert not has_table_privilege('authenticated', 'public.expense_accounts', 'delete');
end; $$;
rollback;
