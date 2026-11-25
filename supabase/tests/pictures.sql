begin;
insert into auth.users(id,email) values
 ('00000000-0000-4000-8000-000000000c01','pic-owner@example.test'),
 ('00000000-0000-4000-8000-000000000c02','pic-member@example.test'),
 ('00000000-0000-4000-8000-000000000c03','pic-outsider@example.test');
insert into public.profiles(id,display_name) values
 ('00000000-0000-4000-8000-000000000c01','Owner'),('00000000-0000-4000-8000-000000000c02','Member'),
 ('00000000-0000-4000-8000-000000000c03','Outsider') on conflict (id) do nothing;
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000c01';
select set_config('loom.pic.home',public.create_household('Picture home','EUR','UTC')::text,true);
reset role;
insert into public.household_members(household_id,user_id,role)
 values (current_setting('loom.pic.home')::uuid,'00000000-0000-4000-8000-000000000c02','member');
do $$
declare
 home text := current_setting('loom.pic.home');
 owner_id text := '00000000-0000-4000-8000-000000000c01';
 first_avatar text := 'profiles/'||owner_id||'/00000000-0000-4000-8000-000000000a01.jpg';
 second_avatar text := 'profiles/'||owner_id||'/00000000-0000-4000-8000-000000000a02.jpg';
 home_picture text := 'households/'||home||'/00000000-0000-4000-8000-000000000a03.jpg';
begin
 insert into storage.objects(bucket_id,name,owner_id) values
  ('pictures',first_avatar,owner_id),('pictures',second_avatar,owner_id),('pictures',home_picture,owner_id);
 perform set_config('role','authenticated',true);
 perform set_config('request.jwt.claim.sub',owner_id,true);
 assert public.set_profile_picture(first_avatar) = first_avatar, 'An uploaded avatar is linked';
 assert public.set_profile_picture(second_avatar) = second_avatar, 'A new avatar replaces it';
 perform set_config('role','postgres',true);
 assert exists(select 1 from loom_private.picture_deletions where object_path = first_avatar),
  'The replaced avatar is queued for deletion, so only one is kept';
 assert not exists(select 1 from loom_private.picture_deletions where object_path = second_avatar);
 perform set_config('role','authenticated',true);
 assert public.set_household_picture(home::uuid, home_picture) = home_picture, 'Members set the household picture';
 begin
  perform public.set_profile_picture(home_picture);
  raise exception 'A household path was accepted as an avatar';
 exception when raise_exception then if sqlerrm <> 'pictures.invalid_picture' then raise; end if; end;
 begin
  update public.profiles set avatar_path = home_picture where id = owner_id::uuid;
  raise exception 'A direct update bypassed the picture functions';
 exception
  when insufficient_privilege then null;
  when raise_exception then if sqlerrm <> 'pictures.access_denied' then raise; end if;
 end;
 -- A household member can read the avatar and household picture; an outsider cannot.
 perform set_config('request.jwt.claim.sub','00000000-0000-4000-8000-000000000c02',true);
 assert loom_private.can_read_picture(second_avatar) and loom_private.can_read_picture(home_picture);
 assert not loom_private.can_read_picture(first_avatar), 'The replaced avatar is no longer readable';
 assert (select avatar_path from public.household_members_with_names(home::uuid) where user_id = owner_id::uuid) = second_avatar,
  'Member lists carry avatars';
 begin
  perform public.set_household_picture(home::uuid, 'households/'||home||'/00000000-0000-4000-8000-000000000a09.jpg');
  raise exception 'A picture that was never uploaded was accepted';
 exception when raise_exception then if sqlerrm <> 'pictures.invalid_picture' then raise; end if; end;
 perform set_config('request.jwt.claim.sub','00000000-0000-4000-8000-000000000c03',true);
 assert not loom_private.can_read_picture(second_avatar) and not loom_private.can_read_picture(home_picture),
  'Outsiders cannot read pictures';
 begin
  perform public.set_household_picture(home::uuid, null);
  raise exception 'An outsider changed the household picture';
 exception when raise_exception then if sqlerrm <> 'pictures.access_denied' then raise; end if; end;
 -- Clearing queues the last picture too.
 perform set_config('request.jwt.claim.sub',owner_id,true);
 assert public.set_profile_picture(null) is null;
 perform set_config('role','postgres',true);
 assert exists(select 1 from loom_private.picture_deletions where object_path = second_avatar);
 assert (select count(*) from public.claim_picture_deletions(10)) = 2, 'The worker claims queued pictures';
end;$$;
reset role;
rollback;
