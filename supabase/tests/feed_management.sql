begin;
insert into auth.users(id,email) values
('00000000-0000-4000-8000-000000001001','feed-owner@example.test'),
('00000000-0000-4000-8000-000000001002','feed-outsider@example.test');
set role authenticated;
set request.jwt.claim.sub='00000000-0000-4000-8000-000000001001';
select set_config('loom.test.feed_home',public.create_household('Feed home','EUR','Europe/Lisbon')::text,true);
insert into public.feed_subscriptions(id,household_id,url,created_by)
values('00000000-0000-4000-8000-000000001010',current_setting('loom.test.feed_home')::uuid,
'https://feeds.bbci.co.uk/news/rss.xml',auth.uid());
reset role;
insert into public.feed_articles(id,subscription_id,household_id,guid,title,url,source)
values('00000000-0000-4000-8000-000000001100','00000000-0000-4000-8000-000000001010',
current_setting('loom.test.feed_home')::uuid,'old-story','Old publisher story','https://example.test/story','Old source');

set role authenticated;
set request.jwt.claim.sub='00000000-0000-4000-8000-000000001002';
do $$ begin
    assert (select count(*) from public.feed_subscriptions)=0,'Subscriptions remain household scoped';
    begin
        perform public.update_feed_subscription(current_setting('loom.test.feed_home')::uuid,
            '00000000-0000-4000-8000-000000001010','https://feeds.bbci.co.uk/news/rss.xml',
            '00000000-0000-4000-8000-000000001011','https://rss.impresa.pt/feed/latest/expresso.rss');
        raise exception 'Cross-household editing accepted';
    exception when raise_exception then
        if sqlerrm <> 'Household access denied.' then raise; end if;
    end;
end $$;

set request.jwt.claim.sub='00000000-0000-4000-8000-000000001001';
do $$ begin
    begin
        perform public.update_feed_subscription(current_setting('loom.test.feed_home')::uuid,
            '00000000-0000-4000-8000-000000001010','https://stale.example.test/feed',
            '00000000-0000-4000-8000-000000001011','https://rss.impresa.pt/feed/latest/expresso.rss');
        raise exception 'Stale edit accepted';
    exception when raise_exception then
        if sqlerrm <> 'feed.subscription_changed' then raise; end if;
    end;
    assert exists(select 1 from public.feed_articles),'A failed edit retains the original articles';
end $$;
select public.update_feed_subscription(current_setting('loom.test.feed_home')::uuid,
'00000000-0000-4000-8000-000000001010','https://feeds.bbci.co.uk/news/rss.xml',
'00000000-0000-4000-8000-000000001011','https://rss.impresa.pt/feed/latest/expresso.rss');
do $$ begin
    assert not exists(select 1 from public.feed_articles),'Old articles are removed atomically';
    assert (select url from public.feed_subscriptions)='https://rss.impresa.pt/feed/latest/expresso.rss';
    assert (select last_fetched_at is null and last_error is null from public.feed_subscriptions);
end $$;

-- Simulate the old worker returning after the address was edited.
reset role;
do $$ begin
    begin
        insert into public.feed_articles(subscription_id,household_id,guid,title,url,source)
        values('00000000-0000-4000-8000-000000001010',current_setting('loom.test.feed_home')::uuid,
            'late-story','Stale fetch','https://example.test/late','Old source');
        raise exception 'Old worker resurrected an edited subscription';
    exception when foreign_key_violation then null; end;
end $$;
insert into public.feed_articles(subscription_id,household_id,guid,title,url,source)
values('00000000-0000-4000-8000-000000001011',current_setting('loom.test.feed_home')::uuid,
'new-story','New publisher story','https://example.test/new','New source');
set role authenticated;
set request.jwt.claim.sub='00000000-0000-4000-8000-000000001001';
delete from public.feed_subscriptions where id='00000000-0000-4000-8000-000000001011';
do $$ begin
    assert not exists(select 1 from public.feed_subscriptions),'A household member can remove a feed';
    assert not exists(select 1 from public.feed_articles),'Feed removal cascades its articles';
end $$;
reset role;
do $$ begin
    assert not has_function_privilege('anon','public.update_feed_subscription(uuid,uuid,text,uuid,text)','execute');
    assert not has_function_privilege('anon','loom_private.update_feed_subscription(uuid,uuid,text,uuid,text)','execute');
end $$;
rollback;
