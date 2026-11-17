begin;
insert into auth.users(id,email) values
('00000000-0000-4000-8000-000000009001','feed-pages-owner@example.test'),
('00000000-0000-4000-8000-000000009002','feed-pages-outsider@example.test');
set role authenticated;
set request.jwt.claim.sub='00000000-0000-4000-8000-000000009001';
select set_config('loom.test.feed_pages_home',public.create_household('Feed pages','EUR','Europe/Lisbon')::text,true);
insert into public.feed_subscriptions(id,household_id,url,created_by) values
('00000000-0000-4000-8000-000000009011',current_setting('loom.test.feed_pages_home')::uuid,'https://feeds.bbci.co.uk/news/world/rss.xml',auth.uid()),
('00000000-0000-4000-8000-000000009012',current_setting('loom.test.feed_pages_home')::uuid,'https://feeds.bbci.co.uk/news/business/rss.xml',auth.uid());
reset role;
insert into public.feed_articles(id,subscription_id,household_id,guid,title,summary,url,source,published_at)
select ('00000000-0000-4000-8000-'||lpad(n::text,12,'0'))::uuid,
    case when n%2=0 then '00000000-0000-4000-8000-000000009011' else '00000000-0000-4000-8000-000000009012' end::uuid,
    current_setting('loom.test.feed_pages_home')::uuid,'story-'||n,'Story '||n,'Summary','https://example.test/story/'||n,
    case when n%2=0 then 'BBC World' else 'BBC Business' end,'2026-11-17T12:00:00Z'::timestamptz
from generate_series(1,45) as n;
set role authenticated;
insert into public.article_reads(article_id,user_id,is_read) values
('00000000-0000-4000-8000-000000000045',auth.uid(),true),
('00000000-0000-4000-8000-000000000043',auth.uid(),true);
do $$declare first_ids uuid[]; second_ids uuid[]; unread_ids uuid[]; begin
    select array_agg(id order by published_at desc,id desc) into first_ids from (
        select id,published_at from public.feed_articles_with_state
        where household_id=current_setting('loom.test.feed_pages_home')::uuid
        order by published_at desc,id desc limit 20
    ) as first_page;
    select array_agg(id order by published_at desc,id desc) into second_ids from (
        select id,published_at from public.feed_articles_with_state
        where household_id=current_setting('loom.test.feed_pages_home')::uuid
          and (published_at<'2026-11-17T12:00:00Z' or (published_at='2026-11-17T12:00:00Z' and id<first_ids[20]))
        order by published_at desc,id desc limit 20
    ) as second_page;
    assert cardinality(first_ids)=20 and cardinality(second_ids)=20,'Pages are bounded';
    assert not first_ids&&second_ids,'Equal timestamps do not duplicate page-boundary stories';
    assert first_ids[20]='00000000-0000-4000-8000-000000000026'::uuid,'Stable ID ordering resolves ties';
    assert second_ids[1]='00000000-0000-4000-8000-000000000025'::uuid,'Second page starts strictly after the cursor';
    select array_agg(id order by published_at desc,id desc) into unread_ids from (
        select id,published_at from public.feed_articles_with_state
        where household_id=current_setting('loom.test.feed_pages_home')::uuid
          and subscription_id='00000000-0000-4000-8000-000000009012'::uuid and not is_read
        order by published_at desc,id desc limit 20
    ) as filtered_page;
    assert cardinality(unread_ids)=20,'Unread/source filters apply before the page limit';
    assert not ('00000000-0000-4000-8000-000000000045'::uuid=any(unread_ids)),'Read stories are excluded';
    assert (select count(*) from public.feed_articles_with_state where household_id=current_setting('loom.test.feed_pages_home')::uuid and subscription_id in('00000000-0000-4000-8000-000000009011','00000000-0000-4000-8000-000000009012'))=45,'Multiple selected feeds form a union';
    assert (select publisher='BBC World' from public.feed_subscriptions_with_publisher where id='00000000-0000-4000-8000-000000009011'),'Publisher labels distinguish feeds on the same host';
    assert (select publisher='BBC Business' from public.feed_subscriptions_with_publisher where id='00000000-0000-4000-8000-000000009012'),'Each source keeps its publisher title';
end;$$;
set request.jwt.claim.sub='00000000-0000-4000-8000-000000009002';
do $$begin
    assert (select count(*) from public.feed_subscriptions_with_publisher)=0,'Publisher view cannot bypass source RLS';
    assert (select count(*) from public.feed_articles_with_state)=0,'Paged articles retain household isolation';
end;$$;
reset role;
do $$begin
    assert (select 'security_invoker=true'=any(reloptions) from pg_class where oid='public.feed_subscriptions_with_publisher'::regclass),'Publisher view uses caller security';
    assert not has_table_privilege('anon','public.feed_subscriptions_with_publisher','select'),'No anonymous publisher access';
    assert exists(select 1 from pg_indexes where schemaname='public' and indexname='feed_articles_household_cursor_idx'),'Household cursor index exists';
    assert exists(select 1 from pg_indexes where schemaname='public' and indexname='feed_articles_source_cursor_idx'),'Source cursor index exists';
end;$$;
rollback;
