begin;

-- Profile and household pictures. Each owner keeps exactly one stored picture: replacing or
-- clearing it queues the previous object, and the household worker deletes it through the
-- Storage API. Unreferenced uploads older than an hour are swept the same way.
insert into storage.buckets(id, name, public, file_size_limit, allowed_mime_types)
values ('pictures', 'pictures', false, 1048576, array['image/jpeg'])
on conflict (id) do nothing;

alter table public.profiles add column avatar_path text;
alter table public.households add column image_path text;

-- Picture columns change only through the functions below, which validate the object and queue
-- the previous one; a direct profile update cannot point at someone else's file.
create function loom_private.guard_picture_columns() returns trigger
language plpgsql security invoker set search_path = '' as $$
begin
    if coalesce(current_setting('loom.picture_write', true), '') <> 'on' then
        raise exception 'pictures.access_denied';
    end if;
    return new;
end;
$$;
revoke all on function loom_private.guard_picture_columns() from public, anon, authenticated;
create trigger profile_avatar_guard before update of avatar_path on public.profiles
for each row when (old.avatar_path is distinct from new.avatar_path) execute function loom_private.guard_picture_columns();
create trigger household_image_guard before update of image_path on public.households
for each row when (old.image_path is distinct from new.image_path) execute function loom_private.guard_picture_columns();

create function loom_private.can_upload_picture(p_path text) returns boolean
language sql stable security invoker set search_path = '' as $$
    select case
        when auth.uid() is null or p_path is null then false
        when p_path ~ '^profiles/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\.jpg$'
            then split_part(p_path, '/', 2) = auth.uid()::text
        when p_path ~ '^households/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\.jpg$'
            then loom_private.is_member(split_part(p_path, '/', 2)::uuid)
        else false end;
$$;

-- Only the current picture is readable: a member's avatar by anyone who can see their profile,
-- a household's picture by its members.
create function loom_private.can_read_picture(p_path text) returns boolean
language sql stable security definer set search_path = '' as $$
    select exists(select 1 from public.profiles p where p.avatar_path = p_path
            and (p.id = auth.uid() or loom_private.can_view_profile(p.id)))
        or exists(select 1 from public.households h where h.image_path = p_path
            and loom_private.is_member(h.id));
$$;
revoke all on function loom_private.can_upload_picture(text), loom_private.can_read_picture(text) from public, anon;
grant execute on function loom_private.can_upload_picture(text), loom_private.can_read_picture(text) to authenticated, service_role;

create policy picture_object_read on storage.objects for select to authenticated using (
    bucket_id = 'pictures' and (loom_private.can_read_picture(name) or
        (owner_id = (select auth.uid())::text and loom_private.can_upload_picture(name)))
);
create policy picture_object_insert on storage.objects for insert to authenticated with check (
    bucket_id = 'pictures' and owner_id = (select auth.uid())::text and loom_private.can_upload_picture(name)
);
-- An unlinked upload of one's own can be withdrawn; current pictures go through the queue.
create policy picture_object_delete on storage.objects for delete to authenticated using (
    bucket_id = 'pictures' and owner_id = (select auth.uid())::text and
    loom_private.can_upload_picture(name) and not loom_private.can_read_picture(name)
);
-- Existing permissive policies for other buckets must never broaden this bucket's authorization.
create policy picture_read_guard on storage.objects as restrictive for select to authenticated using (
    bucket_id <> 'pictures' or loom_private.can_read_picture(name) or
    (owner_id = (select auth.uid())::text and loom_private.can_upload_picture(name))
);
create policy picture_insert_guard on storage.objects as restrictive for insert to authenticated with check (
    bucket_id <> 'pictures' or (owner_id = (select auth.uid())::text and loom_private.can_upload_picture(name))
);
create policy picture_delete_guard on storage.objects as restrictive for delete to authenticated using (
    bucket_id <> 'pictures' or (owner_id = (select auth.uid())::text and
        loom_private.can_upload_picture(name) and not loom_private.can_read_picture(name))
);
create policy picture_update_guard on storage.objects as restrictive for update to authenticated
    using (bucket_id <> 'pictures') with check (bucket_id <> 'pictures');
create policy picture_anon_guard on storage.objects as restrictive for all to anon
    using (bucket_id <> 'pictures') with check (bucket_id <> 'pictures');

create table loom_private.picture_deletions (
    object_path text primary key,
    available_at timestamptz not null default now(),
    leased_until timestamptz,
    attempts integer not null default 0
);
alter table loom_private.picture_deletions enable row level security;
create index picture_deletions_ready_idx on loom_private.picture_deletions(available_at, leased_until);
revoke all on loom_private.picture_deletions from public, anon, authenticated;
grant all on loom_private.picture_deletions to service_role;

create function loom_private.require_uploaded_picture(p_path text) returns void
language plpgsql stable security definer set search_path = '' as $$
begin
    if not loom_private.can_upload_picture(p_path) or not exists(select 1 from storage.objects
        where bucket_id = 'pictures' and name = p_path and owner_id = auth.uid()::text) then
        raise exception 'pictures.invalid_picture';
    end if;
end;
$$;
revoke all on function loom_private.require_uploaded_picture(text) from public, anon, authenticated;

-- A null path clears the picture. Returns the stored path.
create function loom_private.set_profile_picture(p_path text) returns text
language plpgsql security definer set search_path = '' as $$
declare previous text;
begin
    if auth.uid() is null then raise exception 'pictures.access_denied'; end if;
    if p_path is not null then
        if split_part(p_path, '/', 1) <> 'profiles' then raise exception 'pictures.invalid_picture'; end if;
        perform loom_private.require_uploaded_picture(p_path);
    end if;
    select avatar_path into previous from public.profiles where id = auth.uid() for update;
    if not found then raise exception 'pictures.access_denied'; end if;
    if previous is not distinct from p_path then return p_path; end if;
    perform pg_catalog.set_config('loom.picture_write', 'on', true);
    update public.profiles set avatar_path = p_path where id = auth.uid();
    perform pg_catalog.set_config('loom.picture_write', '', true);
    delete from loom_private.picture_deletions where object_path = p_path;
    if previous is not null then
        insert into loom_private.picture_deletions(object_path) values (previous) on conflict do nothing;
    end if;
    return p_path;
end;
$$;

create function loom_private.set_household_picture(p_household_id uuid, p_path text) returns text
language plpgsql security definer set search_path = '' as $$
declare previous text;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'pictures.access_denied'; end if;
    if p_path is not null then
        if split_part(p_path, '/', 1) <> 'households' or split_part(p_path, '/', 2) <> p_household_id::text then
            raise exception 'pictures.invalid_picture';
        end if;
        perform loom_private.require_uploaded_picture(p_path);
    end if;
    select image_path into previous from public.households where id = p_household_id for update;
    if not found then raise exception 'pictures.access_denied'; end if;
    if previous is not distinct from p_path then return p_path; end if;
    perform pg_catalog.set_config('loom.picture_write', 'on', true);
    update public.households set image_path = p_path where id = p_household_id;
    perform pg_catalog.set_config('loom.picture_write', '', true);
    delete from loom_private.picture_deletions where object_path = p_path;
    if previous is not null then
        insert into loom_private.picture_deletions(object_path) values (previous) on conflict do nothing;
    end if;
    return p_path;
end;
$$;
create function public.set_profile_picture(p_path text) returns text
language sql security invoker set search_path = '' as $$ select loom_private.set_profile_picture(p_path); $$;
create function public.set_household_picture(p_household_id uuid, p_path text) returns text
language sql security invoker set search_path = '' as $$ select loom_private.set_household_picture(p_household_id, p_path); $$;
revoke all on function loom_private.set_profile_picture(text), public.set_profile_picture(text),
    loom_private.set_household_picture(uuid, text), public.set_household_picture(uuid, text) from public, anon;
grant execute on function loom_private.set_profile_picture(text), public.set_profile_picture(text),
    loom_private.set_household_picture(uuid, text), public.set_household_picture(uuid, text) to authenticated, service_role;

-- Claim bounded batches; the worker removes blobs through the Storage API, never only metadata.
create function loom_private.claim_picture_deletions(p_limit integer default 20) returns setof text
language plpgsql security definer set search_path = '' as $$
begin
    insert into loom_private.picture_deletions(object_path)
        select o.name from storage.objects o where o.bucket_id = 'pictures' and o.created_at < now() - interval '1 hour'
        and not exists(select 1 from public.profiles p where p.avatar_path = o.name)
        and not exists(select 1 from public.households h where h.image_path = o.name)
        order by o.created_at limit 100 on conflict do nothing;
    -- Never delete a picture that became current again.
    delete from loom_private.picture_deletions q
        where exists(select 1 from public.profiles p where p.avatar_path = q.object_path)
        or exists(select 1 from public.households h where h.image_path = q.object_path);
    return query update loom_private.picture_deletions q set leased_until = now() + interval '5 minutes', attempts = attempts + 1
        where object_path in (select object_path from loom_private.picture_deletions where available_at <= now()
            and (leased_until is null or leased_until < now()) order by available_at limit least(greatest(p_limit, 1), 100) for update skip locked)
        returning q.object_path;
end;
$$;
create function loom_private.finish_picture_deletions(p_paths text[], p_success boolean) returns void
language plpgsql security definer set search_path = '' as $$
begin
    if p_success then delete from loom_private.picture_deletions where object_path = any(p_paths);
    else update loom_private.picture_deletions set available_at = now() + interval '10 minutes', leased_until = null where object_path = any(p_paths); end if;
end;
$$;
create function public.claim_picture_deletions(p_limit integer default 20) returns setof text
language sql security invoker set search_path = '' as $$ select * from loom_private.claim_picture_deletions(p_limit); $$;
create function public.finish_picture_deletions(p_paths text[], p_success boolean) returns void
language sql security invoker set search_path = '' as $$ select loom_private.finish_picture_deletions(p_paths, p_success); $$;
revoke all on function loom_private.claim_picture_deletions(integer), public.claim_picture_deletions(integer),
    loom_private.finish_picture_deletions(text[], boolean), public.finish_picture_deletions(text[], boolean) from public, anon, authenticated;
grant execute on function loom_private.claim_picture_deletions(integer), public.claim_picture_deletions(integer),
    loom_private.finish_picture_deletions(text[], boolean), public.finish_picture_deletions(text[], boolean) to service_role;

-- Member lists carry each avatar so every screen can show it. Appended columns are ignored by
-- older clients.
drop function public.household_members_with_names(uuid);
drop function loom_private.household_members_with_names(uuid);
create function loom_private.household_members_with_names(p_household_id uuid)
returns table(user_id uuid, display_name text, role text, avatar_path text)
language plpgsql stable security definer set search_path = '' as $$
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'Household access denied.'; end if;
    return query select m.user_id, p.display_name, m.role, p.avatar_path from public.household_members m
        join public.profiles p on p.id = m.user_id where m.household_id = p_household_id and m.active order by m.joined_at;
end;
$$;
create function public.household_members_with_names(p_household_id uuid)
returns table(user_id uuid, display_name text, role text, avatar_path text)
language sql stable security invoker set search_path = '' as $$ select * from loom_private.household_members_with_names(p_household_id); $$;

drop function public.household_member_history(uuid);
drop function loom_private.household_member_history(uuid);
create function loom_private.household_member_history(p_household_id uuid)
returns table(user_id uuid, display_name text, role text, active boolean, version bigint, avatar_path text)
language plpgsql stable security definer set search_path = '' as $$
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'household.access_denied'; end if;
    return query select m.user_id, p.display_name, m.role, m.active, m.version, p.avatar_path
        from public.household_members m join public.profiles p on p.id = m.user_id
        where m.household_id = p_household_id order by m.joined_at;
end;
$$;
create function public.household_member_history(p_household_id uuid)
returns table(user_id uuid, display_name text, role text, active boolean, version bigint, avatar_path text)
language sql stable security invoker set search_path = '' as $$ select * from loom_private.household_member_history(p_household_id); $$;
revoke all on function loom_private.household_members_with_names(uuid), public.household_members_with_names(uuid),
    loom_private.household_member_history(uuid), public.household_member_history(uuid) from public, anon;
grant execute on function loom_private.household_members_with_names(uuid), public.household_members_with_names(uuid),
    loom_private.household_member_history(uuid), public.household_member_history(uuid) to authenticated, service_role;

commit;
