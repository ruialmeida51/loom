-- Tasks own the records. The legacy view keeps dated reminder clients and push workers compatible.
alter table public.reminders rename to tasks;
alter table public.tasks alter column due_at drop not null;
alter table public.tasks add column notification_enabled boolean not null default true;
alter table public.tasks add constraint tasks_repeat_requires_date
    check (due_at is not null or recurrence='none');
alter table public.tasks add constraint tasks_notification_requires_date
    check (not notification_enabled or due_at is not null);
alter table public.tasks enable row level security;

create view public.reminders with (security_invoker=true) as
    select id,household_id,title,due_at,recurrence,completed,created_by,created_at
    from public.tasks
    where due_at is not null and notification_enabled
    with local check option;
revoke all on public.reminders from public,anon;
grant select,insert,update,delete on public.reminders to authenticated;
grant all on public.reminders to service_role;

create function loom_private.complete_task(p_task_id uuid,p_completed boolean,p_expected_due_at timestamptz)
returns void language plpgsql security definer set search_path='' as $$
declare v_task public.tasks; v_zone text;
begin
    select * into v_task from public.tasks where id=p_task_id for update;
    if auth.uid() is null or not found or not loom_private.is_member(v_task.household_id) then
        raise exception 'Task access denied.';
    end if;
    -- A stale completion cannot advance another occurrence, including a task that gained a date.
    if v_task.due_at is distinct from p_expected_due_at then return; end if;
    if p_completed and not v_task.completed and v_task.recurrence<>'none' then
        select time_zone into v_zone from public.households where id=v_task.household_id;
        update public.tasks
        set due_at=((due_at at time zone v_zone)+case when recurrence='weekly' then interval '7 days' else interval '1 day' end) at time zone v_zone,
            completed=false
        where id=p_task_id;
    else
        update public.tasks set completed=p_completed where id=p_task_id;
    end if;
end;
$$;
create function public.complete_task(p_task_id uuid,p_completed boolean,p_expected_due_at timestamptz default null)
returns void language sql security invoker set search_path='' as
$$select loom_private.complete_task(p_task_id,p_completed,p_expected_due_at);$$;

create or replace function loom_private.complete_reminder(p_reminder_id uuid,p_completed boolean,p_expected_due_at timestamptz)
returns void language sql security definer set search_path='' as
$$select loom_private.complete_task(p_reminder_id,p_completed,p_expected_due_at);$$;

revoke all on function loom_private.complete_task(uuid,boolean,timestamptz) from public,anon;
revoke all on function public.complete_task(uuid,boolean,timestamptz) from public,anon;
grant execute on function loom_private.complete_task(uuid,boolean,timestamptz) to authenticated;
grant execute on function public.complete_task(uuid,boolean,timestamptz) to authenticated;
-- The renamed table retains its membership policies, grants, scope trigger and realtime publication.
