-- Stories keep the picture their feed offers, so the news list and Home can show it.
begin;

alter table public.feed_articles
    add column image_url text
    check (image_url is null or (image_url like 'https://%' and length(image_url) <= 2048));

-- Listed explicitly: `a.*` would put the new column before is_read, which a replace cannot do.
create or replace view public.feed_articles_with_state with (security_invoker = true) as
select a.id, a.subscription_id, a.household_id, a.guid, a.title, a.summary, a.url, a.source,
       a.published_at, coalesce(r.is_read, false) as is_read, a.image_url
from public.feed_articles a
left join public.article_reads r on r.article_id = a.id and r.user_id = (select auth.uid());

-- Stories saved before pictures were kept pick theirs up on the next refresh. Only empty pictures
-- are filled, so a refresh does not rewrite rows (or wake every reader) when nothing changed.
-- Called only by the refresh-feed function. It runs with its caller's rights (the service role
-- may already write stories), so it grants nothing; members cannot execute it at all.
create function public.fill_article_images(p_subscription_id uuid, p_images jsonb)
returns integer
language sql
security invoker
set search_path = ''
as $$
    with filled as (
        update public.feed_articles a
        set image_url = i.image_url
        from jsonb_to_recordset(p_images) as i(guid text, image_url text)
        where a.subscription_id = p_subscription_id
          and a.guid = i.guid
          and a.image_url is null
          and i.image_url like 'https://%'
          and length(i.image_url) <= 2048
        returning 1
    )
    select count(*)::integer from filled;
$$;

revoke all on function public.fill_article_images(uuid, jsonb) from public, anon, authenticated;
grant execute on function public.fill_article_images(uuid, jsonb) to service_role;

commit;
