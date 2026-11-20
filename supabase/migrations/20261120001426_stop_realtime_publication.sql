begin;

-- Loom no longer subscribes to live changes; screens load on open, on pull-to-refresh and after
-- the app's own writes. Stop publishing table changes so no replication work runs for them on the
-- free tier. Older app versions keep working: their subscriptions simply receive no events.
do $$
declare
    published record;
begin
    if exists(select 1 from pg_publication where pubname = 'supabase_realtime') then
        for published in
            select schemaname, tablename from pg_publication_tables
            where pubname = 'supabase_realtime' and schemaname = 'public'
        loop
            execute format('alter publication supabase_realtime drop table %I.%I',
                published.schemaname, published.tablename);
        end loop;
    end if;
end;
$$;

commit;
