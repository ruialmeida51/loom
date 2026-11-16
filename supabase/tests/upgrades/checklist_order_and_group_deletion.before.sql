-- An already archived list must survive the ordering backfill without metadata/version writes.
insert into auth.users(id, email) values ('00000000-0000-4000-8000-000000000078', 'upgrade-only@example.test');
insert into public.households(id, name, currency, time_zone, created_by)
values ('00000000-0000-4000-8000-000000000079', 'Upgrade fixture', 'EUR', 'UTC', '00000000-0000-4000-8000-000000000078');
insert into public.household_members(household_id, user_id, role)
values ('00000000-0000-4000-8000-000000000079', '00000000-0000-4000-8000-000000000078', 'owner');
insert into public.shopping_lists(id, household_id, name)
values ('00000000-0000-4000-8000-000000000080', '00000000-0000-4000-8000-000000000079', 'Old holiday');
insert into public.shopping_items(id, household_id, list_id, name, created_by)
values ('00000000-0000-4000-8000-000000000081', '00000000-0000-4000-8000-000000000079',
    '00000000-0000-4000-8000-000000000080', 'Stored item', '00000000-0000-4000-8000-000000000078');
update public.shopping_lists set archived = true where id = '00000000-0000-4000-8000-000000000080';
