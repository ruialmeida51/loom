begin;
insert into auth.users(id, email) values
 ('00000000-0000-4000-8000-000000000061', 'repayment-owner@example.test'),
 ('00000000-0000-4000-8000-000000000062', 'repayment-sender@example.test'),
 ('00000000-0000-4000-8000-000000000063', 'repayment-recipient@example.test'),
 ('00000000-0000-4000-8000-000000000064', 'repayment-other@example.test'),
 ('00000000-0000-4000-8000-000000000065', 'repayment-outsider@example.test');
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000061';
select set_config('loom.repayments.home', public.create_household('Repayments home', 'EUR', 'UTC')::text, true);
select set_config('loom.repayments.invite', public.create_household_invite(current_setting('loom.repayments.home')::uuid), true);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000062';
select public.redeem_household_invitation(current_setting('loom.repayments.invite'));
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000061';
select set_config('loom.repayments.invite', public.create_household_invite(current_setting('loom.repayments.home')::uuid), true);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000063';
select public.redeem_household_invitation(current_setting('loom.repayments.invite'));
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000061';
select set_config('loom.repayments.invite', public.create_household_invite(current_setting('loom.repayments.home')::uuid), true);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000064';
select public.redeem_household_invitation(current_setting('loom.repayments.invite'));
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000062';
select public.record_account_settlement('00000000-0000-4000-8000-000000000661',
 current_setting('loom.repayments.home')::uuid, current_setting('loom.repayments.home')::uuid,
 '00000000-0000-4000-8000-000000000062', '00000000-0000-4000-8000-000000000063', 2500, current_date);

set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000064';
do $$ begin
 begin
  perform public.delete_account_settlement(current_setting('loom.repayments.home')::uuid,
   current_setting('loom.repayments.home')::uuid, '00000000-0000-4000-8000-000000000661', 1);
  raise exception 'Unrelated member deleted a repayment';
 exception when raise_exception then if sqlerrm <> 'expenses.payment_permission_denied' then raise; end if; end;
 begin
  perform public.save_account_settlement(current_setting('loom.repayments.home')::uuid,
   current_setting('loom.repayments.home')::uuid, '00000000-0000-4000-8000-000000000661',
   '00000000-0000-4000-8000-000000000062', '00000000-0000-4000-8000-000000000063', 1500, current_date, 1);
  raise exception 'Unrelated member edited a repayment';
 exception when raise_exception then if sqlerrm <> 'expenses.payment_permission_denied' then raise; end if; end;
end; $$;

-- The recipient can correct a payment; a lost-response retry is idempotent.
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000063';
select public.save_account_settlement(current_setting('loom.repayments.home')::uuid,
 current_setting('loom.repayments.home')::uuid, '00000000-0000-4000-8000-000000000661',
 '00000000-0000-4000-8000-000000000062', '00000000-0000-4000-8000-000000000063', 1500, current_date - 1, 1);
select public.save_account_settlement(current_setting('loom.repayments.home')::uuid,
 current_setting('loom.repayments.home')::uuid, '00000000-0000-4000-8000-000000000661',
 '00000000-0000-4000-8000-000000000062', '00000000-0000-4000-8000-000000000063', 1500, current_date - 1, 1);
do $$ begin
 assert (select version from public.settlements where id = '00000000-0000-4000-8000-000000000661') = 2;
 assert (select sum(delta_minor) from public.account_ledger_entries where account_id = current_setting('loom.repayments.home')::uuid) = 0, 'Editing retains a zero-sum ledger';
 assert (select sum(delta_minor) from public.account_ledger_entries where account_id = current_setting('loom.repayments.home')::uuid and user_id = '00000000-0000-4000-8000-000000000062') = 1500, 'Only the corrected amount contributes';
 begin
  perform public.delete_account_settlement(current_setting('loom.repayments.home')::uuid,
   current_setting('loom.repayments.home')::uuid, '00000000-0000-4000-8000-000000000661', 1);
  raise exception 'Stale confirmation deleted a changed payment';
 exception when raise_exception then if sqlerrm <> 'expenses.payment_changed' then raise; end if; end;
 begin
  perform public.save_account_settlement(current_setting('loom.repayments.home')::uuid,
   current_setting('loom.repayments.home')::uuid, '00000000-0000-4000-8000-000000000661',
   '00000000-0000-4000-8000-000000000062', '00000000-0000-4000-8000-000000000065', 1500, current_date, 2);
  raise exception 'Accepted an outside recipient';
 exception when raise_exception then if sqlerrm <> 'expenses.invalid_account_members' then raise; end if; end;
end; $$;

-- Household owner can remove a payment they did not create or receive.
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000061';
select public.delete_account_settlement(current_setting('loom.repayments.home')::uuid,
 current_setting('loom.repayments.home')::uuid, '00000000-0000-4000-8000-000000000661', 2);
select public.delete_account_settlement(current_setting('loom.repayments.home')::uuid,
 current_setting('loom.repayments.home')::uuid, '00000000-0000-4000-8000-000000000661', 2);
do $$ begin
 assert not exists(select 1 from public.settlements where id = '00000000-0000-4000-8000-000000000661'), 'Voided payments are hidden from realtime reads';
 assert not exists(select 1 from public.account_ledger_entries where account_id = current_setting('loom.repayments.home')::uuid), 'Deleted payment no longer changes balances';
 begin
  perform public.save_account_settlement(current_setting('loom.repayments.home')::uuid,
   current_setting('loom.repayments.home')::uuid, '00000000-0000-4000-8000-000000000661',
   '00000000-0000-4000-8000-000000000062', '00000000-0000-4000-8000-000000000063', 1500, current_date - 1, 2);
  raise exception 'A delayed edit resurrected a payment';
 exception when raise_exception then if sqlerrm <> 'expenses.payment_changed' then raise; end if; end;
end; $$;
reset role;
do $$ begin
 assert (select version from public.settlements where id = '00000000-0000-4000-8000-000000000661') = 3;
 assert not exists(select 1 from public.account_ledger_entries where account_id = current_setting('loom.repayments.home')::uuid), 'Service reads also exclude tombstones';
end; $$;
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000065';
do $$ begin
 begin
  perform public.delete_account_settlement(current_setting('loom.repayments.home')::uuid,
   current_setting('loom.repayments.home')::uuid, '00000000-0000-4000-8000-000000000661', 2);
  raise exception 'Outsider removed a payment';
 exception when raise_exception then if sqlerrm <> 'expenses.payment_permission_denied' then raise; end if; end;
end; $$;
rollback;
