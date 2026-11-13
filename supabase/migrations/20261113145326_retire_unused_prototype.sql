-- Retire the unused prototype reversibly; its private archive is not exposed through the Data API.
-- Keep auth identities so existing accounts can sign in and create their new household.
begin;
create schema if not exists loom_legacy;
revoke all on schema loom_legacy from public,anon,authenticated;
do $$declare item record;begin
    for item in select p.oid::regprocedure as signature from pg_proc p join pg_namespace n on n.oid=p.pronamespace
        where n.nspname='public' and p.proname in('create_household','is_household_member','redeem_household_invite','is_household_owner','is_shopping_list_accessible','create_household_invite','decline_household_invite','revoke_household_invite')
    loop execute format('alter function %s set schema loom_legacy',item.signature);end loop;
    for item in select tablename from pg_tables where schemaname='public' and tablename in('shopping_list_items','shopping_lists','household_invites','expenses','household_members','households')
    loop execute format('alter table public.%I set schema loom_legacy',item.tablename);end loop;
    for item in select p.oid::regprocedure as signature from pg_proc p join pg_namespace n on n.oid=p.pronamespace where n.nspname='loom_legacy'
    loop execute format('alter function %s set search_path = %L',item.signature,'');end loop;
end;$$;
revoke all on all functions in schema loom_legacy from public,anon,authenticated;
revoke all on all tables in schema loom_legacy from public,anon,authenticated;
commit;
