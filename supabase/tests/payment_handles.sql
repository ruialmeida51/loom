begin;
insert into auth.users(id, email) values
    ('00000000-0000-4000-8000-000000000f01', 'pay-owner@example.test'),
    ('00000000-0000-4000-8000-000000000f02', 'pay-alex@example.test'),
    ('00000000-0000-4000-8000-000000000f03', 'pay-outsider@example.test');
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000f01';
select set_config('loom.pay.home', public.create_household('Pay home', 'EUR', 'UTC')::text, true);
select set_config('loom.pay.invite', public.create_household_invite(current_setting('loom.pay.home')::uuid), true);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000f02';
select public.redeem_household_invitation(current_setting('loom.pay.invite'));
insert into public.payment_handles(user_id, mbway_phone, revolut_username, paypal_username)
values ('00000000-0000-4000-8000-000000000f02', '+351912345678', 'alex.s', 'AlexS');

do $$
begin
    begin
        insert into public.payment_handles(user_id, mbway_phone)
        values ('00000000-0000-4000-8000-000000000f01', '+351900000000');
        raise exception 'Someone saved another person''s payment details';
    exception when insufficient_privilege then null; end;
    begin
        update public.payment_handles set mbway_phone = '912345678'
        where user_id = '00000000-0000-4000-8000-000000000f02';
        raise exception 'A phone number without its country code was saved';
    exception when check_violation then null; end;
end;
$$;

set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000f01';
do $$
begin
    assert (select mbway_phone from public.payment_handles where user_id = '00000000-0000-4000-8000-000000000f02')
        = '+351912345678', 'Housemates see each other''s payment details';
    update public.payment_handles set mbway_phone = '+351900000000'
    where user_id = '00000000-0000-4000-8000-000000000f02';
    assert (select mbway_phone from public.payment_handles where user_id = '00000000-0000-4000-8000-000000000f02')
        = '+351912345678', 'Housemates cannot change someone else''s details';
end;
$$;

set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000f03';
do $$
begin
    assert (select count(*) from public.payment_handles) = 0, 'Outsiders see no payment details';
end;
$$;

-- Leaving the household stops sharing them.
reset role;
update public.household_members set active = false
where household_id = current_setting('loom.pay.home')::uuid and user_id = '00000000-0000-4000-8000-000000000f02';
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000f01';
do $$
begin
    assert (select count(*) from public.payment_handles) = 0, 'A former housemate''s details are no longer shared';
end;
$$;
rollback;
