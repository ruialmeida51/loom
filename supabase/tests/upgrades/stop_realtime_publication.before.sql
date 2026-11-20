-- Hosted projects publish Loom tables through Supabase's realtime publication; recreate that here.
do $$ begin
 if not exists(select 1 from pg_publication where pubname = 'supabase_realtime') then
  create publication supabase_realtime;
 end if;
end;$$;
alter publication supabase_realtime add table public.shopping_items, public.tasks;
