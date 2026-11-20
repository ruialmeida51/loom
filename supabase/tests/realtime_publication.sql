do $$ begin
 assert not exists(select 1 from pg_publication_tables where pubname = 'supabase_realtime' and schemaname = 'public'),
  'Loom tables are no longer published for realtime';
end;$$;
