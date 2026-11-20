begin;
insert into auth.users(id,email) values
('00000000-0000-4000-8000-000000000a01','keep-owner@example.test'),
('00000000-0000-4000-8000-000000000a03','keep-outsider@example.test');
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000a01';
select set_config('loom.keep.home',public.create_household('Keep home','EUR','UTC')::text,true);
select set_config('loom.keep.list',(select id::text from public.shopping_lists where household_id=current_setting('loom.keep.home')::uuid),true);
select public.add_shopping_items(current_setting('loom.keep.home')::uuid,jsonb_build_array(
 jsonb_build_object('id','00000000-0000-4000-8000-000000000a10','list_id',current_setting('loom.keep.list'),'name','Apples'),
 jsonb_build_object('id','00000000-0000-4000-8000-000000000a11','list_id',current_setting('loom.keep.list'),'name','Bread'),
 jsonb_build_object('id','00000000-0000-4000-8000-000000000a12','list_id',current_setting('loom.keep.list'),'name','Cheese')));
do $$
declare
 home uuid := current_setting('loom.keep.home')::uuid;
 list text := current_setting('loom.keep.list');
 returned integer;
 ordered text;
 versions jsonb;
begin
 select jsonb_object_agg(id,version) into versions from public.shopping_items where household_id=home;
 select count(*) into returned from public.insert_shopping_item_after(home,
  jsonb_build_object('id','00000000-0000-4000-8000-000000000a20','list_id',list,'name','Avocados'),
  '00000000-0000-4000-8000-000000000a10');
 assert returned=3,'The new row and both shifted rows are returned';
 select string_agg(name,',' order by position) into ordered from public.shopping_items where household_id=home;
 assert ordered='Apples,Avocados,Bread,Cheese','A new line is placed directly below its anchor: '||ordered;
 assert (select bool_and(version=(versions->>id::text)::bigint) from public.shopping_items
  where household_id=home and id<>'00000000-0000-4000-8000-000000000a20'),'Shifting keeps every existing version';
 select count(*) into returned from public.insert_shopping_item_after(home,
  jsonb_build_object('id','00000000-0000-4000-8000-000000000a20','list_id',list,'name','Avocados'),
  '00000000-0000-4000-8000-000000000a10');
 assert returned=1,'A repeated request returns only the stored row';
 assert (select count(*) from public.shopping_items where household_id=home)=4,'A repeated request inserts nothing';
 perform public.insert_shopping_item_after(home,
  jsonb_build_object('id','00000000-0000-4000-8000-000000000a21','list_id',list,'name','Dates'),
  '00000000-0000-4000-8000-0000000009ff');
 select string_agg(name,',' order by position) into ordered from public.shopping_items where household_id=home;
 assert ordered='Apples,Avocados,Bread,Cheese,Dates','A missing anchor appends: '||ordered;
 perform public.add_shopping_items(home,jsonb_build_array(
  jsonb_build_object('id','00000000-0000-4000-8000-000000000a22','list_id',list,'name','Eggs')));
 assert (select name from public.shopping_items where household_id=home order by position desc limit 1)='Eggs',
  'Later appends stay after shifted rows';
 -- Bread becomes a group parent with Cheese as its child.
 perform public.save_shopping_group(home,(select jsonb_agg(to_jsonb(i)||jsonb_build_object('parent_id',
  case when id='00000000-0000-4000-8000-000000000a12' then '00000000-0000-4000-8000-000000000a11' end))
  from public.shopping_items i where id in ('00000000-0000-4000-8000-000000000a11','00000000-0000-4000-8000-000000000a12')));
 perform public.insert_shopping_item_after(home,
  jsonb_build_object('id','00000000-0000-4000-8000-000000000a23','list_id',list,'name','Brioche'),
  '00000000-0000-4000-8000-000000000a11');
 assert (select parent_id from public.shopping_items where id='00000000-0000-4000-8000-000000000a23')=
  '00000000-0000-4000-8000-000000000a11','A line below a group parent joins its group';
 perform public.insert_shopping_item_after(home,
  jsonb_build_object('id','00000000-0000-4000-8000-000000000a24','list_id',list,'name','Cheddar'),
  '00000000-0000-4000-8000-000000000a12');
 assert (select parent_id from public.shopping_items where id='00000000-0000-4000-8000-000000000a24')=
  '00000000-0000-4000-8000-000000000a11','A line below a child joins the same group';
 select string_agg(name,',' order by position) into ordered from public.shopping_items where household_id=home;
 assert ordered='Apples,Avocados,Bread,Brioche,Cheese,Cheddar,Dates,Eggs','Grouped insertion keeps order: '||ordered;
 begin
  perform set_config('loom.shopping_reposition','on',true);
  update public.shopping_items set name='Renamed' where id='00000000-0000-4000-8000-000000000a10';
  raise exception 'Reposition flag allowed a content change';
 exception when raise_exception then if sqlerrm <> 'shopping.invalid_reposition' then raise; end if; end;
 perform set_config('loom.shopping_reposition','off',true);
end;$$;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000a03';
do $$ begin
 perform public.insert_shopping_item_after(current_setting('loom.keep.home')::uuid,
  jsonb_build_object('id','00000000-0000-4000-8000-000000000a30','list_id',current_setting('loom.keep.list'),'name','Intruder'),
  '00000000-0000-4000-8000-000000000a10');
 raise exception 'Outsider inserted a shopping item';
exception when raise_exception then if sqlerrm <> 'shopping.message_household_access_denied' then raise; end if;
end;$$;
reset role;
rollback;
