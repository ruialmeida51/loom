begin;

alter table public.household_members add column version bigint not null default 1;
create function loom_private.bump_household_member_version() returns trigger
language plpgsql set search_path = '' as $$
begin
    new.version := old.version + 1;
    return new;
end;
$$;
create trigger household_member_version before update on public.household_members
    for each row execute function loom_private.bump_household_member_version();

-- Durable response recovery contains identities/results, never invitation plaintext.
create table loom_private.management_operations (
    request_id uuid primary key,
    actor_id uuid not null,
    household_id uuid not null,
    payload jsonb not null,
    result jsonb not null,
    created_at timestamptz not null default now()
);
alter table loom_private.management_operations enable row level security;
revoke all on loom_private.management_operations from public, anon, authenticated, service_role;
create index management_operations_household_idx on loom_private.management_operations(household_id);

create function loom_private.management_retry(p_request_id uuid, p_household_id uuid, p_payload jsonb)
returns jsonb language plpgsql security definer set search_path = '' as $$
declare operation loom_private.management_operations;
begin
    if auth.uid() is null then raise exception 'household.access_denied'; end if;
    if p_request_id is null then raise exception 'household.changed'; end if;
    perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(p_request_id::text, 0));
    select * into operation from loom_private.management_operations where request_id = p_request_id;
    if not found then return null; end if;
    if operation.actor_id <> auth.uid() or operation.household_id <> p_household_id
        or operation.payload <> p_payload then raise exception 'household.changed'; end if;
    return operation.result;
end;
$$;
create function loom_private.complete_management_operation(p_request_id uuid, p_household_id uuid, p_payload jsonb, p_result jsonb)
returns void language sql security definer set search_path = '' as $$
    insert into loom_private.management_operations(request_id, actor_id, household_id, payload, result)
    values(p_request_id, auth.uid(), p_household_id, p_payload, p_result);
$$;
revoke all on function loom_private.management_retry(uuid,uuid,jsonb),
    loom_private.complete_management_operation(uuid,uuid,jsonb,jsonb),
    loom_private.bump_household_member_version() from public,anon,authenticated,service_role;

alter table loom_private.household_invites
    add column reusable boolean not null default false,
    add column secret_id uuid,
    add column version bigint not null default 1;

create function loom_private.release_invitation_secret() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
    if old.secret_id is not null then
        delete from vault.secrets where id = old.secret_id;
    end if;
    return null;
end;
$$;
create trigger release_revoked_invitation after update of revoked_at on loom_private.household_invites
    for each row when (new.revoked_at is not null) execute function loom_private.release_invitation_secret();
create trigger release_deleted_invitation after delete on loom_private.household_invites
    for each row execute function loom_private.release_invitation_secret();
revoke all on function loom_private.release_invitation_secret() from public,anon,authenticated,service_role;

-- Call only after current owner authorization. Vault keeps redisplay codes encrypted at rest.
create function loom_private.invitation_details(p_invitation loom_private.household_invites)
returns jsonb language plpgsql security definer set search_path = '' as $$
declare code text;
begin
    if p_invitation.id is null or p_invitation.revoked_at is not null or p_invitation.expires_at <= now()
        or (not p_invitation.reusable and p_invitation.used_at is not null) then return null; end if;
    if p_invitation.secret_id is not null then
        select decrypted_secret into code from vault.decrypted_secrets where id = p_invitation.secret_id;
    end if;
    return jsonb_build_object('id',p_invitation.id,'version',p_invitation.version,'code',code,
        'expires_at',p_invitation.expires_at,'reusable',p_invitation.reusable);
end;
$$;
revoke all on function loom_private.invitation_details(loom_private.household_invites) from public,anon,authenticated,service_role;

create function loom_private.get_household_management(p_household_id uuid) returns jsonb
language plpgsql security definer set search_path = '' as $$
declare members jsonb; invitation loom_private.household_invites; invite jsonb;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'household.access_denied'; end if;
    select coalesce(jsonb_agg(jsonb_build_object('user_id',m.user_id,'display_name',p.display_name,
        'role',m.role,'active',m.active,'version',m.version) order by m.joined_at),'[]'::jsonb)
        into members from public.household_members m join public.profiles p on p.id = m.user_id
        where m.household_id = p_household_id and m.active;
    if loom_private.is_owner(p_household_id) then
        select * into invitation from loom_private.household_invites where household_id = p_household_id
            and revoked_at is null and expires_at > now() and (reusable or used_at is null)
            order by expires_at desc, id limit 1;
        invite := loom_private.invitation_details(invitation);
    end if;
    return jsonb_build_object('members',members,'invitation',invite);
end;
$$;

create function loom_private.household_member_history(p_household_id uuid)
returns table(user_id uuid,display_name text,role text,active boolean,version bigint)
language plpgsql stable security definer set search_path = '' as $$
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'household.access_denied'; end if;
    return query select m.user_id,p.display_name,m.role,m.active,m.version
        from public.household_members m join public.profiles p on p.id = m.user_id
        where m.household_id = p_household_id order by m.joined_at;
end;
$$;

create function loom_private.manage_household_invitation(p_household_id uuid, p_action text,
    p_invitation_id uuid, p_version bigint, p_request_id uuid) returns jsonb
language plpgsql security definer set search_path = '' as $$
declare invitation loom_private.household_invites; current_invitation loom_private.household_invites;
    payload jsonb := jsonb_build_object('action',p_action,'invitation_id',p_invitation_id,'version',p_version);
    prior jsonb; code text; random_bytes bytea; secret uuid;
    alphabet constant text := 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
begin
    prior := loom_private.management_retry(p_request_id,p_household_id,payload);
    perform 1 from public.households where id = p_household_id for update;
    if auth.uid() is null or not loom_private.is_owner(p_household_id) then raise exception 'household.owner_required'; end if;
    if prior is not null then
        if p_action = 'revoke' then return null; end if;
        select * into invitation from loom_private.household_invites where id = (prior->>'invitation_id')::uuid;
        if loom_private.invitation_details(invitation) is null then raise exception 'household.invitation_changed'; end if;
        return loom_private.invitation_details(invitation);
    end if;
    if p_action is null or p_action not in ('replace','revoke') then raise exception 'household.changed'; end if;
    select * into current_invitation from loom_private.household_invites where household_id = p_household_id
        and revoked_at is null and expires_at > now() and (reusable or used_at is null)
        order by expires_at desc, id limit 1 for update;
    if current_invitation.id is distinct from p_invitation_id or
        (p_invitation_id is not null and current_invitation.version is distinct from p_version) then
        raise exception 'household.invitation_changed';
    end if;
    update loom_private.household_invites set revoked_at = now(), version = version + 1
        where household_id = p_household_id and revoked_at is null and (reusable or used_at is null);
    -- Reclaim expired encrypted secrets when managing this household's invitation.
    delete from vault.secrets where id in(select secret_id from loom_private.household_invites
        where household_id = p_household_id and expires_at <= now());
    if p_action = 'revoke' then
        perform loom_private.complete_management_operation(p_request_id,p_household_id,payload,'{}'::jsonb);
        return null;
    end if;
    for retry in 1..16 loop
        code := '';
        random_bytes := extensions.gen_random_bytes(6);
        for i in 0..5 loop code := code || substr(alphabet,(get_byte(random_bytes,i) % 32)+1,1); end loop;
        begin
            secret := vault.create_secret(code);
            insert into loom_private.household_invites(household_id,token_hash,created_by,expires_at,code_format,reusable,secret_id)
                values(p_household_id,extensions.digest(code,'sha256'),auth.uid(),now()+interval '24 hours','short',true,secret)
                returning * into invitation;
            perform loom_private.complete_management_operation(p_request_id,p_household_id,payload,jsonb_build_object('invitation_id',invitation.id));
            return loom_private.invitation_details(invitation);
        exception when unique_violation then null;
        end;
    end loop;
    raise exception 'household.invitation_failed';
end;
$$;

-- Keep old app versions functional while new clients use frozen invitation identities/versions.
create or replace function loom_private.create_short_household_invitation(p_household_id uuid) returns text
language plpgsql security definer set search_path = '' as $$
declare invitation loom_private.household_invites; result jsonb;
begin
    perform 1 from public.households where id = p_household_id for update;
    if auth.uid() is null or not loom_private.is_owner(p_household_id) then raise exception 'Only the household owner can invite members.'; end if;
    select * into invitation from loom_private.household_invites where household_id = p_household_id
        and revoked_at is null and expires_at > now() and (reusable or used_at is null)
        order by expires_at desc, id limit 1;
    result := loom_private.manage_household_invitation(p_household_id,'replace',invitation.id,invitation.version,gen_random_uuid());
    return result->>'code';
end;
$$;

create function loom_private.change_household_member(p_household_id uuid, p_member_id uuid,
    p_action text, p_version bigint, p_request_id uuid) returns void
language plpgsql security definer set search_path = '' as $$
declare member public.household_members;
    payload jsonb := jsonb_build_object('action',p_action,'member_id',p_member_id,'version',p_version);
begin
    if loom_private.management_retry(p_request_id,p_household_id,payload) is not null then return; end if;
    perform 1 from public.households where id = p_household_id for update;
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'household.access_denied'; end if;
    if p_action is null or p_action not in ('promote','demote','remove','leave') then raise exception 'household.changed'; end if;
    if p_action = 'leave' then
        if p_member_id is distinct from auth.uid() then raise exception 'household.access_denied'; end if;
    elsif not loom_private.is_owner(p_household_id) then raise exception 'household.owner_required';
    elsif p_member_id = auth.uid() then raise exception 'household.changed'; end if;
    select * into member from public.household_members where household_id = p_household_id and user_id = p_member_id for update;
    if not found or not member.active or p_version is null or member.version <> p_version then raise exception 'household.changed'; end if;
    if member.role = 'owner' and p_action in ('demote','remove','leave') and
        (select count(*) from public.household_members where household_id = p_household_id and active and role = 'owner') <= 1 then
        raise exception 'household.last_owner';
    end if;
    if p_action in ('promote','demote') then
        if (p_action = 'promote' and member.role = 'owner') or (p_action = 'demote' and member.role <> 'owner') then raise exception 'household.changed'; end if;
        update public.household_members set role = case when p_action = 'promote' then 'owner' else 'member' end
            where household_id = p_household_id and user_id = p_member_id;
    else
        update public.household_members set active = false where household_id = p_household_id and user_id = p_member_id;
        -- Existing bills keep their payer/split. Never grant former members continued access.
        update loom_private.household_invites set revoked_at = now(), version = version + 1
            where household_id = p_household_id and revoked_at is null and (reusable or used_at is null);
    end if;
    perform loom_private.complete_management_operation(p_request_id,p_household_id,payload,'{}'::jsonb);
end;
$$;

create function loom_private.delete_household(p_household_id uuid, p_name text, p_request_id uuid)
returns void language plpgsql security definer set search_path = '' as $$
declare household public.households; payload jsonb := jsonb_build_object('action','delete','name',p_name);
begin
    if loom_private.management_retry(p_request_id,p_household_id,payload) is not null then return; end if;
    select * into household from public.households where id = p_household_id for update;
    if not found or auth.uid() is null or not loom_private.is_owner(p_household_id) then raise exception 'household.owner_required'; end if;
    if p_name is null or p_name <> household.name then raise exception 'household.name_confirmation'; end if;
    -- Existing receipt-delete triggers enqueue durable object cleanup before the scope disappears.
    delete from public.expense_receipts where household_id = p_household_id;
    delete from public.expense_shares where household_id = p_household_id;
    delete from public.expenses where household_id = p_household_id;
    delete from public.settlements where household_id = p_household_id;
    delete from public.monthly_statements where household_id = p_household_id;
    delete from public.recurring_expenses where household_id = p_household_id;
    delete from public.expense_account_members where household_id = p_household_id;
    delete from public.expense_accounts where household_id = p_household_id;
    delete from public.shopping_items where household_id = p_household_id;
    delete from public.shopping_lists where household_id = p_household_id;
    delete from public.tasks where household_id = p_household_id;
    delete from public.feed_subscriptions where household_id = p_household_id;
    delete from public.notifications where household_id = p_household_id;
    delete from loom_private.statement_runs where household_id = p_household_id;
    delete from loom_private.household_invites where household_id = p_household_id;
    delete from public.household_members where household_id = p_household_id;
    delete from public.households where id = p_household_id;
    perform loom_private.complete_management_operation(p_request_id,p_household_id,payload,'{}'::jsonb);
end;
$$;

create function public.get_household_management(p_household_id uuid) returns jsonb
language sql security invoker set search_path = '' as $$select loom_private.get_household_management(p_household_id);$$;
create function public.household_member_history(p_household_id uuid)
returns table(user_id uuid,display_name text,role text,active boolean,version bigint)
language sql stable security invoker set search_path = '' as $$select * from loom_private.household_member_history(p_household_id);$$;
create function public.manage_household_invitation(p_household_id uuid, p_action text, p_invitation_id uuid default null, p_version bigint default null, p_request_id uuid default null)
returns jsonb language sql security invoker set search_path = '' as $$
    select loom_private.manage_household_invitation(p_household_id,p_action,p_invitation_id,p_version,p_request_id);
$$;
create function public.change_household_member(p_household_id uuid, p_member_id uuid, p_action text, p_version bigint, p_request_id uuid)
returns void language sql security invoker set search_path = '' as $$
    select loom_private.change_household_member(p_household_id,p_member_id,p_action,p_version,p_request_id);
$$;
create function public.delete_household(p_household_id uuid, p_name text, p_request_id uuid)
returns void language sql security invoker set search_path = '' as $$select loom_private.delete_household(p_household_id,p_name,p_request_id);$$;

-- Expose only authenticated invoker wrappers; their private definers always recheck the actor.
do $$
declare routine record;
begin
    for routine in select n.nspname,p.proname,pg_get_function_identity_arguments(p.oid) args
        from pg_proc p join pg_namespace n on n.oid = p.pronamespace
        where n.nspname in ('public','loom_private') and p.proname in
            ('get_household_management','household_member_history','manage_household_invitation','change_household_member','delete_household')
    loop
        execute format('revoke all on function %I.%I(%s) from public,anon,authenticated,service_role',routine.nspname,routine.proname,routine.args);
        execute format('grant execute on function %I.%I(%s) to authenticated',routine.nspname,routine.proname,routine.args);
    end loop;
end;
$$;

create or replace function loom_private.redeem_household_invitation(p_token text) returns jsonb
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
    if not found or (not invitation.reusable and invitation.used_at is not null) or invitation.revoked_at is not null or invitation.expires_at <= now() then
        return jsonb_build_object('status', 'invalid');
    end if;
    if not invitation.reusable and not exists(select 1 from public.household_members where household_id = invitation.household_id
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
    if not invitation.reusable then
        update loom_private.household_invites set used_at = now(), used_by = auth.uid() where id = invitation.id;
    end if;
    return jsonb_build_object('status', 'joined');
end;
$$;


-- Expenses owns these account/currency positions; household confirmations compose the preview.
create function loom_private.member_account_positions(p_household_id uuid, p_member_id uuid)
returns jsonb language plpgsql stable security definer set search_path = '' as $$
declare positions jsonb;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) or
        (p_member_id is distinct from auth.uid() and not loom_private.is_owner(p_household_id)) then
        raise exception 'expenses.account_access_denied';
    end if;
    select coalesce(jsonb_agg(jsonb_build_object('account_id',a.id,'account_name',a.name,
        'is_default',a.is_default,'currency',a.currency,
        'balance_minor',coalesce((select sum(l.delta_minor) from public.account_ledger_entries l
            where l.household_id = p_household_id and l.account_id = a.id and l.user_id = p_member_id),0))
        order by a.is_default desc,a.name,a.id),'[]'::jsonb) into positions
        from public.expense_accounts a where a.household_id = p_household_id and
        (exists(select 1 from public.expense_account_members m where m.account_id = a.id and m.user_id = p_member_id)
         or exists(select 1 from public.account_ledger_entries l where l.account_id = a.id and l.user_id = p_member_id));
    return positions;
end;
$$;
create function public.member_account_positions(p_household_id uuid,p_member_id uuid) returns jsonb
language sql stable security invoker set search_path = '' as $$select loom_private.member_account_positions(p_household_id,p_member_id);$$;
revoke all on function loom_private.member_account_positions(uuid,uuid),public.member_account_positions(uuid,uuid) from public,anon,service_role;
grant execute on function loom_private.member_account_positions(uuid,uuid),public.member_account_positions(uuid,uuid) to authenticated;

create or replace function loom_private.record_account_settlement(
    p_id uuid, p_household_id uuid, p_account_id uuid, p_from uuid, p_to uuid, p_amount_minor bigint, p_occurred_on date
) returns uuid language plpgsql security definer set search_path = '' as $$
declare existing public.settlements;
begin
    perform 1 from public.households where id = p_household_id for update;
    if auth.uid() is null or not loom_private.is_member(p_household_id) or auth.uid() not in (p_from, p_to) or not exists (
        select 1 from public.expense_accounts where id = p_account_id and household_id = p_household_id
    ) then raise exception 'You can only record repayments you send or receive.'; end if;
    if p_from is null or p_to is null or p_from = p_to or p_amount_minor is null or p_amount_minor not between 1 and 100000000000 or
       p_occurred_on is null or p_occurred_on > (select (now() at time zone time_zone)::date from public.households where id = p_household_id) then
        raise exception 'Choose two people and a positive amount.';
    end if;
    if (select count(*) from public.expense_account_members am join public.household_members m
        on m.household_id = am.household_id and m.user_id = am.user_id
        where am.account_id = p_account_id and am.user_id in (p_from, p_to) and (m.active or exists(select 1 from public.account_ledger_entries history
            where history.account_id = p_account_id and history.household_id = p_household_id and history.user_id = m.user_id))) <> 2 then
        raise exception 'expenses.invalid_account_members';
    end if;
    perform 1 from public.expense_accounts where id = p_account_id for no key update;
    perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(p_id::text, 0));
    select * into existing from public.settlements where id = p_id;
    if found then
        if existing.household_id = p_household_id and existing.account_id = p_account_id and existing.created_by = auth.uid() and
           existing.from_user = p_from and existing.to_user = p_to and existing.amount_minor = p_amount_minor and existing.occurred_on = p_occurred_on then return p_id; end if;
        raise exception 'This repayment ID has already been used.';
    end if;
    insert into public.settlements(id, household_id, account_id, from_user, to_user, amount_minor, occurred_on, created_by)
    values (p_id, p_household_id, p_account_id, p_from, p_to, p_amount_minor, p_occurred_on, auth.uid());
    return p_id;
end;
$$;

create or replace function loom_private.save_account_settlement(
    p_household_id uuid, p_account_id uuid, p_id uuid, p_from uuid, p_to uuid,
    p_amount_minor bigint, p_occurred_on date, p_version bigint
) returns public.settlements language plpgsql security definer set search_path = '' as $$
declare existing public.settlements; result public.settlements;
begin
    perform 1 from public.households where id = p_household_id for update;
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'expenses.payment_permission_denied'; end if;
    perform 1 from public.expense_accounts where id = p_account_id and household_id = p_household_id and not archived for no key update;
    if not found then raise exception 'expenses.account_archived'; end if;
    select * into existing from public.settlements where id = p_id and household_id = p_household_id and account_id = p_account_id for update;
    if not found or existing.voided_at is not null then raise exception 'expenses.payment_changed'; end if;
    if auth.uid() not in (existing.from_user, existing.to_user, existing.created_by) and not loom_private.is_owner(p_household_id) then
        raise exception 'expenses.payment_permission_denied';
    end if;
    if p_from is null or p_to is null or p_from = p_to or p_amount_minor is null or p_amount_minor not between 1 and 100000000000 or
       p_occurred_on is null or p_occurred_on > (select (now() at time zone time_zone)::date from public.households where id = p_household_id) then
        raise exception 'Choose two people and a positive amount.';
    end if;
    if (select count(*) from public.expense_account_members a join public.household_members m
        on m.household_id = a.household_id and m.user_id = a.user_id
        where a.account_id = p_account_id and a.user_id in (p_from, p_to) and (m.active or exists(select 1 from public.account_ledger_entries history
            where history.account_id = p_account_id and history.household_id = p_household_id and history.user_id = m.user_id))) <> 2 then
        raise exception 'expenses.invalid_account_members';
    end if;
    if (existing.from_user, existing.to_user, existing.amount_minor, existing.occurred_on) is not distinct from
       (p_from, p_to, p_amount_minor, p_occurred_on) then return existing; end if;
    if existing.version is distinct from p_version then raise exception 'expenses.payment_changed'; end if;
    update public.settlements set from_user = p_from, to_user = p_to, amount_minor = p_amount_minor,
        occurred_on = p_occurred_on, version = version + 1 where id = p_id returning * into result;
    return result;
end;
$$;


create or replace function loom_private.validate_retained_account_expense(
    p_household_id uuid, p_account_id uuid, p_title text, p_amount_minor bigint,
    p_paid_by uuid, p_occurred_on date, p_shares jsonb, p_existing_id uuid
) returns void language plpgsql security definer set search_path = '' as $$
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) or not exists (
        select 1 from public.expense_account_members where account_id = p_account_id and user_id = auth.uid()
    ) then raise exception 'expenses.account_access_denied'; end if;
    if not exists (select 1 from public.expense_accounts where id = p_account_id and household_id = p_household_id and not archived) then
        raise exception 'expenses.account_archived';
    end if;
    if p_title is null or length(trim(p_title)) not between 1 and 160 then raise exception 'expenses.invalid_title'; end if;
    if p_amount_minor is null or p_amount_minor not between 1 and 100000000000 or
       p_shares is null or jsonb_typeof(p_shares) <> 'object' then raise exception 'Invalid expense amount or shares.'; end if;
    if p_occurred_on is null or p_occurred_on > (select (now() at time zone time_zone)::date from public.households where id = p_household_id) then
        raise exception 'expenses.message_payment_date_future';
    end if;
    if (select count(*) from jsonb_each(p_shares)) not between 1 and 100 then raise exception 'Choose who to split with.'; end if;
    if exists (select 1 from jsonb_each(p_shares) s where jsonb_typeof(s.value) <> 'number' or s.value::text !~ '^[0-9]+$') then
        raise exception 'Shares must be integer minor units.';
    end if;
    if (select sum(value::text::numeric) from jsonb_each(p_shares)) <> p_amount_minor or
       exists (select 1 from jsonb_each(p_shares) where value::text::numeric > p_amount_minor) then
        raise exception 'Shares must equal the expense total.';
    end if;
    if not exists (select 1 from public.expense_account_members am join public.household_members m
        on m.household_id = am.household_id and m.user_id = am.user_id
        where am.account_id = p_account_id and am.user_id = p_paid_by and (m.active or exists(
            select 1 from public.expenses original where original.id = p_existing_id and original.household_id = p_household_id
                and original.account_id = p_account_id and original.paid_by = p_paid_by))) or
       exists (select 1 from jsonb_each(p_shares) s where not exists (
           select 1 from public.expense_account_members am join public.household_members m
           on m.household_id = am.household_id and m.user_id = am.user_id
           where am.account_id = p_account_id and am.user_id = s.key::uuid and (m.active or exists(
               select 1 from public.expense_shares old_share join public.expenses original on original.id = old_share.expense_id
               where original.id = p_existing_id and original.household_id = p_household_id and original.account_id = p_account_id
                   and old_share.user_id = s.key::uuid))
       )) then raise exception 'expenses.invalid_account_members'; end if;
end;
$$;
revoke all on function loom_private.validate_retained_account_expense(uuid,uuid,text,bigint,uuid,date,jsonb,uuid) from public,anon,authenticated,service_role;

create or replace function loom_private.save_account_expense(
    p_id uuid, p_household_id uuid, p_account_id uuid, p_title text, p_amount_minor bigint,
    p_paid_by uuid, p_category text, p_occurred_on date, p_recurring boolean, p_shares jsonb,
    p_version bigint, p_split_method text, p_split_values jsonb
) returns uuid language plpgsql security definer set search_path = '' as $$
declare existing public.expenses; rule_id uuid;
begin
    perform 1 from public.households where id = p_household_id for update;
    if auth.uid() is null or not loom_private.is_member(p_household_id) then
        raise exception 'expenses.account_access_denied';
    end if;
    perform 1 from public.expense_accounts where id = p_account_id and household_id = p_household_id for no key update;
    perform loom_private.validate_retained_account_expense(p_household_id, p_account_id, p_title, p_amount_minor, p_paid_by, p_occurred_on, p_shares, case when p_version > 0 then p_id else null end);
    if p_version is null or p_version < 0 or p_split_method is null or
       p_split_method not in ('equal', 'shares', 'amounts', 'percentages') or
       p_split_values is null or jsonb_typeof(p_split_values) <> 'object' then raise exception 'expenses.invalid_split'; end if;
    perform loom_private.validate_expense_split(p_amount_minor, p_shares, p_split_method, p_split_values);
    perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(p_id::text, 0));
    select * into existing from public.expenses where id = p_id for update;
    if found then
        if existing.household_id <> p_household_id or existing.account_id <> p_account_id or existing.voided_at is not null then
            raise exception 'expenses.record_changed';
        end if;
        if p_version = 0 and existing.created_by = auth.uid() and existing.title = trim(p_title) and
           existing.amount_minor = p_amount_minor and existing.paid_by = p_paid_by and existing.category = p_category and
           existing.occurred_on = p_occurred_on and (existing.recurring_rule_id is not null) = p_recurring and
           existing.split_method = p_split_method and existing.split_values = p_split_values and
           (select jsonb_object_agg(user_id::text, amount_minor) from public.expense_shares where expense_id = p_id) = p_shares then
            return p_id;
        end if;
        if auth.uid() not in (existing.created_by, existing.paid_by) and not loom_private.is_owner(p_household_id) then
            raise exception 'expenses.account_access_denied';
        end if;
        if existing.version <> p_version or (existing.recurring_rule_id is not null) <> p_recurring then
            raise exception 'expenses.record_changed';
        end if;
        update public.expenses set title = trim(p_title), amount_minor = p_amount_minor, paid_by = p_paid_by,
            category = p_category, occurred_on = p_occurred_on, split_method = p_split_method,
            split_values = p_split_values, version = version + 1 where id = p_id;
        delete from public.expense_shares where expense_id = p_id;
    else
        if p_version <> 0 then raise exception 'expenses.record_changed'; end if;
        if p_recurring then
            insert into public.recurring_expenses(household_id, account_id, title, amount_minor, paid_by, category,
                shares, anchor_day, next_on, created_by, split_method, split_values)
            values (p_household_id, p_account_id, trim(p_title), p_amount_minor, p_paid_by, p_category, p_shares,
                extract(day from p_occurred_on)::integer,
                loom_private.next_month_date(p_occurred_on, extract(day from p_occurred_on)::integer), auth.uid(), p_split_method, p_split_values)
            returning id into rule_id;
        end if;
        insert into public.expenses(id, household_id, account_id, title, amount_minor, paid_by, category,
            occurred_on, recurring_rule_id, created_by, split_method, split_values)
        values (p_id, p_household_id, p_account_id, trim(p_title), p_amount_minor, p_paid_by, p_category,
            p_occurred_on, rule_id, auth.uid(), p_split_method, p_split_values);
        insert into public.notifications(household_id, user_id, event_key, title, body, data)
        select p_household_id, m.user_id, 'expense:' || p_id, 'A new shared expense',
            'There’s a new expense in your household.',
            jsonb_build_object('route', 'money', 'household_id', p_household_id, 'account_id', p_account_id)
        from public.expense_account_members m join public.household_members hm
            on hm.household_id = m.household_id and hm.user_id = m.user_id
        where m.account_id = p_account_id and hm.active and m.user_id <> auth.uid()
        on conflict(user_id, event_key) do nothing;
    end if;
    insert into public.expense_shares(expense_id, household_id, user_id, amount_minor)
    select p_id, p_household_id, key::uuid, value::text::bigint from jsonb_each(p_shares);
    return p_id;
end;
$$;

create or replace function loom_private.save_expense_account(
    p_household_id uuid, p_id uuid, p_name text, p_currency text,
    p_member_ids uuid[], p_archived boolean, p_version bigint
) returns uuid language plpgsql security definer set search_path = '' as $$
declare account public.expense_accounts;
begin
    perform 1 from public.households where id = p_household_id for update;
    if auth.uid() is null or not loom_private.is_member(p_household_id) then
        raise exception 'expenses.account_access_denied';
    end if;
    if p_name is null or length(trim(p_name)) not between 1 and 80 or
       p_currency is null or p_currency not in ('EUR', 'GBP', 'USD', 'CAD', 'AUD') then
        raise exception 'expenses.invalid_account';
    end if;
    perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(p_id::text, 0));
    select * into account from public.expense_accounts where id = p_id for no key update;
    if found then
        if account.household_id <> p_household_id or
           (account.created_by <> auth.uid() and not loom_private.is_owner(p_household_id)) then
            raise exception 'expenses.account_access_denied';
        end if;
        if p_version = 0 and account.created_by = auth.uid() and account.name = trim(p_name) and
           account.currency = p_currency and not account.archived and not p_archived and
           (select array_agg(user_id order by user_id) from public.expense_account_members where account_id = p_id) =
           (select array_agg(member order by member) from unnest(p_member_ids) as member) then return p_id; end if;
        if account.is_default or account.currency <> p_currency then raise exception 'expenses.invalid_account'; end if;
        if account.version <> p_version then raise exception 'expenses.record_changed'; end if;
        if (select array_agg(user_id order by user_id) from public.expense_account_members where account_id = p_id)
            is distinct from (select array_agg(member order by member) from unnest(p_member_ids) as member) then
            raise exception 'expenses.invalid_account_members';
        end if;
        update public.expense_accounts set name = trim(p_name), archived = p_archived, version = version + 1 where id = p_id;
        if p_archived then update public.recurring_expenses set active = false where account_id = p_id and active; end if;
        return p_id;
    end if;
    if p_version <> 0 or p_archived or p_member_ids is null or cardinality(p_member_ids) not between 1 and 100 or
       not (auth.uid() = any(p_member_ids)) or
       (select count(distinct member) from unnest(p_member_ids) as member) <> cardinality(p_member_ids) or
       exists (select 1 from unnest(p_member_ids) as member where not exists (
           select 1 from public.household_members m where m.household_id = p_household_id and m.user_id = member and (m.active or (p_version > 0 and exists(select 1 from public.expense_account_members old_member where old_member.account_id = p_id and old_member.household_id = p_household_id and old_member.user_id = member)))
       )) then raise exception 'expenses.invalid_account_members'; end if;
    insert into public.expense_accounts(id, household_id, name, currency, created_by)
    values (p_id, p_household_id, trim(p_name), p_currency, auth.uid());
    insert into public.expense_account_members(account_id, household_id, user_id)
    select p_id, p_household_id, member from unnest(p_member_ids) as member;
    return p_id;
end;
$$;

commit;
