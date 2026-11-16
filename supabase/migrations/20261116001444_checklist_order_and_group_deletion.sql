begin;

-- Keep checklist order independent of UUID/keyset read order. The server owns every position.
alter table public.shopping_items add column position bigint;
alter table public.shopping_items disable trigger shopping_zmetadata;
with ordered as (
    select id, row_number() over (order by created_at, id) as position
    from public.shopping_items
)
update public.shopping_items item set position = ordered.position
from ordered where item.id = ordered.id;
alter table public.shopping_items enable trigger shopping_zmetadata;
alter table public.shopping_items alter column position set not null;
create sequence loom_private.shopping_item_position;
select pg_catalog.setval('loom_private.shopping_item_position',
    coalesce((select max(position) from public.shopping_items), 0) + 1, false);
revoke all on sequence loom_private.shopping_item_position from public, anon, authenticated;

create function loom_private.assign_shopping_position() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
    if tg_op = 'INSERT' then
        new.position := nextval('loom_private.shopping_item_position');
    elsif new.list_id is distinct from old.list_id or
        (old.checked and not new.checked) or (old.removed and not new.removed) then
        new.position := nextval('loom_private.shopping_item_position');
    else
        new.position := old.position;
    end if;
    return new;
end;
$$;
revoke all on function loom_private.assign_shopping_position() from public, anon, authenticated, service_role;
create trigger shopping_zposition before insert or update on public.shopping_items
for each row execute function loom_private.assign_shopping_position();
create index shopping_items_position_idx on public.shopping_items(household_id, list_id, position);

-- Deletion is explicit and transactional, including the final list. Membership, not authorship,
-- authorizes this shared household action. A repeated successful request remains harmless.
create function loom_private.delete_shopping_list(p_household_id uuid, p_id uuid, p_version bigint)
returns void language plpgsql security definer set search_path = '' as $$
declare target public.shopping_lists;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then
        raise exception 'shopping.message_household_access_denied';
    end if;
    perform 1 from public.households where id = p_household_id for update;
    select * into target from public.shopping_lists where id = p_id for update;
    if not found then return; end if;
    if target.household_id <> p_household_id then
        raise exception 'shopping.message_household_access_denied';
    end if;
    if p_version is null or target.version <> p_version then
        raise exception 'shopping.message_this_list_changed_refresh_and_try_again';
    end if;
    delete from public.shopping_items where household_id = p_household_id and list_id = p_id;
    delete from public.shopping_lists where id = p_id and household_id = p_household_id;
end;
$$;
create function public.delete_shopping_list(p_household_id uuid, p_id uuid, p_version bigint)
returns void language sql security invoker set search_path = '' as $$
    select loom_private.delete_shopping_list(p_household_id, p_id, p_version);
$$;

-- Everyday is the household's built-in account. Trips/events can be permanently deleted by
-- their creator or household owner; the confirmation UI enumerates the records removed here.
create function loom_private.delete_expense_account(p_household_id uuid, p_id uuid, p_version bigint)
returns void language plpgsql security definer set search_path = '' as $$
declare target public.expense_accounts;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then
        raise exception 'expenses.account_access_denied';
    end if;
    perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(p_id::text, 0));
    select * into target from public.expense_accounts where id = p_id for no key update;
    if not found then return; end if;
    if target.household_id <> p_household_id or target.is_default or
       (target.created_by <> auth.uid() and not loom_private.is_owner(p_household_id)) then
        raise exception 'expenses.account_access_denied';
    end if;
    if p_version is null or target.version <> p_version then
        raise exception 'expenses.record_changed';
    end if;
    -- The worker locks schedules before creating occurrences. Wait for any occurrence already
    -- being issued, then remove its shares/expense as part of this same transaction.
    perform 1 from public.recurring_expenses where account_id = p_id for update;
    delete from public.notifications where household_id = p_household_id and data->>'account_id' = p_id::text;
    delete from public.monthly_statements where household_id = p_household_id and account_id = p_id;
    delete from public.expense_shares share using public.expenses expense
        where share.expense_id = expense.id and expense.account_id = p_id and expense.household_id = p_household_id;
    delete from public.expenses where household_id = p_household_id and account_id = p_id;
    delete from public.settlements where household_id = p_household_id and account_id = p_id;
    delete from public.recurring_expenses where household_id = p_household_id and account_id = p_id;
    delete from public.expense_account_members where household_id = p_household_id and account_id = p_id;
    delete from public.expense_accounts where household_id = p_household_id and id = p_id;
end;
$$;
create function public.delete_expense_account(p_household_id uuid, p_id uuid, p_version bigint)
returns void language sql security invoker set search_path = '' as $$
    select loom_private.delete_expense_account(p_household_id, p_id, p_version);
$$;

revoke all on function public.delete_shopping_list(uuid, uuid, bigint),
    loom_private.delete_shopping_list(uuid, uuid, bigint),
    public.delete_expense_account(uuid, uuid, bigint),
    loom_private.delete_expense_account(uuid, uuid, bigint) from public, anon;
grant execute on function public.delete_shopping_list(uuid, uuid, bigint),
    loom_private.delete_shopping_list(uuid, uuid, bigint),
    public.delete_expense_account(uuid, uuid, bigint),
    loom_private.delete_expense_account(uuid, uuid, bigint) to authenticated, service_role;

commit;
