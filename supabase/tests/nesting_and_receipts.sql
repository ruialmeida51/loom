begin;
insert into auth.users(id,email) values
('00000000-0000-4000-8000-000000000901','receipt-owner@example.test'),
('00000000-0000-4000-8000-000000000902','receipt-member@example.test'),
('00000000-0000-4000-8000-000000000903','receipt-outsider@example.test');
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000901';
select set_config('loom.new.home',public.create_household('Grouped home','EUR','UTC')::text,true);
select set_config('loom.new.list',(select id::text from public.shopping_lists where household_id=current_setting('loom.new.home')::uuid),true);
select set_config('loom.new.code',public.create_short_household_invitation(current_setting('loom.new.home')::uuid),true);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000902';
select public.redeem_household_invitation(current_setting('loom.new.code'));
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000901';
select public.add_shopping_items(current_setting('loom.new.home')::uuid,jsonb_build_array(
 jsonb_build_object('id','00000000-0000-4000-8000-000000000910','list_id',current_setting('loom.new.list'),'name','Produce'),
 jsonb_build_object('id','00000000-0000-4000-8000-000000000911','list_id',current_setting('loom.new.list'),'name','Apples'),
 jsonb_build_object('id','00000000-0000-4000-8000-000000000912','list_id',current_setting('loom.new.list'),'name','Bananas')));
select public.save_shopping_group(current_setting('loom.new.home')::uuid,
 (select jsonb_agg(to_jsonb(i)||jsonb_build_object('parent_id',case when right(id::text,3) <> '910' then '00000000-0000-4000-8000-000000000910' end))
 from public.shopping_items i where household_id=current_setting('loom.new.home')::uuid));
set constraints shopping_parent_scope immediate;
set constraints shopping_parent_scope deferred;
do $$ declare payload jsonb; begin
 assert (select count(*) from public.shopping_items where parent_id='00000000-0000-4000-8000-000000000910')=2,'Nested children persist';
 select jsonb_agg(to_jsonb(i)||jsonb_build_object('checked',true)) into payload from public.shopping_items i where household_id=current_setting('loom.new.home')::uuid;
 perform public.save_shopping_group(current_setting('loom.new.home')::uuid,payload);
 assert (select bool_and(checked) from public.shopping_items where household_id=current_setting('loom.new.home')::uuid),'Parent completion checks its children';
 -- A stale sibling makes the entire write fail, including the parent.
 select jsonb_agg(to_jsonb(i)||jsonb_build_object('checked',false,'version',version-case when right(id::text,3)='912' then 1 else 0 end)) into payload from public.shopping_items i where household_id=current_setting('loom.new.home')::uuid;
 begin perform public.save_shopping_group(current_setting('loom.new.home')::uuid,payload); raise exception 'Stale group accepted';
 exception when raise_exception then if sqlerrm <> 'shopping.message_this_item_changed_refresh_before_undoing_or_editing_it' then raise;end if;end;
 assert (select bool_and(checked) from public.shopping_items where household_id=current_setting('loom.new.home')::uuid),'Rejected group writes change no rows';
 -- Old clients also uncheck the parent when one child is unchecked.
 update public.shopping_items set checked=false where id='00000000-0000-4000-8000-000000000911';
 assert not(select checked from public.shopping_items where id='00000000-0000-4000-8000-000000000910');
 update public.shopping_items set removed=true where id='00000000-0000-4000-8000-000000000910';
 assert (select count(*) from public.shopping_items where id in ('00000000-0000-4000-8000-000000000911','00000000-0000-4000-8000-000000000912') and not removed and parent_id is null)=2,'Deleting a parent promotes children';
 select jsonb_agg(to_jsonb(i)||jsonb_build_object('removed',false,'parent_id',case when right(id::text,3)<>'910' then '00000000-0000-4000-8000-000000000910' end)) into payload from public.shopping_items i where household_id=current_setting('loom.new.home')::uuid;
 perform public.save_shopping_group(current_setting('loom.new.home')::uuid,payload);
 assert (select count(*) from public.shopping_items where parent_id='00000000-0000-4000-8000-000000000910')=2,'Restoring a parent retains its group';
 begin
  update public.shopping_items set parent_id='00000000-0000-4000-8000-000000000911' where id='00000000-0000-4000-8000-000000000912';
  set constraints shopping_parent_scope immediate;
  raise exception 'Second nesting level accepted';
 exception when raise_exception then if sqlerrm <> 'shopping.invalid_group' then raise;end if;end;
end;$$;
set constraints shopping_parent_scope deferred;
do $$ declare payload jsonb; affected integer; begin
 select jsonb_agg(to_jsonb(i)||jsonb_build_object('checked',true)) into payload from public.shopping_items i where household_id=current_setting('loom.new.home')::uuid;
 perform public.save_shopping_group(current_setting('loom.new.home')::uuid,payload);
 select count(*) into affected from public.remove_completed_shopping_group(current_setting('loom.new.home')::uuid,current_setting('loom.new.list')::uuid,
  (select jsonb_build_object(id::text,version) from public.shopping_items where id='00000000-0000-4000-8000-000000000910'));
 assert affected=3,'Bulk deletion returns promoted children with their new versions';
 assert not exists(select 1 from public.shopping_items where parent_id is not null),'Deleting only the completed parent promotes surviving children';
 assert (select count(*) from public.shopping_items where not removed)=2,'Bulk selection deletes only the selected item';
end;$$;
select public.create_expense('00000000-0000-4000-8000-000000000919',current_setting('loom.new.home')::uuid,
 'Household groceries',3000,'00000000-0000-4000-8000-000000000901','Groceries',current_date,false,
 '{"00000000-0000-4000-8000-000000000901":3000}');
select set_config('loom.new.photo1',current_setting('loom.new.home')||'/'||current_setting('loom.new.home')||'/00000000-0000-4000-8000-000000000919/00000000-0000-4000-8000-000000000921.jpg',true);
select set_config('loom.new.photo2',current_setting('loom.new.home')||'/'||current_setting('loom.new.home')||'/00000000-0000-4000-8000-000000000919/00000000-0000-4000-8000-000000000922.jpg',true);
insert into storage.objects(bucket_id,name,owner_id) values('expense-receipts',current_setting('loom.new.photo1'),auth.uid()::text);
select public.save_expense_receipt(current_setting('loom.new.home')::uuid,current_setting('loom.new.home')::uuid,'00000000-0000-4000-8000-000000000919',current_setting('loom.new.photo1'),0);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000902';
do $$ begin
 assert (select count(*) from public.expense_receipts)=1,'Household members can read an attached receipt';
 assert (select count(*) from storage.objects where bucket_id='expense-receipts')=1,'Authenticated household photo read';
 begin perform public.save_expense_receipt(current_setting('loom.new.home')::uuid,current_setting('loom.new.home')::uuid,'00000000-0000-4000-8000-000000000919',null,1); raise exception 'Non-manager removed receipt';
 exception when raise_exception then if sqlerrm <> 'expenses.account_access_denied' then raise;end if;end;
end;$$;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000903';
do $$ begin
 assert (select count(*) from public.expense_receipts)=0,'Outsiders cannot read receipt metadata';
 assert (select count(*) from storage.objects where bucket_id='expense-receipts')=0,'Outsiders cannot read objects';
 begin insert into storage.objects(bucket_id,name,owner_id) values('expense-receipts',current_setting('loom.new.photo2'),auth.uid()::text);raise exception 'Outsider uploaded';
 exception when insufficient_privilege then null;end;
end;$$;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000901';
select public.save_expense_receipt(current_setting('loom.new.home')::uuid,current_setting('loom.new.home')::uuid,'00000000-0000-4000-8000-000000000919',null,1);
do $$ begin
 begin perform public.save_expense_receipt(current_setting('loom.new.home')::uuid,current_setting('loom.new.home')::uuid,'00000000-0000-4000-8000-000000000919',current_setting('loom.new.photo1'),2); raise exception 'Queued object was reattached';
 exception when raise_exception then if sqlerrm <> 'expenses.receipt_changed' then raise;end if;end;
end;$$;
insert into storage.objects(bucket_id,name,owner_id) values('expense-receipts',current_setting('loom.new.photo2'),auth.uid()::text);
select public.save_expense_receipt(current_setting('loom.new.home')::uuid,current_setting('loom.new.home')::uuid,'00000000-0000-4000-8000-000000000919',current_setting('loom.new.photo2'),2);
do $$ begin
 assert (select version from public.expense_receipts)=3,'Removal keeps a monotonic version';
 begin perform public.save_expense_receipt(current_setting('loom.new.home')::uuid,current_setting('loom.new.home')::uuid,'00000000-0000-4000-8000-000000000919',null,1); raise exception 'Stale delete removed replacement';
 exception when raise_exception then if sqlerrm <> 'expenses.receipt_changed' then raise;end if;end;
 assert (select object_path from public.expense_receipts)=current_setting('loom.new.photo2');
 assert not has_function_privilege('authenticated','public.claim_receipt_deletions(integer)','execute'),'Only server workers claim file cleanup';
end;$$;
select public.delete_account_expense(current_setting('loom.new.home')::uuid,current_setting('loom.new.home')::uuid,'00000000-0000-4000-8000-000000000919',1);
do $$ begin
 assert (select count(*) from public.expense_receipts)=0,'Voided expenses unlink receipts';
 assert (select count(*) from storage.objects where bucket_id='expense-receipts')=0,'Voided photos stop being readable immediately';
end;$$;
reset role;
do $$ begin
 assert (select count(*) from loom_private.receipt_deletions)=2,'Removed/replaced and voided photos are queued';
 assert (select count(*) from public.claim_receipt_deletions(20))=2,'Worker claims physical cleanup';
end;$$;
rollback;
