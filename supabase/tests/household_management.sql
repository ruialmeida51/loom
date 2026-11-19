begin;
insert into auth.users(id,email) values
('00000000-0000-4000-8000-000000007001','management-owner@example.test'),
('00000000-0000-4000-8000-000000007002','management-member@example.test'),
('00000000-0000-4000-8000-000000007003','management-coowner@example.test'),
('00000000-0000-4000-8000-000000007004','management-outsider@example.test');
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000007001';
select set_config('loom.manage.home',public.create_household('Management home','EUR','UTC')::text,true);
select set_config('loom.manage.other',public.create_household('Other home','USD','UTC')::text,true);
select set_config('loom.manage.invitation',public.manage_household_invitation(current_setting('loom.manage.home')::uuid,'replace',null,null,'00000000-0000-4000-8000-000000007010')::text,true);
do $$ declare invite jsonb := current_setting('loom.manage.invitation')::jsonb; begin
    assert invite->>'code' ~ '^[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}$';
    assert (invite->>'reusable')::boolean;
    assert public.get_household_management(current_setting('loom.manage.home')::uuid)->'invitation' = invite,'Owner can redisplay current invitation';
    assert public.manage_household_invitation(current_setting('loom.manage.home')::uuid,'replace',null,null,'00000000-0000-4000-8000-000000007010') = invite,'Lost create response is retryable';
    begin
        perform public.manage_household_invitation(current_setting('loom.manage.home')::uuid,'replace',null,null,'00000000-0000-4000-8000-000000007011');
        raise exception 'Stale create rotated the current code';
    exception when raise_exception then if sqlerrm <> 'household.invitation_changed' then raise; end if; end;
end; $$;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000007002';
do $$ begin
    assert public.redeem_household_invitation(current_setting('loom.manage.invitation')::jsonb->>'code')->>'status' = 'joined';
    assert public.redeem_household_invitation(current_setting('loom.manage.invitation')::jsonb->>'code')->>'status' = 'already_joined';
    assert public.get_household_management(current_setting('loom.manage.home')::uuid)->'invitation' = 'null'::jsonb,'Members cannot recover invitation codes';
    begin
        perform public.change_household_member(current_setting('loom.manage.home')::uuid,'00000000-0000-4000-8000-000000007001','demote',1,'00000000-0000-4000-8000-000000007012');
        raise exception 'Member changed owner permissions';
    exception when raise_exception then if sqlerrm <> 'household.owner_required' then raise; end if; end;
end; $$;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000007003';
do $$ begin
    assert public.redeem_household_invitation(current_setting('loom.manage.invitation')::jsonb->>'code')->>'status' = 'joined','A second roommate can reuse the same code';
end; $$;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000007004';
do $$ begin
    begin perform public.get_household_management(current_setting('loom.manage.home')::uuid); raise exception 'Outsider read management';
    exception when raise_exception then if sqlerrm <> 'household.access_denied' then raise; end if; end;
end; $$;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000007001';
do $$ begin
    begin
        perform public.change_household_member(current_setting('loom.manage.home')::uuid,auth.uid(),'leave',1,'00000000-0000-4000-8000-000000007013');
        raise exception 'Last owner left';
    exception when raise_exception then if sqlerrm <> 'household.last_owner' then raise; end if; end;
end; $$;
select public.change_household_member(current_setting('loom.manage.home')::uuid,'00000000-0000-4000-8000-000000007003','promote',1,'00000000-0000-4000-8000-000000007014');
select public.change_household_member(current_setting('loom.manage.home')::uuid,'00000000-0000-4000-8000-000000007003','promote',1,'00000000-0000-4000-8000-000000007014');
do $$ begin
    assert (select count(*) from public.household_members where household_id=current_setting('loom.manage.home')::uuid and role='owner' and active)=2,'Promotion adds a co-owner';
    assert (select version from public.household_member_history(current_setting('loom.manage.home')::uuid) where user_id='00000000-0000-4000-8000-000000007003')=2,'Replay does not apply twice';
end; $$;
select public.change_household_member(current_setting('loom.manage.home')::uuid,'00000000-0000-4000-8000-000000007003','demote',2,'00000000-0000-4000-8000-000000007015');
select public.change_household_member(current_setting('loom.manage.home')::uuid,'00000000-0000-4000-8000-000000007003','promote',3,'00000000-0000-4000-8000-000000007016');
select public.save_account_expense('00000000-0000-4000-8000-000000007020',current_setting('loom.manage.home')::uuid,current_setting('loom.manage.home')::uuid,
    'Monthly bill',3000,'00000000-0000-4000-8000-000000007002','Other',current_date,true,
    '{"00000000-0000-4000-8000-000000007001":1500,"00000000-0000-4000-8000-000000007002":1500}',0,'equal','{}');
select public.save_expense_account(current_setting('loom.manage.home')::uuid,'00000000-0000-4000-8000-000000007030','Holiday','USD',
    array['00000000-0000-4000-8000-000000007001','00000000-0000-4000-8000-000000007002']::uuid[],false,0);
select public.save_account_expense('00000000-0000-4000-8000-000000007031',current_setting('loom.manage.home')::uuid,'00000000-0000-4000-8000-000000007030',
    'Holiday groceries',1000,'00000000-0000-4000-8000-000000007001','Other',current_date,false,
    '{"00000000-0000-4000-8000-000000007001":500,"00000000-0000-4000-8000-000000007002":500}',0,'equal','{}');
select set_config('loom.manage.rule',(select recurring_rule_id::text from public.expenses where id='00000000-0000-4000-8000-000000007020'),true);
select set_config('loom.manage.photo',current_setting('loom.manage.home')||'/'||current_setting('loom.manage.home')||'/00000000-0000-4000-8000-000000007020/00000000-0000-4000-8000-000000007021.jpg',true);
insert into storage.objects(bucket_id,name,owner_id) values('expense-receipts',current_setting('loom.manage.photo'),auth.uid()::text);
select public.save_expense_receipt(current_setting('loom.manage.home')::uuid,current_setting('loom.manage.home')::uuid,'00000000-0000-4000-8000-000000007020',current_setting('loom.manage.photo'),0);
do $$ declare schedule public.recurring_expenses; begin
    select * into schedule from public.control_recurring_expense(current_setting('loom.manage.home')::uuid,current_setting('loom.manage.rule')::uuid,'pause',1);
    select * into schedule from public.save_recurring_expense(current_setting('loom.manage.home')::uuid,schedule.id,schedule.version,'Updated bill',4000,schedule.paid_by,schedule.next_on,
        '{"00000000-0000-4000-8000-000000007001":2000,"00000000-0000-4000-8000-000000007002":2000}','equal','{}');
    assert not schedule.active,'Saving a paused bill keeps it paused';
    assert (select amount_minor from public.expenses where id='00000000-0000-4000-8000-000000007020')=3000,'History is unchanged';
    begin
        perform public.save_recurring_expense(current_setting('loom.manage.home')::uuid,schedule.id,1,'Stale update',4000,schedule.paid_by,schedule.next_on,schedule.shares,'equal','{}');
        raise exception 'Stale schedule edit overwrote changes';
    exception when raise_exception then if sqlerrm <> 'expenses.message_this_schedule_changed_refresh_and_try_again' then raise; end if; end;
end; $$;
-- Old cancelled schedules can be deleted without removing their recorded expense.
select public.save_account_expense('00000000-0000-4000-8000-000000007032',current_setting('loom.manage.home')::uuid,current_setting('loom.manage.home')::uuid,
    'Retired bill',700,'00000000-0000-4000-8000-000000007001','Other',current_date,true,
    '{"00000000-0000-4000-8000-000000007001":700}',0,'equal','{}');
do $$ declare schedule public.recurring_expenses; begin
    select r.* into schedule from public.recurring_expenses r join public.expenses e on e.recurring_rule_id=r.id where e.id='00000000-0000-4000-8000-000000007032';
    select * into schedule from public.control_recurring_expense(current_setting('loom.manage.home')::uuid,schedule.id,'cancel',schedule.version);
    perform public.delete_recurring_expense(current_setting('loom.manage.home')::uuid,schedule.id,schedule.version);
    assert exists(select 1 from public.expenses where id='00000000-0000-4000-8000-000000007032'),'Cancelled schedule deletion keeps history';
end; $$;
-- Hash-only legacy invitations keep their original single-use rules and have no recoverable code.
select set_config('loom.manage.legacy',public.create_household_invite(current_setting('loom.manage.other')::uuid),true);
do $$ begin
    assert public.get_household_management(current_setting('loom.manage.other')::uuid)->'invitation'->'code'='null'::jsonb;
end; $$;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000007004';
do $$ begin
    assert public.redeem_household_invitation(current_setting('loom.manage.legacy'))->>'status'='joined';
    assert public.redeem_household_invitation(current_setting('loom.manage.legacy'))->>'status'='invalid','Legacy code is consumed once';
end; $$;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000007001';
-- Departure with debt is allowed and revokes the reusable invitation.
select public.change_household_member(current_setting('loom.manage.home')::uuid,'00000000-0000-4000-8000-000000007002','remove',1,'00000000-0000-4000-8000-000000007022');
select public.change_household_member(current_setting('loom.manage.home')::uuid,'00000000-0000-4000-8000-000000007002','remove',1,'00000000-0000-4000-8000-000000007022');
do $$ declare schedule public.recurring_expenses; positions jsonb; begin
    assert (select count(*) from public.expense_shares where user_id='00000000-0000-4000-8000-000000007002')=2,'Financial attribution remains';
    positions := public.member_account_positions(current_setting('loom.manage.home')::uuid,'00000000-0000-4000-8000-000000007002');
    assert (positions->0->>'balance_minor')::bigint=1500,'Former member balance remains visible';
    assert jsonb_array_length(positions)=2 and positions->0->>'currency'='EUR' and positions->1->>'currency'='USD','Balances remain separate by account/currency';
    assert (positions->1->>'balance_minor')::bigint=-500;
    assert (select display_name from public.household_member_history(current_setting('loom.manage.home')::uuid) where user_id='00000000-0000-4000-8000-000000007002') is not null,'Former names are retained';
    select * into schedule from public.recurring_expenses where id=current_setting('loom.manage.rule')::uuid;
    select * into schedule from public.control_recurring_expense(current_setting('loom.manage.home')::uuid,schedule.id,'resume',schedule.version);
    assert schedule.active,'Existing bills can resume with their retained former payer/share';
end; $$;
-- Existing financial records and account names remain editable with retained identities.
select public.save_account_expense('00000000-0000-4000-8000-000000007020',current_setting('loom.manage.home')::uuid,current_setting('loom.manage.home')::uuid,
    'Corrected monthly bill',3000,'00000000-0000-4000-8000-000000007002','Other',current_date,true,
    '{"00000000-0000-4000-8000-000000007001":1500,"00000000-0000-4000-8000-000000007002":1500}',1,'equal','{}');
select public.save_expense_account(current_setting('loom.manage.home')::uuid,'00000000-0000-4000-8000-000000007030','Holiday renamed','USD',
    array['00000000-0000-4000-8000-000000007001','00000000-0000-4000-8000-000000007002']::uuid[],false,1);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000007002';
do $$ begin
    assert public.redeem_household_invitation(current_setting('loom.manage.invitation')::jsonb->>'code')->>'status'='invalid','Old code cannot undo removal';
    assert (select count(*) from public.expenses)=0,'Removed member loses access';
    begin perform public.get_household_management(current_setting('loom.manage.home')::uuid); raise exception 'Removed member retained management access';
    exception when raise_exception then if sqlerrm <> 'household.access_denied' then raise; end if; end;
end; $$;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000007001';
-- Remaining participants can record real repayments with a former member.
select public.record_account_settlement('00000000-0000-4000-8000-000000007023',current_setting('loom.manage.home')::uuid,current_setting('loom.manage.home')::uuid,
    auth.uid(),'00000000-0000-4000-8000-000000007002',1000,current_date);
select public.change_household_member(current_setting('loom.manage.home')::uuid,auth.uid(),'leave',1,'00000000-0000-4000-8000-000000007024');
select public.change_household_member(current_setting('loom.manage.home')::uuid,auth.uid(),'leave',1,'00000000-0000-4000-8000-000000007024');
reset role;
update public.recurring_expenses set next_on=current_date-1 where id=current_setting('loom.manage.rule')::uuid;
select loom_private.queue_due_work();
do $$ begin
    assert (select active from public.recurring_expenses where id=current_setting('loom.manage.rule')::uuid),'No automatic pause for former payer, participant or creator';
    assert (select count(*) from public.expenses where recurring_rule_id=current_setting('loom.manage.rule')::uuid)=2,'Future charges continue unchanged';
    assert (select paid_by from public.expenses where recurring_rule_id=current_setting('loom.manage.rule')::uuid order by occurred_on desc limit 1)='00000000-0000-4000-8000-000000007002'::uuid;
end; $$;
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000007003';
do $$ declare schedule public.recurring_expenses; begin
    select * into schedule from public.recurring_expenses where id=current_setting('loom.manage.rule')::uuid;
    perform public.delete_recurring_expense(current_setting('loom.manage.home')::uuid,schedule.id,schedule.version);
    perform public.delete_recurring_expense(current_setting('loom.manage.home')::uuid,schedule.id,schedule.version);
    assert (select count(*) from public.recurring_expenses where id=schedule.id)=0,'Deleted schedule is hidden';
    assert (select count(*) from public.expenses where recurring_rule_id=schedule.id)=2,'Schedule deletion preserves generated history';
    begin perform public.delete_household(current_setting('loom.manage.home')::uuid,'Wrong name','00000000-0000-4000-8000-000000007025'); raise exception 'Wrong name deleted household';
    exception when raise_exception then if sqlerrm <> 'household.name_confirmation' then raise; end if; end;
end; $$;
select public.delete_household(current_setting('loom.manage.home')::uuid,'Management home','00000000-0000-4000-8000-000000007026');
select public.delete_household(current_setting('loom.manage.home')::uuid,'Management home','00000000-0000-4000-8000-000000007026');
reset role;
do $$ begin
    assert not exists(select 1 from public.households where id=current_setting('loom.manage.home')::uuid),'Confirmed household was deleted';
    assert exists(select 1 from public.households where id=current_setting('loom.manage.other')::uuid),'Other household remains';
    assert exists(select 1 from public.profiles where id='00000000-0000-4000-8000-000000007003'),'Global profile remains';
    assert exists(select 1 from loom_private.receipt_deletions where object_path=current_setting('loom.manage.photo')),'Receipt cleanup survives household deletion';
    assert not exists(select 1 from public.expenses where household_id=current_setting('loom.manage.home')::uuid);
    assert not exists(select 1 from loom_private.household_invites where household_id=current_setting('loom.manage.home')::uuid);
end; $$;
rollback;
