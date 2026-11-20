begin;

-- Each shopping list can carry one of a small set of icons. Older clients omit p_icon, which the
-- named-argument call still matches, and the list keeps its current icon.
alter table public.shopping_lists add column icon text not null default 'bag'
    check (icon in ('bag', 'cart', 'produce', 'home', 'gift', 'travel', 'pets', 'health'));

drop function public.save_shopping_list(uuid, uuid, text, boolean, bigint);
drop function loom_private.save_shopping_list(uuid, uuid, text, boolean, bigint);

create function loom_private.save_shopping_list(p_household_id uuid, p_id uuid, p_name text,
    p_archived boolean, p_version bigint, p_icon text default null) returns public.shopping_lists
language plpgsql security definer set search_path = '' as $$
declare row public.shopping_lists;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'shopping.message_household_access_denied'; end if;
    if p_name is null or length(trim(p_name)) not between 1 and 60 or p_archived is null or p_version is null then raise exception 'shopping.message_give_the_list_a_name_up_to_60_characters'; end if;
    if p_icon is not null and p_icon not in ('bag', 'cart', 'produce', 'home', 'gift', 'travel', 'pets', 'health') then raise exception 'shopping.message_choose_a_list_icon'; end if;
    perform 1 from public.households where id = p_household_id for update;
    select * into row from public.shopping_lists where id = p_id for update;
    if found then
        if row.household_id <> p_household_id then raise exception 'shopping.message_household_access_denied'; end if;
        if row.name = trim(p_name) and row.archived = p_archived and row.icon = coalesce(p_icon, row.icon) then return row; end if;
        if row.version <> p_version then raise exception 'shopping.message_this_list_changed_refresh_and_try_again'; end if;
        if p_archived and not row.archived and (select count(*) from public.shopping_lists where household_id = p_household_id and not archived) <= 1 then raise exception 'shopping.message_keep_at_least_one_shopping_list_active'; end if;
        update public.shopping_lists set name = trim(p_name), archived = p_archived, icon = coalesce(p_icon, icon), version = version + 1
        where id = p_id returning * into row;
    else
        if p_version <> 0 or p_archived then raise exception 'shopping.message_this_shopping_list_no_longer_exists'; end if;
        insert into public.shopping_lists(id, household_id, name, icon)
        values (p_id, p_household_id, trim(p_name), coalesce(p_icon, 'bag')) returning * into row;
    end if;
    return row;
end;
$$;
create function public.save_shopping_list(p_household_id uuid, p_id uuid, p_name text,
    p_archived boolean, p_version bigint, p_icon text default null) returns public.shopping_lists
language sql security invoker set search_path = '' as $$
    select loom_private.save_shopping_list(p_household_id, p_id, p_name, p_archived, p_version, p_icon);
$$;
revoke all on function loom_private.save_shopping_list(uuid, uuid, text, boolean, bigint, text) from public, anon;
revoke all on function public.save_shopping_list(uuid, uuid, text, boolean, bigint, text) from public, anon;
grant execute on function loom_private.save_shopping_list(uuid, uuid, text, boolean, bigint, text) to authenticated, service_role;
grant execute on function public.save_shopping_list(uuid, uuid, text, boolean, bigint, text) to authenticated, service_role;

commit;
