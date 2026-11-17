-- Stable cursors avoid scanning every previous page and disambiguate equal publisher timestamps.
create index feed_articles_household_cursor_idx
    on public.feed_articles(household_id,published_at desc,id desc);
create index feed_articles_source_cursor_idx
    on public.feed_articles(household_id,subscription_id,published_at desc,id desc);

-- Show the publisher's actual title without copying user data or adding a second source of truth.
-- Both outer and inner reads use the caller's existing household policies.
create view public.feed_subscriptions_with_publisher with (security_invoker=true) as
select s.*,(
    select a.source from public.feed_articles a
    where a.subscription_id=s.id and a.household_id=s.household_id
    order by a.published_at desc,a.id desc limit 1
) as publisher
from public.feed_subscriptions s;
revoke all on public.feed_subscriptions_with_publisher from public,anon;
grant select on public.feed_subscriptions_with_publisher to authenticated,service_role;
