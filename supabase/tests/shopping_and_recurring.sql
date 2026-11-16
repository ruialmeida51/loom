begin;
insert into auth.users(id,email) values
('00000000-0000-4000-8000-000000000011','owner2@example.test'),
('00000000-0000-4000-8000-000000000012','member2@example.test');
set role authenticated;
set request.jwt.claim.sub='00000000-0000-4000-8000-000000000011';
select set_config('loom.test.home',public.create_household('Shared flat','EUR','UTC')::text,true);
select set_config('loom.test.other',public.create_household('Other flat','EUR','UTC')::text,true);
select set_config('loom.test.groceries',(select id::text from public.shopping_lists where household_id=current_setting('loom.test.home')::uuid),true);
select set_config('loom.test.invite',public.create_household_invite(current_setting('loom.test.home')::uuid),true);
select public.save_shopping_list(current_setting('loom.test.home')::uuid,'00000000-0000-4000-8000-000000000020','Pharmacy',false,0);
select public.add_shopping_items(current_setting('loom.test.home')::uuid,jsonb_build_array(jsonb_build_object('id','00000000-0000-4000-8000-000000000030','list_id',current_setting('loom.test.groceries'),'name','Milk')));
select public.add_shopping_items(current_setting('loom.test.home')::uuid,jsonb_build_array(jsonb_build_object('id','00000000-0000-4000-8000-000000000030','list_id',current_setting('loom.test.groceries'),'name','Milk')));
do $$begin
    assert (select count(*) from public.shopping_items)=1,'Batch retries are idempotent';
    begin
        perform public.add_shopping_items(current_setting('loom.test.home')::uuid,jsonb_build_array(
            jsonb_build_object('id',gen_random_uuid(),'list_id',current_setting('loom.test.groceries'),'name','Valid'),
            jsonb_build_object('id',gen_random_uuid(),'list_id',current_setting('loom.test.groceries'),'name','')));
        raise exception 'Invalid batch accepted';
    exception when raise_exception then if sqlerrm not like '%shopping.message_check_the_item_name_quantity_and_category%' then raise; end if;end;
    assert (select count(*) from public.shopping_items)=1,'A failed batch rolls back every insertion';
end;$$;
select public.save_shopping_item(current_setting('loom.test.home')::uuid,'00000000-0000-4000-8000-000000000030','00000000-0000-4000-8000-000000000020','Milk','2 cartons','Pantry',false,true,false,1);
select public.save_shopping_item(current_setting('loom.test.home')::uuid,'00000000-0000-4000-8000-000000000030','00000000-0000-4000-8000-000000000020','Milk','2 cartons','Pantry',false,true,true,2);
do $$begin
    assert (select removed and version=3 from public.shopping_items where id='00000000-0000-4000-8000-000000000030');
    begin
        perform public.save_shopping_item(current_setting('loom.test.home')::uuid,'00000000-0000-4000-8000-000000000030','00000000-0000-4000-8000-000000000020','Milk','','Pantry',false,false,false,1);
        raise exception 'Stale undo accepted';
    exception when raise_exception then if sqlerrm not like '%shopping.message_this_item_changed_refresh_before_undoing_or_editing_it%' then raise;end if;end;
end;$$;
select public.save_shopping_item(current_setting('loom.test.home')::uuid,'00000000-0000-4000-8000-000000000030','00000000-0000-4000-8000-000000000020','Milk','2 cartons','Pantry',false,true,false,3);
select public.save_shopping_list(current_setting('loom.test.home')::uuid,current_setting('loom.test.groceries')::uuid,'Groceries',true,1);
do $$begin
    begin perform public.save_shopping_list(current_setting('loom.test.home')::uuid,'00000000-0000-4000-8000-000000000020','Pharmacy',true,1);raise exception 'Last list archived';
    exception when raise_exception then if sqlerrm not like '%shopping.message_keep_at_least_one_shopping_list_active%' then raise;end if;end;
    begin perform public.save_shopping_item(current_setting('loom.test.home')::uuid,'00000000-0000-4000-8000-000000000030',current_setting('loom.test.groceries')::uuid,'Milk','','Pantry',false,false,false,4);raise exception 'Archived list accepted';
    exception when raise_exception then if sqlerrm not like '%shopping.message_choose_an_active_shopping_list_in_this_household%' then raise;end if;end;
    begin perform public.save_shopping_item(current_setting('loom.test.home')::uuid,'00000000-0000-4000-8000-000000000030',(select id from public.shopping_lists where household_id=current_setting('loom.test.other')::uuid limit 1),'Milk','','Pantry',false,false,false,4);raise exception 'Cross-household move accepted';
    exception when raise_exception then if sqlerrm not like '%shopping.message_choose_an_active_shopping_list_in_this_household%' then raise;end if;end;
end;$$;
do $$begin
    begin
        perform public.create_expense(gen_random_uuid(),current_setting('loom.test.home')::uuid,'Future payment',100,auth.uid(),'Bills',current_date+1,false,jsonb_build_object(auth.uid()::text,100));
        raise exception 'Unpaid future expense accepted';
    exception when raise_exception then if sqlerrm <> 'expenses.message_payment_date_future' then raise;end if;end;
end;$$;
select public.create_expense('00000000-0000-4000-8000-000000000040',current_setting('loom.test.home')::uuid,'Rent',10000,auth.uid(),'Bills',current_date,true,jsonb_build_object(auth.uid()::text,10000));
select set_config('loom.test.rule',(select recurring_rule_id::text from public.expenses where id='00000000-0000-4000-8000-000000000040'),true);
select public.control_recurring_expense(current_setting('loom.test.home')::uuid,current_setting('loom.test.rule')::uuid,'pause',1);
set request.jwt.claim.sub='00000000-0000-4000-8000-000000000012';
select public.redeem_household_invitation(current_setting('loom.test.invite'));
do $$begin
    assert (select count(*) from public.shopping_lists)=2,'Other households lists are not visible';
    begin perform public.control_recurring_expense(current_setting('loom.test.home')::uuid,current_setting('loom.test.rule')::uuid,'resume',2);raise exception 'Non-manager changed schedule';
    exception when raise_exception then if sqlerrm not like '%expenses.message_only_the_payer_creator_or_household_owner_can_manage%' then raise;end if;end;
end;$$;
reset role;
update public.recurring_expenses set next_on=current_date-interval '3 months' where id=current_setting('loom.test.rule')::uuid;
select loom_private.queue_due_work();
do $$begin assert (select count(*) from public.expenses)=1,'Paused bills generate no entries';end;$$;
set role authenticated;
set request.jwt.claim.sub='00000000-0000-4000-8000-000000000011';
select public.control_recurring_expense(current_setting('loom.test.home')::uuid,current_setting('loom.test.rule')::uuid,'resume',(select version from public.recurring_expenses where id=current_setting('loom.test.rule')::uuid));
do $$begin assert (select next_on>current_date from public.recurring_expenses where id=current_setting('loom.test.rule')::uuid),'Resuming skips past paused bills';end;$$;
select public.control_recurring_expense(current_setting('loom.test.home')::uuid,current_setting('loom.test.rule')::uuid,'cancel',(select version from public.recurring_expenses where id=current_setting('loom.test.rule')::uuid));
do $$begin
    assert (select cancelled_at is not null and not active from public.recurring_expenses where id=current_setting('loom.test.rule')::uuid);
    assert (select count(*) from public.expenses)=1,'Cancelling preserves expense history';
    begin perform public.control_recurring_expense(current_setting('loom.test.home')::uuid,current_setting('loom.test.rule')::uuid,'resume',4);raise exception 'Cancelled rule resumed';
    exception when raise_exception then if sqlerrm not like '%expenses.message_a_cancelled_schedule_cannot_be_resumed%' then raise;end if;end;
end;$$;
rollback;
