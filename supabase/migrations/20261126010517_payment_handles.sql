begin;

-- How someone likes to be paid back: an MB Way phone number, a Revolut username and a PayPal.me
-- username, each optional. Only the person edits theirs; only people who currently share a
-- household with them can read it, so leaving a household stops sharing it.
create table public.payment_handles (
    user_id uuid primary key references public.profiles(id) on delete cascade,
    mbway_phone text check (mbway_phone ~ '^\+[1-9][0-9]{7,14}$'),
    revolut_username text check (revolut_username ~ '^[a-z0-9][a-z0-9._-]{2,31}$'),
    paypal_username text check (paypal_username ~ '^[A-Za-z0-9]{1,20}$'),
    updated_at timestamptz not null default now()
);
alter table public.payment_handles enable row level security;
revoke all on public.payment_handles from public, anon, authenticated;
grant select, insert, update, delete on public.payment_handles to authenticated;
grant all on public.payment_handles to service_role;

create function loom_private.shares_active_household(p_user_id uuid) returns boolean
language sql stable security definer set search_path = '' as $$
    select auth.uid() is not null and (p_user_id = (select auth.uid()) or exists (
        select 1 from public.household_members mine
        join public.household_members other on other.household_id = mine.household_id
        where mine.user_id = (select auth.uid()) and mine.active and other.user_id = p_user_id and other.active
    ));
$$;
revoke all on function loom_private.shares_active_household(uuid) from public, anon;
grant execute on function loom_private.shares_active_household(uuid) to authenticated;

create policy payment_handles_read on public.payment_handles for select to authenticated
    using (loom_private.shares_active_household(user_id));
create policy payment_handles_insert on public.payment_handles for insert to authenticated
    with check (user_id = (select auth.uid()));
create policy payment_handles_update on public.payment_handles for update to authenticated
    using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));
create policy payment_handles_delete on public.payment_handles for delete to authenticated
    using (user_id = (select auth.uid()));

commit;
