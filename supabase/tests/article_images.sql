begin;
insert into auth.users(id,email) values
('00000000-0000-4000-8000-000000009101','article-images-owner@example.test');
set role authenticated;
set request.jwt.claim.sub='00000000-0000-4000-8000-000000009101';
select set_config('loom.test.images_home',public.create_household('Pictures','EUR','Europe/Lisbon')::text,true);
insert into public.feed_subscriptions(id,household_id,url,created_by) values
('00000000-0000-4000-8000-000000009111',current_setting('loom.test.images_home')::uuid,'https://feeds.bbci.co.uk/news/rss.xml',auth.uid());
reset role;
insert into public.feed_articles(id,subscription_id,household_id,guid,title,url,source,image_url) values
('00000000-0000-4000-8000-000000009121','00000000-0000-4000-8000-000000009111',current_setting('loom.test.images_home')::uuid,'with','With','https://example.test/1','BBC','https://example.test/1.jpg'),
('00000000-0000-4000-8000-000000009122','00000000-0000-4000-8000-000000009111',current_setting('loom.test.images_home')::uuid,'older','Older','https://example.test/2','BBC',null),
('00000000-0000-4000-8000-000000009123','00000000-0000-4000-8000-000000009111',current_setting('loom.test.images_home')::uuid,'plain','Plain','https://example.test/3','BBC',null);
do $$begin
    begin
        insert into public.feed_articles(subscription_id,household_id,guid,title,url,source,image_url) values
        ('00000000-0000-4000-8000-000000009111',current_setting('loom.test.images_home')::uuid,'http','Http','https://example.test/4','BBC','http://example.test/4.jpg');
        raise exception 'insecure picture accepted';
    exception when check_violation then null;
    end;
end;$$;
set role service_role;
do $$declare filled integer; begin
    filled:=public.fill_article_images('00000000-0000-4000-8000-000000009111',
        '[{"guid":"with","image_url":"https://example.test/new.jpg"},
          {"guid":"older","image_url":"https://example.test/2.jpg"},
          {"guid":"plain","image_url":"http://example.test/3.jpg"}]'::jsonb);
    assert filled=1,'Only the empty, secure picture is filled';
end;$$;
reset role;
set role authenticated;
set request.jwt.claim.sub='00000000-0000-4000-8000-000000009101';
do $$begin
    assert (select image_url from public.feed_articles_with_state where id='00000000-0000-4000-8000-000000009121')='https://example.test/1.jpg','A saved picture is kept';
    assert (select image_url from public.feed_articles_with_state where id='00000000-0000-4000-8000-000000009122')='https://example.test/2.jpg','An older story picks up its picture';
    assert (select image_url is null from public.feed_articles_with_state where id='00000000-0000-4000-8000-000000009123'),'Insecure pictures are ignored';
    assert not (select is_read from public.feed_articles_with_state where id='00000000-0000-4000-8000-000000009121'),'Read state still comes through';
    begin
        perform public.fill_article_images('00000000-0000-4000-8000-000000009111','[]'::jsonb);
        raise exception 'members can fill pictures';
    exception when insufficient_privilege then null;
    end;
end;$$;
rollback;
