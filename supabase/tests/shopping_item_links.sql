begin;
insert into auth.users(id,email) values ('00000000-0000-4000-8000-000000009201','links-owner@example.test');
set role authenticated;
set request.jwt.claim.sub='00000000-0000-4000-8000-000000009201';
select set_config('loom.test.links_home',public.create_household('Links','EUR','Europe/Lisbon')::text,true);
do $$declare list uuid; saved public.shopping_items; long_name text; begin
    select id into list from public.shopping_lists where household_id=current_setting('loom.test.links_home')::uuid limit 1;
    long_name:='Desk lamp https://www.example.test/pt/products/'||repeat('a',300);
    saved:=public.save_shopping_item(current_setting('loom.test.links_home')::uuid,'00000000-0000-4000-8000-000000009211',list,long_name,'','Pantry',false,false,false,0);
    assert saved.name=long_name,'A long product link is kept whole';
    begin
        perform public.save_shopping_item(current_setting('loom.test.links_home')::uuid,'00000000-0000-4000-8000-000000009212',list,repeat('b',501),'','Pantry',false,false,false,0);
        raise exception 'an over-long name was accepted';
    exception when raise_exception then
        if sqlerrm<>'shopping.message_check_the_item_name_quantity_and_category' then raise; end if;
    end;
end;$$;
rollback;
