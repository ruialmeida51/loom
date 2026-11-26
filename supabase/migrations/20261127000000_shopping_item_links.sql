-- Shopping items can hold a link (a product page, a recipe), and shop addresses are often longer
-- than the old 120 characters. The app shows links by their site, so long names stay tidy.
begin;

alter table public.shopping_items drop constraint shopping_items_name_check;
alter table public.shopping_items add constraint shopping_items_name_check
    check (length(trim(name)) between 1 and 500);

-- Unchanged apart from the longer name. Every add, insert-below and grouping path saves through it.
create or replace function loom_private.save_shopping_item(p_household_id uuid,p_id uuid,p_list_id uuid,p_name text,p_quantity text,p_category text,p_checked boolean,p_favorite boolean,p_removed boolean,p_version bigint) returns public.shopping_items
language plpgsql security definer set search_path='' as $$
declare row public.shopping_items;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'shopping.message_household_access_denied'; end if;
    if p_name is null or length(trim(p_name)) not between 1 and 500 or p_quantity is null or length(p_quantity)>40 or p_category is null or length(p_category) not between 1 and 40 or p_version is null or p_checked is null or p_favorite is null or p_removed is null then raise exception 'shopping.message_check_the_item_name_quantity_and_category'; end if;
    perform 1 from public.households where id=p_household_id for update;
    if not exists(select 1 from public.shopping_lists where id=p_list_id and household_id=p_household_id and not archived) then raise exception 'shopping.message_choose_an_active_shopping_list_in_this_household'; end if;
    select * into row from public.shopping_items where id=p_id for update;
    if found then
        if row.household_id<>p_household_id then raise exception 'shopping.message_household_access_denied'; end if;
        if (row.list_id,row.name,row.quantity,row.category,row.checked,row.favorite,row.removed) is not distinct from (p_list_id,trim(p_name),trim(p_quantity),p_category,p_checked,p_favorite,p_removed) then return row; end if;
        if row.version<>p_version then raise exception 'shopping.message_this_item_changed_refresh_before_undoing_or_editing_it'; end if;
        update public.shopping_items set list_id=p_list_id,name=trim(p_name),quantity=trim(p_quantity),category=p_category,checked=p_checked,favorite=p_favorite,removed=p_removed where id=p_id returning * into row;
    else
        if p_version<>0 then raise exception 'shopping.message_this_shopping_item_no_longer_exists'; end if;
        insert into public.shopping_items(id,household_id,list_id,name,quantity,category,checked,favorite,removed,created_by)
        values(p_id,p_household_id,p_list_id,trim(p_name),trim(p_quantity),p_category,p_checked,p_favorite,p_removed,auth.uid()) returning * into row;
    end if;
    return row;
end;
$$;

commit;
