begin;

-- A checklist has one parent level. Group writes freeze every affected version.
-- Removing a parent promotes its children; the app retains the full family for transactional Undo.
alter table public.shopping_items add column parent_id uuid references public.shopping_items(id) on delete set null;
alter table public.shopping_items add constraint shopping_not_own_parent check(parent_id is distinct from id);
create index shopping_items_parent_idx on public.shopping_items(parent_id) where parent_id is not null;

create function loom_private.validate_shopping_parent() returns trigger
language plpgsql security definer set search_path = '' as $$
declare item public.shopping_items; parent public.shopping_items;
begin
    -- Deferred checks inspect the final row, after a whole group has moved.
    select * into item from public.shopping_items where id = new.id;
    if not found then return null; end if;
    if item.parent_id is not null then
        select * into parent from public.shopping_items where id = item.parent_id;
        if not found or parent.parent_id is not null or parent.household_id <> item.household_id or parent.list_id <> item.list_id then
            raise exception 'shopping.invalid_group';
        end if;
    end if;
    if exists(select 1 from public.shopping_items c where c.parent_id = item.id and
        (item.parent_id is not null or c.list_id <> item.list_id or c.household_id <> item.household_id)) then
        raise exception 'shopping.invalid_group';
    end if;
    return null;
end;
$$;
create constraint trigger shopping_parent_scope after insert or update on public.shopping_items
    deferrable initially deferred for each row execute function loom_private.validate_shopping_parent();

-- Keep old clients' single-item writes consistent too; metadata triggers own every changed version.
create function loom_private.sync_shopping_group() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
    if pg_trigger_depth() > 1 then return null; end if;
    if tg_op = 'INSERT' then
        if new.parent_id is not null then
            update public.shopping_items p set checked = summary.complete
            from (select bool_and(checked) as complete from public.shopping_items where parent_id = new.parent_id and not removed) summary
            where p.id = new.parent_id and p.checked is distinct from summary.complete;
        end if;
        return null;
    end if;
    if new.parent_id is null and new.checked is distinct from old.checked then
        update public.shopping_items set checked = new.checked
        where parent_id = new.id and not removed and checked is distinct from new.checked;
    end if;
    if new.removed and not old.removed then
        update public.shopping_items set parent_id = null where parent_id = new.id;
    end if;
    if new.parent_id is null and new.list_id is distinct from old.list_id then
        update public.shopping_items set list_id = new.list_id where parent_id = new.id;
    end if;
    if new.parent_id is distinct from old.parent_id or new.checked is distinct from old.checked or new.removed is distinct from old.removed then
        update public.shopping_items p set checked = summary.complete
        from (
            select parent_id, bool_and(checked) as complete from public.shopping_items
            where parent_id in (new.parent_id, old.parent_id) and not removed group by parent_id
        ) summary where p.id = summary.parent_id and p.checked is distinct from summary.complete;
    end if;
    return null;
end;
$$;
create trigger shopping_group_sync after insert or update on public.shopping_items
    for each row execute function loom_private.sync_shopping_group();
revoke all on function loom_private.validate_shopping_parent(), loom_private.sync_shopping_group() from public, anon, authenticated;

create function loom_private.save_shopping_group(p_household_id uuid, p_items jsonb)
returns setof public.shopping_items language plpgsql security definer set search_path = '' as $$
declare item jsonb; current_item public.shopping_items; all_same boolean := true; ids uuid[];
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then
        raise exception 'shopping.message_household_access_denied';
    end if;
    if p_items is null or jsonb_typeof(p_items) <> 'array' or jsonb_array_length(p_items) not between 1 and 500 then
        raise exception 'shopping.invalid_group';
    end if;
    select array_agg((value->>'id')::uuid) into ids from jsonb_array_elements(p_items);
    if cardinality(ids) <> (select count(distinct id) from unnest(ids) id) then raise exception 'shopping.invalid_group'; end if;
    perform 1 from public.households where id = p_household_id for update;
    perform 1 from public.shopping_items where id = any(ids) order by id for update;
    for item in select value from jsonb_array_elements(p_items) loop
        select * into current_item from public.shopping_items where id = (item->>'id')::uuid;
        if not found then raise exception 'shopping.message_this_shopping_item_no_longer_exists'; end if;
        if current_item.household_id <> p_household_id then raise exception 'shopping.message_household_access_denied'; end if;
        all_same := all_same and (current_item.list_id, current_item.name, current_item.quantity, current_item.category,
            current_item.checked, current_item.favorite, current_item.removed, current_item.parent_id)
            is not distinct from ((item->>'list_id')::uuid, trim(item->>'name'), trim(item->>'quantity'), item->>'category',
            (item->>'checked')::boolean, (item->>'favorite')::boolean, (item->>'removed')::boolean, (item->>'parent_id')::uuid);
    end loop;
    if all_same then return query select * from public.shopping_items where id = any(ids); return; end if;
    -- Freeze the whole family before any trigger can cascade a write.
    for item in select value from jsonb_array_elements(p_items) loop
        select * into current_item from public.shopping_items where id = (item->>'id')::uuid;
        if current_item.version is distinct from (item->>'version')::bigint then
            raise exception 'shopping.message_this_item_changed_refresh_before_undoing_or_editing_it';
        end if;
        if exists(select 1 from public.shopping_items c where not c.removed and
            (c.parent_id = current_item.id or c.id = current_item.parent_id or
             (current_item.parent_id is not null and c.parent_id = current_item.parent_id)) and not(c.id = any(ids))) then
            raise exception 'shopping.message_this_item_changed_refresh_before_undoing_or_editing_it';
        end if;
        if (item->>'parent_id') is not null and not((item->>'parent_id')::uuid = any(ids)) then
            raise exception 'shopping.invalid_group';
        end if;
    end loop;
    -- Detach first, so moving a child never leaves a cross-list link.
    for item in select value from jsonb_array_elements(p_items) loop
        update public.shopping_items set parent_id = null where id = (item->>'id')::uuid
            and parent_id is not null and parent_id is distinct from (item->>'parent_id')::uuid;
    end loop;
    for item in select value from jsonb_array_elements(p_items) loop
        select * into current_item from public.shopping_items where id = (item->>'id')::uuid;
        perform loom_private.save_shopping_item(p_household_id, current_item.id, (item->>'list_id')::uuid,
            item->>'name', item->>'quantity', item->>'category', (item->>'checked')::boolean,
            (item->>'favorite')::boolean, (item->>'removed')::boolean, current_item.version);
        update public.shopping_items set parent_id = (item->>'parent_id')::uuid
            where id = current_item.id and parent_id is distinct from (item->>'parent_id')::uuid;
    end loop;
    return query select * from public.shopping_items where id = any(ids);
end;
$$;
create function public.save_shopping_group(p_household_id uuid, p_items jsonb)
returns setof public.shopping_items language sql security invoker set search_path = '' as $$
    select * from loom_private.save_shopping_group(p_household_id, p_items);
$$;
revoke all on function loom_private.save_shopping_group(uuid,jsonb), public.save_shopping_group(uuid,jsonb) from public, anon;
grant execute on function loom_private.save_shopping_group(uuid,jsonb), public.save_shopping_group(uuid,jsonb) to authenticated, service_role;

-- Bulk removal can promote children and update parent versions. Return the current list
-- in the same transaction so the next gesture never starts from stale family metadata.
create function loom_private.remove_completed_shopping_group(p_household_id uuid, p_list_id uuid, p_versions jsonb)
returns setof public.shopping_items language plpgsql security definer set search_path = '' as $$
begin
    perform loom_private.remove_completed_shopping_items(p_household_id,p_list_id,p_versions);
    return query select * from public.shopping_items where household_id = p_household_id and list_id = p_list_id;
end;
$$;
create function public.remove_completed_shopping_group(p_household_id uuid, p_list_id uuid, p_versions jsonb)
returns setof public.shopping_items language sql security invoker set search_path = '' as $$
    select * from loom_private.remove_completed_shopping_group(p_household_id,p_list_id,p_versions);
$$;
revoke all on function loom_private.remove_completed_shopping_group(uuid,uuid,jsonb), public.remove_completed_shopping_group(uuid,uuid,jsonb) from public,anon;
grant execute on function loom_private.remove_completed_shopping_group(uuid,uuid,jsonb), public.remove_completed_shopping_group(uuid,uuid,jsonb) to authenticated,service_role;

-- Receipts are immutable objects; only the current, versioned link is visible to readers.
insert into storage.buckets(id, name, public, file_size_limit, allowed_mime_types)
values ('expense-receipts', 'expense-receipts', false, 5242880, array['image/jpeg'])
on conflict(id) do update set public = false, file_size_limit = excluded.file_size_limit, allowed_mime_types = excluded.allowed_mime_types;

create table public.expense_receipts (
    expense_id uuid primary key references public.expenses(id) on delete cascade,
    household_id uuid not null references public.households(id),
    account_id uuid not null references public.expense_accounts(id),
    object_path text unique,
    version bigint not null default 1,
    created_at timestamptz not null default now()
);
create index expense_receipts_household_idx on public.expense_receipts(household_id);
create index expense_receipts_account_idx on public.expense_receipts(account_id);
alter table public.expense_receipts enable row level security;
revoke all on public.expense_receipts from public, anon, authenticated;
grant select on public.expense_receipts to authenticated;
grant all on public.expense_receipts to service_role;
create policy expense_receipts_read on public.expense_receipts for select to authenticated using (
    loom_private.is_member(household_id) and exists(select 1 from public.expenses e
        where e.id = expense_id and e.household_id = expense_receipts.household_id
        and e.account_id = expense_receipts.account_id and e.voided_at is null)
);

create function loom_private.can_upload_receipt(p_path text) returns boolean
language sql stable security invoker set search_path = '' as $$
    select case when auth.uid() is not null and p_path ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\.jpg$'
    then exists(select 1 from public.expenses e join public.expense_accounts a on a.id = e.account_id
        where e.id = split_part(p_path,'/',3)::uuid and e.account_id::text = split_part(p_path,'/',2)
        and e.household_id::text = split_part(p_path,'/',1) and e.voided_at is null and not a.archived
        and loom_private.is_member(e.household_id)
        and (e.created_by = auth.uid() or e.paid_by = auth.uid() or loom_private.is_owner(e.household_id)))
    else false end;

$$;
create function loom_private.can_read_receipt(p_path text) returns boolean
language sql stable security invoker set search_path = '' as $$
    select exists(select 1 from public.expense_receipts where object_path = p_path);
$$;
revoke all on function loom_private.can_upload_receipt(text), loom_private.can_read_receipt(text) from public, anon;
grant execute on function loom_private.can_upload_receipt(text), loom_private.can_read_receipt(text) to authenticated, service_role;

create policy receipt_object_read on storage.objects for select to authenticated using (
    bucket_id = 'expense-receipts' and (loom_private.can_read_receipt(name) or
        (owner_id = (select auth.uid())::text and loom_private.can_upload_receipt(name)))
);
create policy receipt_object_insert on storage.objects for insert to authenticated with check (
    bucket_id = 'expense-receipts' and owner_id = (select auth.uid())::text and loom_private.can_upload_receipt(name)
);
create policy receipt_object_delete on storage.objects for delete to authenticated using (
    bucket_id = 'expense-receipts' and owner_id = (select auth.uid())::text and
    loom_private.can_upload_receipt(name) and not loom_private.can_read_receipt(name)
);
-- Existing permissive policies for other buckets must never broaden this bucket's authorization.
create policy receipt_read_guard on storage.objects as restrictive for select to authenticated using (
    bucket_id <> 'expense-receipts' or loom_private.can_read_receipt(name) or
    (owner_id = (select auth.uid())::text and loom_private.can_upload_receipt(name))
);
create policy receipt_insert_guard on storage.objects as restrictive for insert to authenticated with check (
    bucket_id <> 'expense-receipts' or (owner_id = (select auth.uid())::text and loom_private.can_upload_receipt(name))
);
create policy receipt_delete_guard on storage.objects as restrictive for delete to authenticated using (
    bucket_id <> 'expense-receipts' or (owner_id = (select auth.uid())::text and loom_private.can_upload_receipt(name) and not loom_private.can_read_receipt(name))
);
create policy receipt_update_guard on storage.objects as restrictive for update to authenticated
    using(bucket_id <> 'expense-receipts') with check(bucket_id <> 'expense-receipts');
create policy receipt_anon_guard on storage.objects as restrictive for all to anon
    using(bucket_id <> 'expense-receipts') with check(bucket_id <> 'expense-receipts');

create table loom_private.receipt_deletions (
    object_path text primary key,
    available_at timestamptz not null default now(),
    leased_until timestamptz,
    attempts integer not null default 0
);
alter table loom_private.receipt_deletions enable row level security;
create index receipt_deletions_ready_idx on loom_private.receipt_deletions(available_at, leased_until);
revoke all on loom_private.receipt_deletions from public, anon, authenticated;
grant all on loom_private.receipt_deletions to service_role;

create function loom_private.queue_receipt_cleanup() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
    if old.object_path is not null and (tg_op = 'DELETE' or old.object_path is distinct from new.object_path) then
        insert into loom_private.receipt_deletions(object_path) values(old.object_path) on conflict do nothing;
    end if;
    return null;
end;
$$;
create trigger receipt_cleanup after update or delete on public.expense_receipts
for each row execute function loom_private.queue_receipt_cleanup();
create function loom_private.clear_voided_receipt() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
    if new.voided_at is not null then delete from public.expense_receipts where expense_id = new.id; end if;
    return null;
end;
$$;
create trigger expense_receipt_voided after update of voided_at on public.expenses
for each row execute function loom_private.clear_voided_receipt();
revoke all on function loom_private.queue_receipt_cleanup(), loom_private.clear_voided_receipt() from public, anon, authenticated;

create function loom_private.save_expense_receipt(p_household_id uuid, p_account_id uuid, p_expense_id uuid, p_object_path text, p_version bigint)
returns public.expense_receipts language plpgsql security definer set search_path = '' as $$
declare existing public.expense_receipts; result public.expense_receipts;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'expenses.account_access_denied'; end if;
    -- Coordinate the short DB link/lease transactions; no Storage API I/O runs under this lock.
    perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended('loom:receipt-links',0));
    perform 1 from public.expense_accounts where id = p_account_id and household_id = p_household_id and not archived for no key update;
    if not found then raise exception 'expenses.account_archived'; end if;
    perform 1 from public.expenses e where e.id = p_expense_id and e.household_id = p_household_id and e.account_id = p_account_id
        and e.voided_at is null and (e.created_by = auth.uid() or e.paid_by = auth.uid() or loom_private.is_owner(p_household_id)) for update;
    if not found then raise exception 'expenses.account_access_denied'; end if;
    select * into existing from public.expense_receipts where expense_id = p_expense_id for update;
    if p_object_path is not null and existing.object_path = p_object_path then return existing; end if;
    if coalesce(existing.version,0) is distinct from p_version then raise exception 'expenses.receipt_changed'; end if;
    -- Retain a tombstone/version after removal: a stale delete cannot remove a later replacement.
    if p_object_path is null then
        if existing.expense_id is null then return null; end if;
        update public.expense_receipts set object_path = null, version = version + 1 where expense_id = p_expense_id returning * into result;
        return result;
    end if;
    if exists(select 1 from loom_private.receipt_deletions where object_path = p_object_path) then raise exception 'expenses.receipt_changed'; end if;
    if split_part(p_object_path,'/',1) <> p_household_id::text or split_part(p_object_path,'/',2) <> p_account_id::text or
        split_part(p_object_path,'/',3) <> p_expense_id::text or not loom_private.can_upload_receipt(p_object_path) or
        not exists(select 1 from storage.objects where bucket_id = 'expense-receipts' and name = p_object_path and owner_id = auth.uid()::text) then
        raise exception 'expenses.invalid_receipt';
    end if;
    insert into public.expense_receipts(expense_id, household_id, account_id, object_path, version)
    values(p_expense_id,p_household_id,p_account_id,p_object_path,coalesce(existing.version,0)+1)
    on conflict(expense_id) do update set object_path = excluded.object_path, version = expense_receipts.version + 1
    returning * into result;
    return result;
end;
$$;
create function public.save_expense_receipt(p_household_id uuid, p_account_id uuid, p_expense_id uuid, p_object_path text, p_version bigint)
returns public.expense_receipts language sql security invoker set search_path = '' as $$
    select loom_private.save_expense_receipt(p_household_id,p_account_id,p_expense_id,p_object_path,p_version);
$$;
revoke all on function loom_private.save_expense_receipt(uuid,uuid,uuid,text,bigint), public.save_expense_receipt(uuid,uuid,uuid,text,bigint) from public,anon;
grant execute on function loom_private.save_expense_receipt(uuid,uuid,uuid,text,bigint), public.save_expense_receipt(uuid,uuid,uuid,text,bigint) to authenticated,service_role;

-- Claim bounded batches; an API worker removes actual blobs, never just Storage metadata rows.
create function loom_private.claim_receipt_deletions(p_limit integer default 20) returns setof text
language plpgsql security definer set search_path = '' as $$
begin
    perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended('loom:receipt-links',0));
    delete from loom_private.receipt_deletions q where exists(select 1 from public.expense_receipts r where r.object_path = q.object_path);
    insert into loom_private.receipt_deletions(object_path)
        select o.name from storage.objects o where o.bucket_id = 'expense-receipts' and o.created_at < now() - interval '24 hours'
        and not exists(select 1 from public.expense_receipts r where r.object_path = o.name)
        order by o.created_at limit 100 on conflict do nothing;
    return query update loom_private.receipt_deletions q set leased_until = now() + interval '5 minutes', attempts = attempts + 1
        where object_path in (select object_path from loom_private.receipt_deletions where available_at <= now()
            and (leased_until is null or leased_until < now()) order by available_at limit least(greatest(p_limit,1),100) for update skip locked)
        returning q.object_path;
end;
$$;
create function loom_private.finish_receipt_deletions(p_paths text[], p_success boolean) returns void
language plpgsql security definer set search_path = '' as $$
begin
    if p_success then delete from loom_private.receipt_deletions where object_path = any(p_paths);
    else update loom_private.receipt_deletions set available_at = now() + interval '10 minutes', leased_until = null where object_path = any(p_paths); end if;
end;
$$;
create function public.claim_receipt_deletions(p_limit integer default 20) returns setof text
language sql security invoker set search_path = '' as $$select * from loom_private.claim_receipt_deletions(p_limit);$$;
create function public.finish_receipt_deletions(p_paths text[], p_success boolean) returns void
language sql security invoker set search_path = '' as $$select loom_private.finish_receipt_deletions(p_paths,p_success);$$;
revoke all on function loom_private.claim_receipt_deletions(integer), public.claim_receipt_deletions(integer),
    loom_private.finish_receipt_deletions(text[],boolean), public.finish_receipt_deletions(text[],boolean) from public,anon,authenticated;
grant execute on function loom_private.claim_receipt_deletions(integer), public.claim_receipt_deletions(integer),
    loom_private.finish_receipt_deletions(text[],boolean), public.finish_receipt_deletions(text[],boolean) to service_role;

commit;
