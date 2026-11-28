-- Household details listed members without their profile pictures, so everyone showed as initials
-- there while every other list carried avatar_path. Same function, now with the avatar.
create or replace function loom_private.get_household_management(p_household_id uuid) returns jsonb
language plpgsql security definer set search_path = '' as $$
declare members jsonb; invitation loom_private.household_invites; invite jsonb;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'household.access_denied'; end if;
    select coalesce(jsonb_agg(jsonb_build_object('user_id',m.user_id,'display_name',p.display_name,
        'role',m.role,'active',m.active,'version',m.version,'avatar_path',p.avatar_path) order by m.joined_at),'[]'::jsonb)
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
