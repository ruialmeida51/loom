insert into auth.users(id,email) values
('00000000-0000-4000-8000-000000000291','legacy-owner@example.test'),
('00000000-0000-4000-8000-000000000292','legacy-member@example.test');
insert into public.households(id,name,currency,time_zone,created_by)
values('00000000-0000-4000-8000-000000000293','Legacy invitation home','EUR','UTC','00000000-0000-4000-8000-000000000291');
insert into public.household_members(household_id,user_id,role)
values('00000000-0000-4000-8000-000000000293','00000000-0000-4000-8000-000000000291','owner');
insert into loom_private.household_invites(household_id,token_hash,created_by)
values('00000000-0000-4000-8000-000000000293',extensions.digest(repeat('a',64),'sha256'),'00000000-0000-4000-8000-000000000291');
