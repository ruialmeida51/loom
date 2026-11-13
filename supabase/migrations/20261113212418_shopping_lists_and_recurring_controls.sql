begin;

create table public.shopping_lists (
    id uuid primary key default gen_random_uuid(),
    household_id uuid not null references public.households(id),
    name text not null check (length(trim(name)) between 1 and 60),
    archived boolean not null default false,
    version bigint not null default 1,
    created_at timestamptz not null default now(),
    unique(household_id,id)
);
create unique index shopping_lists_active_name_idx on public.shopping_lists(household_id,lower(trim(name))) where not archived;
alter table public.shopping_lists enable row level security;
create policy shopping_lists_read on public.shopping_lists for select to authenticated using(loom_private.is_member(household_id));
revoke all on public.shopping_lists from public,anon,authenticated;
grant select on public.shopping_lists to authenticated;
grant all on public.shopping_lists to service_role;

insert into public.shopping_lists(household_id,name) select id,'Groceries' from public.households;
create function loom_private.create_default_shopping_list() returns trigger
language plpgsql security definer set search_path='' as $$
begin
    insert into public.shopping_lists(household_id,name) values(new.id,'Groceries');
    return new;
end;
$$;
revoke all on function loom_private.create_default_shopping_list() from public,anon,authenticated;
create trigger household_shopping_list after insert on public.households for each row execute function loom_private.create_default_shopping_list();

alter table public.shopping_items add column list_id uuid;
alter table public.shopping_items add column version bigint not null default 1;
alter table public.shopping_items add column removed boolean not null default false;
alter table public.shopping_items add column favorite boolean not null default false;
update public.shopping_items i set list_id=l.id from public.shopping_lists l where l.household_id=i.household_id;
alter table public.shopping_items alter column list_id set not null;
alter table public.shopping_items add constraint shopping_items_list_scope foreign key(household_id,list_id) references public.shopping_lists(household_id,id);
create index shopping_items_list_idx on public.shopping_items(household_id,list_id,removed,checked);

-- Existing clients can still insert without a list ID. All writers receive server-owned versions.
create function loom_private.shopping_item_metadata() returns trigger
language plpgsql security definer set search_path='' as $$
begin
    if new.list_id is null then
        select id into new.list_id from public.shopping_lists where household_id=new.household_id and not archived order by created_at,id limit 1;
    end if;
    if not exists(select 1 from public.shopping_lists where id=new.list_id and household_id=new.household_id and not archived) then
        raise exception 'shopping.message_choose_an_active_shopping_list_in_this_household';
    end if;
    new.version:=case when tg_op='INSERT' then 1 else old.version+1 end;
    return new;
end;
$$;
revoke all on function loom_private.shopping_item_metadata() from public,anon,authenticated;
create trigger shopping_zmetadata before insert or update on public.shopping_items for each row execute function loom_private.shopping_item_metadata();

create function loom_private.save_shopping_list(p_household_id uuid,p_id uuid,p_name text,p_archived boolean,p_version bigint) returns public.shopping_lists
language plpgsql security definer set search_path='' as $$
declare row public.shopping_lists;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'shopping.message_household_access_denied'; end if;
    if p_name is null or length(trim(p_name)) not between 1 and 60 or p_archived is null or p_version is null then raise exception 'shopping.message_give_the_list_a_name_up_to_60_characters'; end if;
    perform 1 from public.households where id=p_household_id for update;
    select * into row from public.shopping_lists where id=p_id for update;
    if found then
        if row.household_id<>p_household_id then raise exception 'shopping.message_household_access_denied'; end if;
        if row.name=trim(p_name) and row.archived=p_archived then return row; end if;
        if row.version<>p_version then raise exception 'shopping.message_this_list_changed_refresh_and_try_again'; end if;
        if p_archived and not row.archived and (select count(*) from public.shopping_lists where household_id=p_household_id and not archived)<=1 then raise exception 'shopping.message_keep_at_least_one_shopping_list_active'; end if;
        update public.shopping_lists set name=trim(p_name),archived=p_archived,version=version+1 where id=p_id returning * into row;
    else
        if p_version<>0 or p_archived then raise exception 'shopping.message_this_shopping_list_no_longer_exists'; end if;
        insert into public.shopping_lists(id,household_id,name) values(p_id,p_household_id,trim(p_name)) returning * into row;
    end if;
    return row;
end;
$$;

create function loom_private.save_shopping_item(p_household_id uuid,p_id uuid,p_list_id uuid,p_name text,p_quantity text,p_category text,p_checked boolean,p_favorite boolean,p_removed boolean,p_version bigint) returns public.shopping_items
language plpgsql security definer set search_path='' as $$
declare row public.shopping_items;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'shopping.message_household_access_denied'; end if;
    if p_name is null or length(trim(p_name)) not between 1 and 120 or p_quantity is null or length(p_quantity)>40 or p_category is null or length(p_category) not between 1 and 40 or p_version is null or p_checked is null or p_favorite is null or p_removed is null then raise exception 'shopping.message_check_the_item_name_quantity_and_category'; end if;
    perform 1 from public.households where id=p_household_id for update;
    if not exists(select 1 from public.shopping_lists where id=p_list_id and household_id=p_household_id and not archived) then raise exception 'shopping.message_choose_an_active_shopping_list_in_this_household'; end if;
    select * into row from public.shopping_items where id=p_id for update;
    if found then
        if row.household_id<>p_household_id then raise exception 'shopping.message_household_access_denied'; end if;
        if (row.list_id,row.name,row.quantity,row.category,row.checked,row.favorite,row.removed) is not distinct from (p_list_id,trim(p_name),trim(p_quantity),p_category,p_checked,p_favorite,p_removed) then return row; end if;
        if row.version<>p_version then raise exception 'shopping.message_this_item_changed_refresh_before_undoing_or_editing_it'; end if;
        update public.shopping_items set list_id=p_list_id,name=trim(p_name),quantity=trim(p_quantity),category=p_category,checked=p_checked,favorite=p_favorite,removed=p_removed where id=p_id returning * into row;
    else
        if p_version<>0 then raise exception 'shopping.message_this_shopping_item_no_longer_exists'; end if;
        insert into public.shopping_items(id,household_id,list_id,name,quantity,category,checked,favorite,removed,created_by)
        values(p_id,p_household_id,p_list_id,trim(p_name),trim(p_quantity),p_category,p_checked,p_favorite,p_removed,auth.uid()) returning * into row;
    end if;
    return row;
end;
$$;
create function loom_private.add_shopping_items(p_household_id uuid,p_items jsonb) returns setof public.shopping_items
language plpgsql security definer set search_path='' as $$
declare item jsonb;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'shopping.message_household_access_denied'; end if;
    if p_items is null or jsonb_typeof(p_items)<>'array' or jsonb_array_length(p_items) not between 1 and 50 then raise exception 'shopping.message_add_between_1_and_50_items_at_once'; end if;
    for item in select value from jsonb_array_elements(p_items) loop
        return next loom_private.save_shopping_item(p_household_id,(item->>'id')::uuid,(item->>'list_id')::uuid,item->>'name',coalesce(item->>'quantity',''),coalesce(item->>'category','Pantry'),false,false,false,0);
    end loop;
end;
$$;

alter table public.recurring_expenses add column cancelled_at timestamptz;
alter table public.recurring_expenses add column version bigint not null default 1;
alter table public.recurring_expenses add constraint recurring_cancelled_inactive check(cancelled_at is null or not active);
create function loom_private.recurring_expense_version() returns trigger
language plpgsql set search_path='' as $$
begin
    if (new.title,new.amount_minor,new.paid_by,new.category,new.shares,new.anchor_day,new.next_on,new.active,new.cancelled_at)
        is distinct from (old.title,old.amount_minor,old.paid_by,old.category,old.shares,old.anchor_day,old.next_on,old.active,old.cancelled_at) then
        new.version:=old.version+1;
    else new.version:=old.version;
    end if;
    return new;
end;
$$;
revoke all on function loom_private.recurring_expense_version() from public,anon,authenticated;
create trigger recurring_version before update on public.recurring_expenses for each row execute function loom_private.recurring_expense_version();

create function loom_private.control_recurring_expense(p_household_id uuid,p_id uuid,p_action text,p_version bigint) returns public.recurring_expenses
language plpgsql security definer set search_path='' as $$
declare row public.recurring_expenses; v_today date; v_candidate date;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'expenses.message_household_access_denied'; end if;
    select * into row from public.recurring_expenses where id=p_id and household_id=p_household_id for update;
    if not found then raise exception 'expenses.message_this_recurring_expense_no_longer_exists'; end if;
    if auth.uid() not in(row.created_by,row.paid_by) and not loom_private.is_owner(p_household_id) then raise exception 'expenses.message_only_the_payer_creator_or_household_owner_can_manage'; end if;
    if p_action is null or p_action not in('pause','resume','cancel') or p_version is null then raise exception 'expenses.message_choose_a_valid_schedule_action'; end if;
    if p_action='cancel' and row.cancelled_at is not null or p_action='pause' and not row.active and row.cancelled_at is null or p_action='resume' and row.active then return row; end if;
    if row.cancelled_at is not null then raise exception 'expenses.message_a_cancelled_schedule_cannot_be_resumed'; end if;
    if row.version<>p_version then raise exception 'expenses.message_this_schedule_changed_refresh_and_try_again'; end if;
    if p_action='resume' then
        if not exists(select 1 from public.household_members where household_id=p_household_id and user_id=row.paid_by and active) or
           not exists(select 1 from public.household_members where household_id=p_household_id and user_id=row.created_by and active) or
           exists(select 1 from jsonb_each(row.shares) s where not exists(select 1 from public.household_members m where m.household_id=p_household_id and m.user_id=s.key::uuid and m.active)) then raise exception 'expenses.message_all_participants_must_be_current_household_members'; end if;
        select (now() at time zone time_zone)::date into v_today from public.households where id=p_household_id;
        if row.next_on<=v_today then
            v_candidate:=date_trunc('month',v_today)::date + least(row.anchor_day,extract(day from date_trunc('month',v_today)+interval '1 month - 1 day')::integer)-1;
            row.next_on:=case when v_candidate>v_today then v_candidate else loom_private.next_month_date(v_today,row.anchor_day) end;
        end if;
    end if;
    update public.recurring_expenses set active=(p_action='resume'),cancelled_at=case when p_action='cancel' then now() else null end,next_on=row.next_on where id=p_id returning * into row;
    return row;
end;
$$;

create function public.save_shopping_list(p_household_id uuid,p_id uuid,p_name text,p_archived boolean,p_version bigint) returns public.shopping_lists language sql security invoker set search_path='' as $$select loom_private.save_shopping_list(p_household_id,p_id,p_name,p_archived,p_version);$$;
create function public.save_shopping_item(p_household_id uuid,p_id uuid,p_list_id uuid,p_name text,p_quantity text,p_category text,p_checked boolean,p_favorite boolean,p_removed boolean,p_version bigint) returns public.shopping_items language sql security invoker set search_path='' as $$select loom_private.save_shopping_item(p_household_id,p_id,p_list_id,p_name,p_quantity,p_category,p_checked,p_favorite,p_removed,p_version);$$;
create function public.add_shopping_items(p_household_id uuid,p_items jsonb) returns setof public.shopping_items language sql security invoker set search_path='' as $$select * from loom_private.add_shopping_items(p_household_id,p_items);$$;
create function public.control_recurring_expense(p_household_id uuid,p_id uuid,p_action text,p_version bigint) returns public.recurring_expenses language sql security invoker set search_path='' as $$select loom_private.control_recurring_expense(p_household_id,p_id,p_action,p_version);$$;
do $$declare fn record; begin
    for fn in select p.oid::regprocedure signature from pg_proc p join pg_namespace n on n.oid=p.pronamespace where n.nspname in('public','loom_private') and p.proname in('save_shopping_list','save_shopping_item','add_shopping_items','control_recurring_expense') loop
        execute format('revoke all on function %s from public,anon',fn.signature);
        execute format('grant execute on function %s to authenticated,service_role',fn.signature);
    end loop;
    if exists(select 1 from pg_publication where pubname='supabase_realtime') then
        alter publication supabase_realtime add table public.shopping_lists,public.recurring_expenses;
    end if;
end;$$;
-- Recorded payments use household-local dates; upcoming repeats remain separate schedule entries.
create or replace function loom_private.create_expense(p_id uuid,p_household_id uuid,p_title text,p_amount_minor bigint,p_paid_by uuid,p_category text,p_occurred_on date,p_recurring boolean,p_shares jsonb) returns uuid
language plpgsql security definer set search_path='' as $$
declare v_rule uuid; v_sum numeric; v_existing public.expenses;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'Household access denied.'; end if;
    if p_occurred_on is null or p_occurred_on>(select (now() at time zone time_zone)::date from public.households where id=p_household_id) then raise exception 'expenses.message_payment_date_future'; end if;
    if jsonb_typeof(p_shares)<>'object' or p_amount_minor not between 1 and 100000000000 then raise exception 'Invalid expense amount or shares.'; end if;
    if (select count(*) from jsonb_each(p_shares)) not between 1 and 100 then raise exception 'Choose who to split with.'; end if;
    if exists(select 1 from jsonb_each(p_shares) s where jsonb_typeof(s.value)<>'number' or s.value::text !~ '^[0-9]+$') then raise exception 'Shares must be integer minor units.'; end if;
    select sum(value::text::numeric) into v_sum from jsonb_each(p_shares);
    if v_sum<>p_amount_minor or exists(select 1 from jsonb_each(p_shares) where value::text::numeric>p_amount_minor) then raise exception 'Shares must equal the expense total.'; end if;
    if not exists(select 1 from public.household_members where household_id=p_household_id and user_id=p_paid_by and active) or
       exists(select 1 from jsonb_each(p_shares) s where not exists(select 1 from public.household_members m where m.household_id=p_household_id and m.user_id=s.key::uuid and m.active)) then raise exception 'All participants must be current household members.'; end if;
    select * into v_existing from public.expenses where id=p_id;
    if found then
        if v_existing.household_id=p_household_id and v_existing.created_by=auth.uid() and v_existing.title=trim(p_title) and v_existing.amount_minor=p_amount_minor and v_existing.paid_by=p_paid_by and v_existing.category=p_category and v_existing.occurred_on=p_occurred_on and (v_existing.recurring_rule_id is not null)=p_recurring and
           (select jsonb_object_agg(user_id::text,amount_minor) from public.expense_shares where expense_id=p_id)=p_shares then return p_id; end if;
        raise exception 'This expense ID has already been used.';
    end if;
    if p_recurring then
        insert into public.recurring_expenses(household_id,title,amount_minor,paid_by,category,shares,anchor_day,next_on,created_by)
        values(p_household_id,trim(p_title),p_amount_minor,p_paid_by,p_category,p_shares,extract(day from p_occurred_on)::integer,loom_private.next_month_date(p_occurred_on,extract(day from p_occurred_on)::integer),auth.uid()) returning id into v_rule;
    end if;
    insert into public.expenses(id,household_id,title,amount_minor,paid_by,category,occurred_on,recurring_rule_id,created_by)
    values(p_id,p_household_id,trim(p_title),p_amount_minor,p_paid_by,p_category,p_occurred_on,v_rule,auth.uid());
    insert into public.expense_shares(expense_id,household_id,user_id,amount_minor) select p_id,p_household_id,key::uuid,value::text::bigint from jsonb_each(p_shares);
    insert into public.notifications(household_id,user_id,event_key,title,body,data)
    select p_household_id,m.user_id,'expense:'||p_id,'A new shared expense','There’s a new expense in your household.',jsonb_build_object('route','money','household_id',p_household_id)
    from public.household_members m where m.household_id=p_household_id and m.active and m.user_id<>auth.uid() on conflict(user_id,event_key) do nothing;
    return p_id;
end;
$$;
commit;
