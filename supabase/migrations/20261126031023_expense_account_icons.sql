begin;

-- Money accounts can carry an icon, like shopping lists. No icon keeps today's look: a house for
-- Everyday and a plane for trips. Any member of an account may change it.
alter table public.expense_accounts add column icon text
    check (icon in ('home', 'travel', 'event', 'gift', 'food', 'nature', 'pets', 'health'));

create function loom_private.set_expense_account_icon(p_household_id uuid, p_id uuid, p_icon text)
returns void language plpgsql security definer set search_path = '' as $$
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) or not exists(
        select 1 from public.expense_account_members where account_id = p_id and user_id = auth.uid()
    ) then raise exception 'expenses.account_access_denied'; end if;
    if p_icon is not null and p_icon not in ('home', 'travel', 'event', 'gift', 'food', 'nature', 'pets', 'health') then
        raise exception 'expenses.invalid_account_icon';
    end if;
    update public.expense_accounts set icon = p_icon
    where id = p_id and household_id = p_household_id and not archived;
    if not found then raise exception 'expenses.account_archived'; end if;
end;
$$;
create function public.set_expense_account_icon(p_household_id uuid, p_id uuid, p_icon text)
returns void language sql security invoker set search_path = '' as $$
    select loom_private.set_expense_account_icon(p_household_id, p_id, p_icon);
$$;
revoke all on function loom_private.set_expense_account_icon(uuid, uuid, text),
    public.set_expense_account_icon(uuid, uuid, text) from public, anon;
grant execute on function loom_private.set_expense_account_icon(uuid, uuid, text),
    public.set_expense_account_icon(uuid, uuid, text) to authenticated, service_role;

-- The icon is appended, so older clients keep reading the columns they know.
create or replace view public.expense_accounts_with_members with (security_invoker = true) as
select a.id, a.household_id, a.name, a.currency, a.is_default, a.archived, a.version, a.created_by, a.created_at,
    coalesce((select jsonb_agg(m.user_id order by m.user_id)
        from public.expense_account_members m where m.account_id = a.id), '[]'::jsonb) as member_ids,
    coalesce((select jsonb_agg(jsonb_build_object('id', g.id, 'name', g.name, 'removed', g.removed_at is not null,
        'version', g.version) order by g.created_at, g.id)
        from public.expense_account_guests g where g.account_id = a.id), '[]'::jsonb) as guests,
    a.icon
from public.expense_accounts a;

commit;
