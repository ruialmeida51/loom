begin;

create table loom_private.worker_auth (
    singleton boolean primary key default true check(singleton),
    secret_hash bytea not null
);
alter table loom_private.worker_auth enable row level security;
revoke all on loom_private.worker_auth from public,anon,authenticated;
grant select on loom_private.worker_auth to service_role;
create function loom_private.verify_loom_worker_secret(p_secret text) returns boolean
language sql stable security definer set search_path='' as $$
    select length(p_secret) between 32 and 256 and exists(
        select 1 from loom_private.worker_auth where singleton and secret_hash=extensions.digest(p_secret,'sha256')
    );
$$;
create function public.verify_loom_worker_secret(p_secret text) returns boolean
language sql stable security invoker set search_path='' as $$select loom_private.verify_loom_worker_secret(p_secret);$$;
revoke all on function loom_private.verify_loom_worker_secret(text),public.verify_loom_worker_secret(text) from public,anon,authenticated;
grant execute on function loom_private.verify_loom_worker_secret(text),public.verify_loom_worker_secret(text) to service_role;


-- This worker is not granted to authenticated/anon. It is callable only with server privileges.
create function loom_private.queue_due_work() returns void
language plpgsql security definer set search_path='' as $$
declare rule public.recurring_expenses; household public.households; v_today date; v_expense uuid; v_month date; v_loops integer;
begin
    insert into public.notifications(household_id,user_id,event_key,title,body,data)
    select r.household_id,m.user_id,'reminder:'||r.id||':'||r.due_at,'A gentle reminder','Something needs a little attention at home.',
           jsonb_build_object('route','reminders','household_id',r.household_id,'reminder_id',r.id,'due_at',r.due_at)
    from public.reminders r join public.household_members m on m.household_id=r.household_id and m.active
    where not r.completed and r.due_at<=now() and r.due_at>now()-interval '24 hours'
    on conflict(user_id,event_key) do nothing;

    for rule in select r.* from public.recurring_expenses r where r.active order by r.next_on for update skip locked loop
        select * into household from public.households where id=rule.household_id;
        v_today:=(now() at time zone household.time_zone)::date;
        if not exists(select 1 from public.household_members where household_id=rule.household_id and user_id=rule.paid_by and active) or
           not exists(select 1 from public.household_members where household_id=rule.household_id and user_id=rule.created_by and active) or
           exists(select 1 from jsonb_each(rule.shares) s where not exists(select 1 from public.household_members m where m.household_id=rule.household_id and m.user_id=s.key::uuid and m.active)) then
            update public.recurring_expenses set active=false where id=rule.id;
            continue;
        end if;
        v_loops:=0;
        while rule.next_on<=v_today and v_loops<24 loop
            v_expense:=gen_random_uuid();
            insert into public.expenses(id,household_id,title,amount_minor,paid_by,category,occurred_on,recurring_rule_id,created_by)
            values(v_expense,rule.household_id,rule.title,rule.amount_minor,rule.paid_by,rule.category,rule.next_on,rule.id,rule.created_by)
            on conflict(recurring_rule_id,occurred_on) do nothing;
            if found then
                insert into public.expense_shares(expense_id,household_id,user_id,amount_minor)
                select v_expense,rule.household_id,key::uuid,value::text::bigint from jsonb_each(rule.shares);
                insert into public.notifications(household_id,user_id,event_key,title,body,data)
                select rule.household_id,m.user_id,'expense:'||v_expense,'A recurring bill was added','Your household’s recurring bill is ready to review.',jsonb_build_object('route','money','household_id',rule.household_id)
                from public.household_members m where m.household_id=rule.household_id and m.active
                on conflict(user_id,event_key) do nothing;
            end if;
            rule.next_on:=loom_private.next_month_date(rule.next_on,rule.anchor_day);
            v_loops:=v_loops+1;
        end loop;
        update public.recurring_expenses set next_on=rule.next_on where id=rule.id;
    end loop;

    for household in select * from public.households loop
        v_month:=(date_trunc('month',now() at time zone household.time_zone)-interval '1 month')::date;
        if household.created_at::date>=(v_month+interval '1 month')::date then continue; end if;
        insert into loom_private.statement_runs(household_id,month) values(household.id,v_month) on conflict do nothing;
        if found then
            perform loom_private.snapshot_month(household.id,v_month);
            insert into public.notifications(household_id,user_id,event_key,title,body,data)
            select household.id,m.user_id,'statement:'||household.id||':'||v_month,'Your monthly statement is ready','A little perspective on the month you shared.',jsonb_build_object('route','insights','household_id',household.id)
            from public.household_members m where m.household_id=household.id and m.active on conflict(user_id,event_key) do nothing;
        end if;
    end loop;
end;
$$;

create function loom_private.claim_notification_deliveries(p_limit integer default 50)
returns table(delivery_id uuid,device_id uuid,token text,platform text,notification_id uuid,title text,body text,data jsonb)
language plpgsql security definer set search_path='' as $$
begin
    perform loom_private.queue_due_work();
    insert into loom_private.notification_deliveries(notification_id,device_id)
    select n.id,d.id from public.notifications n join public.push_devices d on d.user_id=n.user_id
    join public.notification_preferences p on p.user_id=n.user_id and p.enabled
    join public.household_members m on m.user_id=n.user_id and m.household_id=n.household_id and m.active
    where n.created_at>now()-interval '24 hours' and d.updated_at<=n.created_at
    on conflict do nothing;

    -- Permission changes and rescheduled reminders are checked again immediately before delivery.
    update loom_private.notification_deliveries delivery set status='dead',last_error='No longer authorized or superseded'
    from public.notifications n,public.push_devices d
    where delivery.notification_id=n.id and delivery.device_id=d.id and delivery.status in('pending','sending') and (
        not exists(select 1 from public.household_members m where m.household_id=n.household_id and m.user_id=n.user_id and m.active)
        or not exists(select 1 from public.notification_preferences p where p.user_id=n.user_id and p.enabled)
        or (n.data ? 'reminder_id' and not exists(select 1 from public.reminders r where r.id=(n.data->>'reminder_id')::uuid and not r.completed and r.due_at=(n.data->>'due_at')::timestamptz))
    );
    return query with candidates as (
        select d.id from loom_private.notification_deliveries d
        where ((d.status='pending' and d.next_attempt_at<=now()) or (d.status='sending' and d.lease_until<now())) and d.attempts<8
        order by d.next_attempt_at for update skip locked limit greatest(1,least(p_limit,100))
    ),claimed as (
        update loom_private.notification_deliveries d set status='sending',lease_until=now()+interval '5 minutes',attempts=d.attempts+1
        from candidates c where c.id=d.id returning d.*
    )select c.id,d.id,d.token,d.platform,n.id,n.title,n.body,n.data
    from claimed c join public.push_devices d on d.id=c.device_id join public.notifications n on n.id=c.notification_id;
end;
$$;
create function loom_private.finish_notification_delivery(p_id uuid,p_result text,p_error text default null) returns void
language plpgsql security definer set search_path='' as $$
begin
    if p_result not in('sent','retry','dead') then raise exception 'Invalid result.'; end if;
    update loom_private.notification_deliveries set
        status=case when p_result='sent' then 'sent' when p_result='dead' or attempts>=8 then 'dead' else 'pending' end,
        lease_until=null,last_error=left(p_error,300),
        next_attempt_at=now()+make_interval(secs=>least(3600,(power(2,attempts)*15)::integer))
    where id=p_id and status='sending';
    if p_result='dead' then
        delete from public.push_devices d using loom_private.notification_deliveries delivery
        where delivery.id=p_id and d.id=delivery.device_id;
    end if;
end;
$$;
create function public.claim_notification_deliveries(p_limit integer default 50)
returns table(delivery_id uuid,device_id uuid,token text,platform text,notification_id uuid,title text,body text,data jsonb)
language sql security invoker set search_path='' as $$select * from loom_private.claim_notification_deliveries(p_limit);$$;
create function public.finish_notification_delivery(p_id uuid,p_result text,p_error text default null) returns void
language sql security invoker set search_path='' as $$select loom_private.finish_notification_delivery(p_id,p_result,p_error);$$;

revoke all on function loom_private.queue_due_work(),loom_private.claim_notification_deliveries(integer),
    loom_private.finish_notification_delivery(uuid,text,text),public.claim_notification_deliveries(integer),
    public.finish_notification_delivery(uuid,text,text) from public,anon,authenticated;
grant execute on function loom_private.claim_notification_deliveries(integer),loom_private.finish_notification_delivery(uuid,text,text),
    public.claim_notification_deliveries(integer),public.finish_notification_delivery(uuid,text,text) to service_role;
commit;
