begin;

alter table loom_private.household_invites
    add column code_format text not null default 'legacy' check (code_format in ('legacy', 'short')),
    add column revoked_at timestamptz;

-- Failed redemptions return a result, so their rate-limit counters commit rather than roll back.
create table loom_private.invitation_attempts (
    user_id uuid primary key references auth.users(id) on delete cascade,
    window_started_at timestamptz not null default now(),
    attempts integer not null default 0 check (attempts between 0 and 11)
);
alter table loom_private.invitation_attempts enable row level security;
revoke all on loom_private.invitation_attempts from public, anon, authenticated;

create function loom_private.create_short_household_invitation(p_household_id uuid) returns text
language plpgsql security definer set search_path = '' as $$
declare
    token text;
    random_bytes bytea;
    alphabet constant text := 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
begin
    if auth.uid() is null or not loom_private.is_owner(p_household_id) then
        raise exception 'Only the household owner can invite members.';
    end if;
    perform 1 from public.households where id = p_household_id for update;
    update loom_private.household_invites set revoked_at = now()
    where household_id = p_household_id and used_at is null and revoked_at is null and expires_at > now();
    for retry in 1..16 loop
        token := '';
        random_bytes := extensions.gen_random_bytes(6);
        for i in 0..5 loop
            token := token || substr(alphabet, (get_byte(random_bytes, i) % 32) + 1, 1);
        end loop;
        begin
            insert into loom_private.household_invites(household_id, token_hash, created_by, expires_at, code_format)
            values(p_household_id, extensions.digest(token, 'sha256'), auth.uid(), now() + interval '24 hours', 'short');
            return token;
        exception when unique_violation then
            -- A collision never replaces an invitation belonging to another household.
            null;
        end;
    end loop;
    raise exception 'Could not create an invitation. Please try again.';
end;
$$;

create function loom_private.revoke_household_invitation(p_household_id uuid) returns void
language plpgsql security definer set search_path = '' as $$
begin
    if auth.uid() is null or not loom_private.is_owner(p_household_id) then
        raise exception 'Only the household owner can invite members.';
    end if;
    perform 1 from public.households where id = p_household_id for update;
    update loom_private.household_invites set revoked_at = now()
    where household_id = p_household_id and used_at is null and revoked_at is null;
end;
$$;

create function loom_private.redeem_household_invitation(p_token text) returns jsonb
language plpgsql security definer set search_path = '' as $$
declare
    invitation loom_private.household_invites;
    attempt_count integer;
    token text := trim(p_token);
begin
    if auth.uid() is null then raise exception 'Please sign in.'; end if;
    insert into loom_private.invitation_attempts(user_id, attempts) values(auth.uid(), 1)
    on conflict(user_id) do update set
        attempts = case when invitation_attempts.window_started_at <= now() - interval '15 minutes'
            then 1 else least(invitation_attempts.attempts + 1, 11) end,
        window_started_at = case when invitation_attempts.window_started_at <= now() - interval '15 minutes'
            then now() else invitation_attempts.window_started_at end
    returning attempts into attempt_count;
    if attempt_count > 10 then return jsonb_build_object('status', 'rate_limited'); end if;

    if token ~* '^[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}$' then
        token := upper(token);
    elsif token ~* '^[0-9a-f]{64}$' then
        token := lower(token);
    else
        return jsonb_build_object('status', 'invalid');
    end if;
    select * into invitation from loom_private.household_invites
    where token_hash = extensions.digest(token, 'sha256');
    if not found then return jsonb_build_object('status', 'invalid'); end if;
    perform 1 from public.households where id = invitation.household_id for update;
    select * into invitation from loom_private.household_invites
    where token_hash = extensions.digest(token, 'sha256') for update;
    if not found or invitation.used_at is not null or invitation.revoked_at is not null or invitation.expires_at <= now() then
        return jsonb_build_object('status', 'invalid');
    end if;
    if not exists(select 1 from public.household_members where household_id = invitation.household_id
        and user_id = invitation.created_by and active and role = 'owner') then
        return jsonb_build_object('status', 'invalid');
    end if;
    if loom_private.is_member(invitation.household_id) then
        return jsonb_build_object('status', 'already_joined');
    end if;
    if (select count(*) from public.household_members where household_id = invitation.household_id and active) >= 100 then
        return jsonb_build_object('status', 'full');
    end if;
    insert into public.household_members(household_id, user_id, role)
    values(invitation.household_id, auth.uid(), 'member')
    on conflict(household_id, user_id) do update set active = true, role = 'member';
    update loom_private.household_invites set used_at = now(), used_by = auth.uid() where id = invitation.id;
    return jsonb_build_object('status', 'joined');
end;
$$;

-- Old clients may redeem their existing high-entropy invitations, but cannot bypass the short-code limiter.
create or replace function loom_private.join_household(p_token text) returns void
language plpgsql security definer set search_path = '' as $$
declare result jsonb;
begin
    if p_token !~ '^[0-9a-f]{64}$' or p_token is null then
        raise exception 'This invitation is invalid or has expired.';
    end if;
    result := loom_private.redeem_household_invitation(p_token);
    case result->>'status'
        when 'joined' then return;
        when 'already_joined' then raise exception 'You are already in this household.';
        when 'full' then raise exception 'This household is full.';
        else raise exception 'This invitation is invalid or has expired.';
    end case;
end;
$$;

create function public.create_short_household_invitation(p_household_id uuid) returns text
language sql security invoker set search_path = '' as $$
    select loom_private.create_short_household_invitation(p_household_id);
$$;
create function public.redeem_household_invitation(p_token text) returns jsonb
language sql security invoker set search_path = '' as $$
    select loom_private.redeem_household_invitation(p_token);
$$;
create function public.revoke_household_invitation(p_household_id uuid) returns void
language sql security invoker set search_path = '' as $$
    select loom_private.revoke_household_invitation(p_household_id);
$$;

create function loom_private.remove_completed_shopping_items(p_household_id uuid, p_list_id uuid, p_versions jsonb)
returns setof public.shopping_items language plpgsql security definer set search_path = '' as $$
declare target record;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then
        raise exception 'shopping.message_household_access_denied';
    end if;
    if p_versions is null or jsonb_typeof(p_versions) <> 'object' then
        raise exception 'shopping.message_invalid_completed_selection';
    end if;
    if (select count(*) from jsonb_each(p_versions)) not between 1 and 1000 or
        exists(select 1 from jsonb_each(p_versions) where key !~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'
            or jsonb_typeof(value) <> 'number' or value::text !~ '^[1-9][0-9]{0,17}$') then
        raise exception 'shopping.message_invalid_completed_selection';
    end if;
    -- Same household lock order as list deletion, then stable row lock order to avoid deadlocks.
    perform 1 from public.households where id = p_household_id for update;
    if not exists(select 1 from public.shopping_lists where id = p_list_id and household_id = p_household_id and not archived) then
        raise exception 'shopping.message_choose_an_active_shopping_list_in_this_household';
    end if;
    for target in
        select key::uuid as id, value::text::bigint as version from jsonb_each(p_versions) order by key
    loop
        perform 1 from public.shopping_items
        where id = target.id and household_id = p_household_id and list_id = p_list_id
            and checked and (version = target.version or (removed and version = target.version + 1)) for update;
        if not found then
            raise exception 'shopping.message_this_item_changed_refresh_before_undoing_or_editing_it';
        end if;
    end loop;
    -- Use the existing household tombstone convention. Retry after a lost response is idempotent.
    return query
        with changed as (
            update public.shopping_items item set removed = true
            where item.household_id = p_household_id and item.list_id = p_list_id and not item.removed
                and item.id in (select key::uuid from jsonb_each(p_versions))
            returning item.*
        )
        select * from changed
        union all
        select item.* from public.shopping_items item
        where item.household_id = p_household_id and item.list_id = p_list_id and item.removed
            and item.id in (select key::uuid from jsonb_each(p_versions));
end;
$$;
create function public.remove_completed_shopping_items(p_household_id uuid, p_list_id uuid, p_versions jsonb)
returns setof public.shopping_items language sql security invoker set search_path = '' as $$
    select * from loom_private.remove_completed_shopping_items(p_household_id, p_list_id, p_versions);
$$;

revoke all on function loom_private.create_short_household_invitation(uuid), public.create_short_household_invitation(uuid),
    loom_private.redeem_household_invitation(text), public.redeem_household_invitation(text),
    loom_private.revoke_household_invitation(uuid), public.revoke_household_invitation(uuid),
    loom_private.remove_completed_shopping_items(uuid, uuid, jsonb), public.remove_completed_shopping_items(uuid, uuid, jsonb)
    from public, anon;
grant execute on function loom_private.create_short_household_invitation(uuid), public.create_short_household_invitation(uuid),
    loom_private.redeem_household_invitation(text), public.redeem_household_invitation(text),
    loom_private.revoke_household_invitation(uuid), public.revoke_household_invitation(uuid),
    loom_private.remove_completed_shopping_items(uuid, uuid, jsonb), public.remove_completed_shopping_items(uuid, uuid, jsonb)
    to authenticated;

commit;
