begin;

-- Keep-style entry places a new line directly below the item being edited. Positions remain
-- server-owned. While inserting, this transaction shifts the later rows of one list without
-- consuming new positions or versions, so other open clients keep valid row versions.
create or replace function loom_private.shopping_item_metadata() returns trigger
language plpgsql security definer set search_path='' as $$
begin
    if tg_op='UPDATE' and current_setting('loom.shopping_reposition',true)='on' then
        if (to_jsonb(new)-'position') is distinct from (to_jsonb(old)-'position') then
            raise exception 'shopping.invalid_reposition';
        end if;
        return new;
    end if;
    if new.list_id is null then
        select id into new.list_id from public.shopping_lists where household_id=new.household_id and not archived order by created_at,id limit 1;
    end if;
    if not exists(select 1 from public.shopping_lists where id=new.list_id and household_id=new.household_id and not archived) then
        raise exception 'shopping.message_choose_an_active_shopping_list_in_this_household';
    end if;
    new.version:=case when tg_op='INSERT' then 1 else old.version+1 end;
    return new;
end;
$$;

create or replace function loom_private.assign_shopping_position() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
    if tg_op = 'UPDATE' and current_setting('loom.shopping_reposition', true) = 'on' then
        return new;
    end if;
    if tg_op = 'INSERT' then
        new.position := nextval('loom_private.shopping_item_position');
    elsif new.list_id is distinct from old.list_id or
        (old.checked and not new.checked) or (old.removed and not new.removed) then
        new.position := nextval('loom_private.shopping_item_position');
    else
        new.position := old.position;
    end if;
    return new;
end;
$$;

-- Returns the new row followed by every row whose position moved. A repeated request returns the
-- stored row unchanged. A missing, removed or moved anchor appends, so queued entries never fail
-- because an earlier line changed. Below a group parent or child, the new line joins that group.
create function loom_private.insert_shopping_item_after(p_household_id uuid, p_item jsonb, p_after_id uuid)
returns setof public.shopping_items language plpgsql security definer set search_path = '' as $$
declare
    v_id uuid := (p_item->>'id')::uuid;
    v_list uuid := (p_item->>'list_id')::uuid;
    v_parent uuid;
    anchor public.shopping_items;
    inserted public.shopping_items;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then
        raise exception 'shopping.message_household_access_denied';
    end if;
    if v_id is null or v_list is null then raise exception 'shopping.invalid_group'; end if;
    select * into inserted from public.shopping_items where id = v_id;
    if found then
        if inserted.household_id <> p_household_id then
            raise exception 'shopping.message_household_access_denied';
        end if;
        return next inserted;
        return;
    end if;
    -- Serialize insertions within one list so concurrent shifts cannot interleave.
    perform 1 from public.shopping_lists where id = v_list and household_id = p_household_id for update;
    select * into anchor from public.shopping_items
    where id = p_after_id and household_id = p_household_id and list_id = v_list and not removed;
    inserted := loom_private.save_shopping_item(p_household_id, v_id, v_list, p_item->>'name',
        coalesce(p_item->>'quantity', ''), coalesce(p_item->>'category', 'Other'), false, false, false, 0);
    if anchor.id is null then
        return next inserted;
        return;
    end if;
    v_parent := coalesce(anchor.parent_id, case when exists(
        select 1 from public.shopping_items c where c.parent_id = anchor.id and not c.removed
    ) then anchor.id end);
    if v_parent is not null then
        update public.shopping_items set parent_id = v_parent where id = v_id returning * into inserted;
    end if;
    perform set_config('loom.shopping_reposition', 'on', true);
    update public.shopping_items set position = anchor.position + 1 where id = v_id returning * into inserted;
    return next inserted;
    return query update public.shopping_items set position = position + 1
        where household_id = p_household_id and list_id = v_list and position > anchor.position and id <> v_id
        returning *;
    perform set_config('loom.shopping_reposition', 'off', true);
    -- Later appends must stay after every shifted row.
    perform setval('loom_private.shopping_item_position', greatest(
        (select last_value from loom_private.shopping_item_position),
        (select max(position) from public.shopping_items)));
end;
$$;
create function public.insert_shopping_item_after(p_household_id uuid, p_item jsonb, p_after_id uuid)
returns setof public.shopping_items language sql security invoker set search_path = '' as $$
    select * from loom_private.insert_shopping_item_after(p_household_id, p_item, p_after_id);
$$;
revoke all on function loom_private.insert_shopping_item_after(uuid, jsonb, uuid) from public, anon;
revoke all on function public.insert_shopping_item_after(uuid, jsonb, uuid) from public, anon;
grant execute on function loom_private.insert_shopping_item_after(uuid, jsonb, uuid) to authenticated, service_role;
grant execute on function public.insert_shopping_item_after(uuid, jsonb, uuid) to authenticated, service_role;

commit;
