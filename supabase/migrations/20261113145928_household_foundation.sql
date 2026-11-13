begin;

create schema if not exists extensions;
create extension if not exists pgcrypto with schema extensions;
create schema loom_private;
revoke all on schema loom_private from public, anon;
grant usage on schema loom_private to authenticated, service_role;

create table public.profiles (
    id uuid primary key references auth.users(id) on delete restrict,
    display_name text not null check (length(display_name) between 1 and 80),
    created_at timestamptz not null default now()
);
create table public.households (
    id uuid primary key default gen_random_uuid(),
    name text not null check (length(name) between 2 and 60),
    currency text not null check (currency in ('EUR','GBP','USD','CAD','AUD')),
    time_zone text not null,
    created_by uuid not null references public.profiles(id),
    created_at timestamptz not null default now()
);
create table public.household_members (
    household_id uuid not null references public.households(id),
    user_id uuid not null references public.profiles(id),
    role text not null check (role in ('owner','member')),
    active boolean not null default true,
    joined_at timestamptz not null default now(),
    primary key (household_id,user_id)
);
create index household_members_user_idx on public.household_members(user_id,household_id) where active;
create index households_creator_idx on public.households(created_by);

create table loom_private.household_invites (
    id uuid primary key default gen_random_uuid(),
    household_id uuid not null references public.households(id),
    token_hash bytea not null unique,
    created_by uuid not null references public.profiles(id),
    expires_at timestamptz not null default now()+interval '48 hours',
    used_by uuid references public.profiles(id),
    used_at timestamptz
);

create function loom_private.is_member(p_household_id uuid) returns boolean
language sql stable security definer set search_path='' as $$
    select auth.uid() is not null and exists (
        select 1 from public.household_members m
        where m.household_id=p_household_id and m.user_id=(select auth.uid()) and m.active
    );
$$;
create function loom_private.is_owner(p_household_id uuid) returns boolean
language sql stable security definer set search_path='' as $$
    select auth.uid() is not null and exists (
        select 1 from public.household_members m
        where m.household_id=p_household_id and m.user_id=(select auth.uid()) and m.active and m.role='owner'
    );
$$;
create function loom_private.can_view_profile(p_user_id uuid) returns boolean
language sql stable security definer set search_path='' as $$
    select auth.uid() is not null and (p_user_id=(select auth.uid()) or exists (
        select 1 from public.household_members mine
        join public.household_members other on other.household_id=mine.household_id
        where mine.user_id=(select auth.uid()) and mine.active and other.user_id=p_user_id
    ));
$$;

create function loom_private.handle_new_user() returns trigger
language plpgsql security definer set search_path='' as $$
begin
    insert into public.profiles(id,display_name)
    values(new.id,left(coalesce(nullif(split_part(new.email,'@',1),''),'Household member'),80));
    return new;
end;
$$;
create trigger loom_profile_on_signup after insert on auth.users for each row execute function loom_private.handle_new_user();
insert into public.profiles(id,display_name)
select id,left(coalesce(nullif(split_part(email,'@',1),''),'Household member'),80) from auth.users
on conflict(id) do nothing;

create table public.shopping_items (
    id uuid primary key default gen_random_uuid(),
    household_id uuid not null references public.households(id),
    name text not null check (length(trim(name)) between 1 and 120),
    quantity text not null default '' check (length(quantity)<=40),
    category text not null default 'Pantry' check (length(category) between 1 and 40),
    checked boolean not null default false,
    created_by uuid not null,
    created_at timestamptz not null default now(),
    foreign key(household_id,created_by) references public.household_members(household_id,user_id)
);
create index shopping_household_idx on public.shopping_items(household_id,checked,created_at);
create index shopping_creator_idx on public.shopping_items(created_by);
create table public.reminders (
    id uuid primary key default gen_random_uuid(),
    household_id uuid not null references public.households(id),
    title text not null check(length(trim(title)) between 1 and 160),
    due_at timestamptz not null,
    recurrence text not null default 'none' check(recurrence in ('none','daily','weekly')),
    completed boolean not null default false,
    created_by uuid not null,
    created_at timestamptz not null default now(),
    foreign key(household_id,created_by) references public.household_members(household_id,user_id)
);
create index reminders_household_idx on public.reminders(household_id,due_at);
create index reminders_due_idx on public.reminders(due_at) where not completed;
create index reminders_creator_idx on public.reminders(created_by);

create table public.recurring_expenses (
    id uuid primary key default gen_random_uuid(),
    household_id uuid not null references public.households(id),
    title text not null,
    amount_minor bigint not null check(amount_minor between 1 and 100000000000),
    paid_by uuid not null,
    category text not null,
    shares jsonb not null check(jsonb_typeof(shares)='object'),
    anchor_day integer not null check(anchor_day between 1 and 31),
    next_on date not null,
    active boolean not null default true,
    created_by uuid not null,
    created_at timestamptz not null default now(),
    foreign key(household_id,paid_by) references public.household_members(household_id,user_id),
    foreign key(household_id,created_by) references public.household_members(household_id,user_id)
);
create index recurring_household_idx on public.recurring_expenses(household_id);
create index recurring_due_idx on public.recurring_expenses(next_on) where active;
create index recurring_payer_idx on public.recurring_expenses(paid_by);
create index recurring_creator_idx on public.recurring_expenses(created_by);
create table public.expenses (
    id uuid primary key,
    household_id uuid not null references public.households(id),
    title text not null check(length(trim(title)) between 1 and 160),
    amount_minor bigint not null check(amount_minor between 1 and 100000000000),
    paid_by uuid not null,
    category text not null check(length(category) between 1 and 40),
    occurred_on date not null,
    recurring_rule_id uuid references public.recurring_expenses(id),
    created_by uuid not null,
    created_at timestamptz not null default now(),
    unique(id,household_id),
    unique(recurring_rule_id,occurred_on),
    foreign key(household_id,paid_by) references public.household_members(household_id,user_id),
    foreign key(household_id,created_by) references public.household_members(household_id,user_id)
);
create index expenses_household_month_idx on public.expenses(household_id,occurred_on);
create index expenses_payer_idx on public.expenses(paid_by);
create index expenses_creator_idx on public.expenses(created_by);
create table public.expense_shares (
    expense_id uuid not null,
    household_id uuid not null,
    user_id uuid not null,
    amount_minor bigint not null check(amount_minor between 0 and 100000000000),
    primary key(expense_id,user_id),
    foreign key(expense_id,household_id) references public.expenses(id,household_id),
    foreign key(household_id,user_id) references public.household_members(household_id,user_id)
);
create index expense_shares_household_idx on public.expense_shares(household_id,user_id);
create index expense_shares_user_idx on public.expense_shares(user_id);
create table public.settlements (
    id uuid primary key,
    household_id uuid not null references public.households(id),
    from_user uuid not null,
    to_user uuid not null,
    amount_minor bigint not null check(amount_minor between 1 and 100000000000),
    occurred_on date not null,
    created_by uuid not null references public.profiles(id),
    created_at timestamptz not null default now(),
    check(from_user<>to_user),
    foreign key(household_id,from_user) references public.household_members(household_id,user_id),
    foreign key(household_id,to_user) references public.household_members(household_id,user_id)
);
create index settlements_household_idx on public.settlements(household_id,occurred_on);
create index settlements_from_idx on public.settlements(from_user);
create index settlements_to_idx on public.settlements(to_user);
create index settlements_creator_idx on public.settlements(created_by);

create table public.feed_subscriptions (
    id uuid primary key,
    household_id uuid not null references public.households(id),
    url text not null check(url like 'https://%' and length(url)<=2048),
    created_by uuid not null,
    last_fetched_at timestamptz,
    last_error text,
    created_at timestamptz not null default now(),
    unique(household_id,url),
    unique(id,household_id),
    foreign key(household_id,created_by) references public.household_members(household_id,user_id)
);
create index feed_creator_idx on public.feed_subscriptions(created_by);
create table public.feed_articles (
    id uuid primary key default gen_random_uuid(),
    subscription_id uuid not null,
    household_id uuid not null,
    guid text not null check(length(guid)<=4096),
    title text not null,
    summary text not null default '',
    url text not null check(url like 'https://%'),
    source text not null,
    published_at timestamptz not null default now(),
    unique(subscription_id,guid),
    foreign key(subscription_id,household_id) references public.feed_subscriptions(id,household_id) on delete cascade
);
create index articles_household_idx on public.feed_articles(household_id,published_at desc);
create table public.article_reads (
    article_id uuid not null references public.feed_articles(id) on delete cascade,
    user_id uuid not null references public.profiles(id),
    is_read boolean not null default true,
    primary key(article_id,user_id)
);
create index reads_user_idx on public.article_reads(user_id);

create table public.monthly_statements (
    id uuid primary key default gen_random_uuid(),
    household_id uuid not null references public.households(id),
    month date not null check(extract(day from month)=1),
    revision integer not null,
    payload jsonb not null,
    content_hash text not null,
    created_at timestamptz not null default now(),
    unique(household_id,month,revision),
    unique(household_id,month,content_hash)
);
create table public.notification_preferences (
    user_id uuid primary key references public.profiles(id),
    enabled boolean not null default false
);
create table public.push_devices (
    id uuid primary key,
    user_id uuid not null references public.profiles(id),
    token text not null unique check(length(token) between 10 and 4096),
    platform text not null check(platform in ('fcm','apns')),
    updated_at timestamptz not null default now()
);
create index devices_user_idx on public.push_devices(user_id);
create table public.notifications (
    id uuid primary key default gen_random_uuid(),
    household_id uuid not null references public.households(id),
    user_id uuid not null references public.profiles(id),
    event_key text not null,
    title text not null,
    body text not null,
    data jsonb not null default '{}',
    created_at timestamptz not null default now(),
    unique(user_id,event_key)
);
create index notifications_household_idx on public.notifications(household_id,created_at);
create index notifications_user_idx on public.notifications(user_id,created_at);
create table loom_private.notification_deliveries (
    id uuid primary key default gen_random_uuid(),
    notification_id uuid not null references public.notifications(id) on delete cascade,
    device_id uuid not null references public.push_devices(id) on delete cascade,
    status text not null default 'pending' check(status in ('pending','sending','sent','dead')),
    attempts integer not null default 0,
    next_attempt_at timestamptz not null default now(),
    lease_until timestamptz,
    last_error text,
    unique(notification_id,device_id)
);
create index deliveries_pending_idx on loom_private.notification_deliveries(next_attempt_at) where status in ('pending','sending');
create table loom_private.statement_runs(household_id uuid references public.households(id),month date,primary key(household_id,month));

-- No authenticated client can insert or modify money rows directly. These views preserve caller RLS.
create view public.expenses_with_shares with(security_invoker=true) as
select e.id,e.household_id,e.title,e.amount_minor,e.paid_by,e.category,e.occurred_on,
       e.recurring_rule_id is not null as recurring,
       coalesce((select jsonb_object_agg(s.user_id::text,s.amount_minor) from public.expense_shares s where s.expense_id=e.id),'{}'::jsonb) as shares
from public.expenses e;
create view public.feed_articles_with_state with(security_invoker=true) as
select a.*,coalesce(r.is_read,false) as is_read from public.feed_articles a
left join public.article_reads r on r.article_id=a.id and r.user_id=(select auth.uid());
create view public.household_ledger_entries with(security_invoker=true) as
select household_id,occurred_on,paid_by as user_id,amount_minor as delta_minor from public.expenses
union all select s.household_id,e.occurred_on,s.user_id,-s.amount_minor from public.expense_shares s join public.expenses e on e.id=s.expense_id
union all select household_id,occurred_on,from_user,amount_minor from public.settlements
union all select household_id,occurred_on,to_user,-amount_minor from public.settlements;

create function loom_private.prevent_scope_change() returns trigger language plpgsql set search_path='' as $$
begin
    if new.id<>old.id or new.household_id<>old.household_id or new.created_by<>old.created_by then
        raise exception 'A record cannot be moved to another household or author.';
    end if;
    return new;
end;
$$;
create trigger shopping_scope before update on public.shopping_items for each row execute function loom_private.prevent_scope_change();
create trigger reminder_scope before update on public.reminders for each row execute function loom_private.prevent_scope_change();
create trigger feed_scope before update on public.feed_subscriptions for each row execute function loom_private.prevent_scope_change();

-- Every exposed table has RLS, including those that are only written by controlled server functions.
alter table public.profiles enable row level security;
alter table public.households enable row level security;
alter table public.household_members enable row level security;
alter table public.shopping_items enable row level security;
alter table public.reminders enable row level security;
alter table public.recurring_expenses enable row level security;
alter table public.expenses enable row level security;
alter table public.expense_shares enable row level security;
alter table public.settlements enable row level security;
alter table public.feed_subscriptions enable row level security;
alter table public.feed_articles enable row level security;
alter table public.article_reads enable row level security;
alter table public.monthly_statements enable row level security;
alter table public.notification_preferences enable row level security;
alter table public.push_devices enable row level security;
alter table public.notifications enable row level security;
alter table loom_private.household_invites enable row level security;
alter table loom_private.notification_deliveries enable row level security;
alter table loom_private.statement_runs enable row level security;

create policy profiles_read on public.profiles for select to authenticated using(loom_private.can_view_profile(id));
create policy profiles_update on public.profiles for update to authenticated using(id=(select auth.uid())) with check(id=(select auth.uid()));
create policy household_read on public.households for select to authenticated using(loom_private.is_member(id));
create policy membership_read on public.household_members for select to authenticated using(loom_private.is_member(household_id));
create policy shopping_read on public.shopping_items for select to authenticated using(loom_private.is_member(household_id));
create policy shopping_insert on public.shopping_items for insert to authenticated with check(loom_private.is_member(household_id) and created_by=(select auth.uid()));
create policy shopping_update on public.shopping_items for update to authenticated using(loom_private.is_member(household_id)) with check(loom_private.is_member(household_id));
create policy shopping_delete on public.shopping_items for delete to authenticated using(loom_private.is_member(household_id));
create policy reminders_read on public.reminders for select to authenticated using(loom_private.is_member(household_id));
create policy reminders_insert on public.reminders for insert to authenticated with check(loom_private.is_member(household_id) and created_by=(select auth.uid()));
create policy reminders_update on public.reminders for update to authenticated using(loom_private.is_member(household_id)) with check(loom_private.is_member(household_id));
create policy reminders_delete on public.reminders for delete to authenticated using(loom_private.is_member(household_id));
create policy recurring_read on public.recurring_expenses for select to authenticated using(loom_private.is_member(household_id));
create policy expenses_read on public.expenses for select to authenticated using(loom_private.is_member(household_id));
create policy shares_read on public.expense_shares for select to authenticated using(loom_private.is_member(household_id));
create policy settlements_read on public.settlements for select to authenticated using(loom_private.is_member(household_id));
create policy feeds_read on public.feed_subscriptions for select to authenticated using(loom_private.is_member(household_id));
create policy feeds_insert on public.feed_subscriptions for insert to authenticated with check(loom_private.is_member(household_id) and created_by=(select auth.uid()));
create policy feeds_delete on public.feed_subscriptions for delete to authenticated using(loom_private.is_member(household_id));
create policy articles_read on public.feed_articles for select to authenticated using(loom_private.is_member(household_id));
create policy reads_read on public.article_reads for select to authenticated using(user_id=(select auth.uid()) and exists(select 1 from public.feed_articles a where a.id=article_id and loom_private.is_member(a.household_id)));
create policy reads_insert on public.article_reads for insert to authenticated with check(user_id=(select auth.uid()) and exists(select 1 from public.feed_articles a where a.id=article_id and loom_private.is_member(a.household_id)));
create policy reads_update on public.article_reads for update to authenticated using(user_id=(select auth.uid())) with check(user_id=(select auth.uid()) and exists(select 1 from public.feed_articles a where a.id=article_id and loom_private.is_member(a.household_id)));
create policy statements_read on public.monthly_statements for select to authenticated using(loom_private.is_member(household_id));
create policy preferences_read on public.notification_preferences for select to authenticated using(user_id=(select auth.uid()));
create policy devices_read on public.push_devices for select to authenticated using(user_id=(select auth.uid()));
create policy notifications_read on public.notifications for select to authenticated using(user_id=(select auth.uid()) and loom_private.is_member(household_id));

revoke all on public.profiles,public.households,public.household_members,public.shopping_items,public.reminders,
    public.recurring_expenses,public.expenses,public.expense_shares,public.settlements,public.feed_subscriptions,
    public.feed_articles,public.article_reads,public.monthly_statements,public.notification_preferences,
    public.push_devices,public.notifications from anon,authenticated;
grant select on public.profiles,public.households,public.household_members,public.shopping_items,public.reminders,
    public.recurring_expenses,public.expenses,public.expense_shares,public.settlements,public.feed_subscriptions,
    public.feed_articles,public.article_reads,public.monthly_statements,public.notification_preferences,
    public.push_devices,public.notifications,public.expenses_with_shares,public.feed_articles_with_state,
    public.household_ledger_entries to authenticated;
grant update(display_name) on public.profiles to authenticated;
grant insert,update,delete on public.shopping_items,public.reminders to authenticated;
grant insert,delete on public.feed_subscriptions to authenticated;
grant insert,update on public.article_reads to authenticated;
grant all on all tables in schema loom_private to service_role;
grant all on public.profiles,public.households,public.household_members,public.shopping_items,public.reminders,
    public.recurring_expenses,public.expenses,public.expense_shares,public.settlements,public.feed_subscriptions,
    public.feed_articles,public.article_reads,public.monthly_statements,public.notification_preferences,
    public.push_devices,public.notifications to service_role;
grant select on public.expenses_with_shares,public.feed_articles_with_state,public.household_ledger_entries to service_role;

-- Privileged implementations live outside the exposed schema and validate the caller explicitly.
create function loom_private.create_household(p_name text,p_currency text,p_time_zone text) returns uuid
language plpgsql security definer set search_path='' as $$
declare v_id uuid;
begin
    if auth.uid() is null then raise exception 'Please sign in.'; end if;
    if not exists(select 1 from pg_catalog.pg_timezone_names where name=p_time_zone) then raise exception 'Invalid timezone.'; end if;
    insert into public.households(name,currency,time_zone,created_by) values(trim(p_name),p_currency,p_time_zone,auth.uid()) returning id into v_id;
    insert into public.household_members(household_id,user_id,role) values(v_id,auth.uid(),'owner');
    return v_id;
end;
$$;
create function loom_private.create_household_invite(p_household_id uuid) returns text
language plpgsql security definer set search_path='' as $$
declare v_token text;
begin
    if auth.uid() is null or not loom_private.is_owner(p_household_id) then raise exception 'Only the household owner can invite members.'; end if;
    v_token:=encode(extensions.gen_random_bytes(32),'hex');
    insert into loom_private.household_invites(household_id,token_hash,created_by) values(p_household_id,extensions.digest(v_token,'sha256'),auth.uid());
    return v_token;
end;
$$;
create function loom_private.join_household(p_token text) returns void
language plpgsql security definer set search_path='' as $$
declare v_invite loom_private.household_invites;
begin
    if auth.uid() is null then raise exception 'Please sign in.'; end if;
    select * into v_invite from loom_private.household_invites where token_hash=extensions.digest(p_token,'sha256') for update;
    if not found or v_invite.used_at is not null or v_invite.expires_at<=now() then raise exception 'This invitation is invalid or has expired.'; end if;
    if loom_private.is_member(v_invite.household_id) then raise exception 'You are already in this household.'; end if;
    if (select count(*) from public.household_members where household_id=v_invite.household_id and active)>=100 then raise exception 'This household is full.'; end if;
    insert into public.household_members(household_id,user_id,role) values(v_invite.household_id,auth.uid(),'member')
    on conflict(household_id,user_id) do update set active=true,role='member';
    update loom_private.household_invites set used_at=now(),used_by=auth.uid() where id=v_invite.id;
end;
$$;
create function loom_private.household_members_with_names(p_household_id uuid) returns table(user_id uuid,display_name text,role text)
language plpgsql stable security definer set search_path='' as $$
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'Household access denied.'; end if;
    return query select m.user_id,p.display_name,m.role from public.household_members m join public.profiles p on p.id=m.user_id where m.household_id=p_household_id and m.active order by m.joined_at;
end;
$$;
create function loom_private.next_month_date(p_date date,p_anchor integer) returns date
language sql immutable set search_path='' as $$
    select (date_trunc('month',p_date)+interval '1 month')::date +
        (least(p_anchor,extract(day from date_trunc('month',p_date)+interval '2 months - 1 day')::integer)-1);
$$;

create function loom_private.create_expense(p_id uuid,p_household_id uuid,p_title text,p_amount_minor bigint,p_paid_by uuid,p_category text,p_occurred_on date,p_recurring boolean,p_shares jsonb) returns uuid
language plpgsql security definer set search_path='' as $$
declare v_rule uuid; v_sum numeric; v_existing public.expenses;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'Household access denied.'; end if;
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
create function loom_private.record_settlement(p_id uuid,p_household_id uuid,p_from uuid,p_to uuid,p_amount_minor bigint,p_occurred_on date) returns uuid
language plpgsql security definer set search_path='' as $$
declare v_existing public.settlements;
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) or auth.uid() not in(p_from,p_to) then raise exception 'You can only record repayments you send or receive.'; end if;
    if p_from=p_to or p_amount_minor not between 1 and 100000000000 then raise exception 'Choose two people and a positive amount.'; end if;
    if (select count(*) from public.household_members where household_id=p_household_id and user_id in(p_from,p_to) and active)<>2 then raise exception 'Choose current household members.'; end if;
    select * into v_existing from public.settlements where id=p_id;
    if found then
        if v_existing.household_id=p_household_id and v_existing.created_by=auth.uid() and v_existing.from_user=p_from and v_existing.to_user=p_to and v_existing.amount_minor=p_amount_minor and v_existing.occurred_on=p_occurred_on then return p_id; end if;
        raise exception 'This repayment ID has already been used.';
    end if;
    insert into public.settlements(id,household_id,from_user,to_user,amount_minor,occurred_on,created_by) values(p_id,p_household_id,p_from,p_to,p_amount_minor,p_occurred_on,auth.uid());
    return p_id;
end;
$$;
create function loom_private.complete_reminder(p_reminder_id uuid,p_completed boolean,p_expected_due_at timestamptz) returns void
language plpgsql security definer set search_path='' as $$
declare v_reminder public.reminders; v_zone text;
begin
    select * into v_reminder from public.reminders where id=p_reminder_id for update;
    if auth.uid() is null or not found or not loom_private.is_member(v_reminder.household_id) then raise exception 'Reminder access denied.'; end if;
    if v_reminder.due_at<>p_expected_due_at then return; end if;
    if p_completed and not v_reminder.completed and v_reminder.recurrence<>'none' then
        select time_zone into v_zone from public.households where id=v_reminder.household_id;
        update public.reminders set due_at=((due_at at time zone v_zone)+case when recurrence='weekly' then interval '7 days' else interval '1 day' end) at time zone v_zone,completed=false where id=p_reminder_id;
    else update public.reminders set completed=p_completed where id=p_reminder_id;
    end if;
end;
$$;

create function loom_private.register_push_device(p_device_id uuid,p_token text,p_platform text) returns void
language plpgsql security definer set search_path='' as $$
begin
    if auth.uid() is null then raise exception 'Please sign in.'; end if;
    if exists(select 1 from public.push_devices where id=p_device_id and user_id<>auth.uid()) then raise exception 'Device access denied.'; end if;
    delete from public.push_devices where user_id=auth.uid() and token=p_token and id<>p_device_id;
    insert into public.push_devices(id,user_id,token,platform) values(p_device_id,auth.uid(),p_token,p_platform)
    on conflict(id) do update set token=excluded.token,platform=excluded.platform,updated_at=now();
    insert into public.notification_preferences(user_id,enabled) values(auth.uid(),true) on conflict(user_id) do update set enabled=true;
end;
$$;
create function loom_private.disable_notifications(p_device_id uuid) returns void
language plpgsql security definer set search_path='' as $$
begin
    if auth.uid() is null then raise exception 'Please sign in.'; end if;
    delete from public.push_devices where id=p_device_id and user_id=auth.uid();
    insert into public.notification_preferences(user_id,enabled) values(auth.uid(),false) on conflict(user_id) do update set enabled=false;
end;
$$;
create function loom_private.unregister_push_device(p_device_id uuid) returns void
language plpgsql security definer set search_path='' as $$
begin
    if auth.uid() is null then raise exception 'Please sign in.'; end if;
    delete from public.push_devices where id=p_device_id and user_id=auth.uid();
end;
$$;

create function loom_private.snapshot_month(p_household_id uuid,p_month date) returns uuid
language plpgsql security definer set search_path='' as $$
declare v_payload jsonb; v_hash text; v_id uuid; v_revision integer;
begin
    if extract(day from p_month)<>1 then raise exception 'Use the first day of the month.'; end if;
    perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(p_household_id::text||p_month::text,0));
    select jsonb_build_object(
        'month',p_month,'currency',h.currency,
        'total_minor',coalesce((select sum(amount_minor) from public.expenses where household_id=h.id and occurred_on>=p_month and occurred_on<(p_month+interval '1 month')::date),0),
        'categories',coalesce((select jsonb_object_agg(category,total) from(select category,sum(amount_minor) total from public.expenses where household_id=h.id and occurred_on>=p_month and occurred_on<(p_month+interval '1 month')::date group by category)c),'{}'::jsonb),
        'members',coalesce((select jsonb_agg(jsonb_build_object('user_id',m.user_id,'name',p.display_name,
            'paid_minor',coalesce((select sum(e.amount_minor) from public.expenses e where e.household_id=h.id and e.paid_by=m.user_id and e.occurred_on>=p_month and e.occurred_on<(p_month+interval '1 month')::date),0),
            'share_minor',coalesce((select sum(s.amount_minor) from public.expense_shares s join public.expenses e on e.id=s.expense_id where s.household_id=h.id and s.user_id=m.user_id and e.occurred_on>=p_month and e.occurred_on<(p_month+interval '1 month')::date),0),
            'opening_balance_minor',coalesce((select sum(l.delta_minor) from public.household_ledger_entries l where l.household_id=h.id and l.user_id=m.user_id and l.occurred_on<p_month),0),
            'closing_balance_minor',coalesce((select sum(l.delta_minor) from public.household_ledger_entries l where l.household_id=h.id and l.user_id=m.user_id and l.occurred_on<(p_month+interval '1 month')::date),0)
        ) order by m.user_id) from public.household_members m join public.profiles p on p.id=m.user_id where m.household_id=h.id),'[]'::jsonb)
    ) into v_payload from public.households h where h.id=p_household_id;
    if v_payload is null then raise exception 'Unknown household.'; end if;
    v_hash:=encode(extensions.digest(v_payload::text,'sha256'),'hex');
    select id into v_id from public.monthly_statements where household_id=p_household_id and month=p_month and content_hash=v_hash;
    if found then return v_id; end if;
    select coalesce(max(revision),0)+1 into v_revision from public.monthly_statements where household_id=p_household_id and month=p_month;
    insert into public.monthly_statements(household_id,month,revision,payload,content_hash) values(p_household_id,p_month,v_revision,v_payload,v_hash) returning id into v_id;
    return v_id;
end;
$$;
create function loom_private.save_monthly_statement(p_household_id uuid,p_month date) returns uuid
language plpgsql security definer set search_path='' as $$
begin
    if auth.uid() is null or not loom_private.is_member(p_household_id) then raise exception 'Household access denied.'; end if;
    return loom_private.snapshot_month(p_household_id,p_month);
end;
$$;

-- Exposed functions are SECURITY INVOKER wrappers. Only named implementations are granted.
create function public.create_household(p_name text,p_currency text,p_time_zone text) returns uuid language sql security invoker set search_path='' as $$select loom_private.create_household(p_name,p_currency,p_time_zone);$$;
create function public.create_household_invite(p_household_id uuid) returns text language sql security invoker set search_path='' as $$select loom_private.create_household_invite(p_household_id);$$;
create function public.join_household(p_token text) returns void language sql security invoker set search_path='' as $$select loom_private.join_household(p_token);$$;
create function public.household_members_with_names(p_household_id uuid) returns table(user_id uuid,display_name text,role text) language sql stable security invoker set search_path='' as $$select * from loom_private.household_members_with_names(p_household_id);$$;
create function public.create_expense(p_id uuid,p_household_id uuid,p_title text,p_amount_minor bigint,p_paid_by uuid,p_category text,p_occurred_on date,p_recurring boolean,p_shares jsonb) returns uuid language sql security invoker set search_path='' as $$select loom_private.create_expense(p_id,p_household_id,p_title,p_amount_minor,p_paid_by,p_category,p_occurred_on,p_recurring,p_shares);$$;
create function public.record_settlement(p_id uuid,p_household_id uuid,p_from uuid,p_to uuid,p_amount_minor bigint,p_occurred_on date) returns uuid language sql security invoker set search_path='' as $$select loom_private.record_settlement(p_id,p_household_id,p_from,p_to,p_amount_minor,p_occurred_on);$$;
create function public.complete_reminder(p_reminder_id uuid,p_completed boolean,p_expected_due_at timestamptz) returns void language sql security invoker set search_path='' as $$select loom_private.complete_reminder(p_reminder_id,p_completed,p_expected_due_at);$$;
create function public.register_push_device(p_device_id uuid,p_token text,p_platform text) returns void language sql security invoker set search_path='' as $$select loom_private.register_push_device(p_device_id,p_token,p_platform);$$;
create function public.disable_notifications(p_device_id uuid) returns void language sql security invoker set search_path='' as $$select loom_private.disable_notifications(p_device_id);$$;
create function public.unregister_push_device(p_device_id uuid) returns void language sql security invoker set search_path='' as $$select loom_private.unregister_push_device(p_device_id);$$;
create function public.save_monthly_statement(p_household_id uuid,p_month date) returns uuid language sql security invoker set search_path='' as $$select loom_private.save_monthly_statement(p_household_id,p_month);$$;

revoke all on all functions in schema loom_private from public,anon,authenticated;
grant execute on function loom_private.is_member(uuid),loom_private.is_owner(uuid),loom_private.can_view_profile(uuid) to authenticated;
do $$declare f record;begin
    for f in select p.oid::regprocedure as signature,n.nspname from pg_proc p join pg_namespace n on n.oid=p.pronamespace
        where n.nspname in('public','loom_private') and p.proname in('create_household','create_household_invite','join_household','household_members_with_names','create_expense','record_settlement','complete_reminder','register_push_device','disable_notifications','unregister_push_device','save_monthly_statement')
    loop execute format('revoke all on function %s from public,anon',f.signature);execute format('grant execute on function %s to authenticated',f.signature);end loop;
end;$$;

-- Realtime only publishes tables that clients are authorized to read.
do $$begin
    if exists(select 1 from pg_publication where pubname='supabase_realtime') then
        alter publication supabase_realtime add table public.shopping_items,public.reminders,public.expenses,public.settlements,public.feed_articles;
    end if;
end;$$;
commit;
