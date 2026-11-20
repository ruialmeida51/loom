begin;
insert into auth.users(id,email) values ('00000000-0000-4000-8000-000000000b01','icon-owner@example.test');
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000b01';
select set_config('loom.icon.home',public.create_household('Icon home','EUR','UTC')::text,true);
do $$
declare
 home uuid := current_setting('loom.icon.home')::uuid;
 list public.shopping_lists;
 created public.shopping_lists;
begin
 select * into list from public.shopping_lists where household_id=home;
 assert list.icon='bag','Existing and default lists use the bag icon';
 -- An older client call without p_icon renames and keeps the icon.
 list := public.save_shopping_list(p_household_id=>home,p_id=>list.id,p_name=>'Groceries',p_archived=>false,p_version=>list.version);
 assert list.icon='bag','Omitting the icon keeps it';
 list := public.save_shopping_list(p_household_id=>home,p_id=>list.id,p_name=>list.name,p_archived=>false,p_version=>list.version,p_icon=>'produce');
 assert list.icon='produce','The icon changes';
 assert (public.save_shopping_list(p_household_id=>home,p_id=>list.id,p_name=>list.name,p_archived=>false,p_version=>list.version,p_icon=>'produce')).version=list.version,
  'Saving the same icon is a no-op';
 created := public.save_shopping_list(p_household_id=>home,p_id=>'00000000-0000-4000-8000-000000000b10',p_name=>'Holiday',p_archived=>false,p_version=>0,p_icon=>'travel');
 assert created.icon='travel','A new list stores its icon';
 begin
  perform public.save_shopping_list(p_household_id=>home,p_id=>created.id,p_name=>'Holiday',p_archived=>false,p_version=>created.version,p_icon=>'rocket');
  raise exception 'Unknown icon accepted';
 exception when raise_exception then if sqlerrm <> 'shopping.message_choose_a_list_icon' then raise; end if; end;
end;$$;
reset role;
rollback;
