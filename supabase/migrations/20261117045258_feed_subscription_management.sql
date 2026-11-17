-- Feed editing is transactional: stale editors cannot replace a newer URL, and old articles
-- never masquerade as content from the new source. Household membership authorizes shared feeds.
create function loom_private.update_feed_subscription(
    p_household_id uuid, p_id uuid, p_expected_url text, p_new_id uuid, p_url text
) returns void language plpgsql security definer set search_path = '' as $$
declare target public.feed_subscriptions;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then
        raise exception 'Household access denied.';
    end if;
    select * into target from public.feed_subscriptions where id = p_id for update;
    if not found or target.household_id <> p_household_id then
        raise exception 'feed.subscription_not_found';
    end if;
    if p_expected_url is null or target.url <> p_expected_url then
        raise exception 'feed.subscription_changed';
    end if;
    if p_url is null or length(p_url) > 2048 or p_url !~ '^https://[^[:space:]]+$' then
        raise exception 'feed.invalid_url';
    end if;
    if p_new_id is null or p_new_id = p_id then raise exception 'feed.invalid_url'; end if;
    -- A replacement identity makes any in-flight worker for the old URL fail its foreign key.
    -- Updating the URL in place would allow that worker to publish old articles under the new URL.
    delete from public.feed_subscriptions where id = p_id and household_id = p_household_id;
    insert into public.feed_subscriptions(id, household_id, url, created_by)
    values(p_new_id, p_household_id, p_url, auth.uid());
end;
$$;
create function public.update_feed_subscription(p_household_id uuid, p_id uuid, p_expected_url text, p_new_id uuid, p_url text)
returns void language sql security invoker set search_path = '' as $$
    select loom_private.update_feed_subscription(p_household_id, p_id, p_expected_url, p_new_id, p_url);
$$;
revoke all on function loom_private.update_feed_subscription(uuid, uuid, text, uuid, text) from public, anon;
revoke all on function public.update_feed_subscription(uuid, uuid, text, uuid, text) from public, anon;
grant execute on function loom_private.update_feed_subscription(uuid, uuid, text, uuid, text) to authenticated;
grant execute on function public.update_feed_subscription(uuid, uuid, text, uuid, text) to authenticated;

do $$ begin
    if exists(select 1 from pg_publication where pubname = 'supabase_realtime') and not exists (
        select 1 from pg_publication_tables where pubname = 'supabase_realtime'
        and schemaname = 'public' and tablename = 'feed_subscriptions'
    ) then alter publication supabase_realtime add table public.feed_subscriptions; end if;
end $$;
