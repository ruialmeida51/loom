begin;
insert into auth.users(id,email) values
('00000000-0000-4000-8000-000000000301','refinement-owner@example.test'),
('00000000-0000-4000-8000-000000000302','refinement-member@example.test'),
('00000000-0000-4000-8000-000000000303','refinement-outsider@example.test');
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000301';
select set_config('loom.refine.home', public.create_household('Refinement home','EUR','UTC')::text,true);
select set_config('loom.refine.list', (select id::text from public.shopping_lists where household_id=current_setting('loom.refine.home')::uuid),true);
select set_config('loom.refine.old',public.create_short_household_invitation(current_setting('loom.refine.home')::uuid),true);
select set_config('loom.refine.code',public.create_short_household_invitation(current_setting('loom.refine.home')::uuid),true);
do $$ begin
    assert current_setting('loom.refine.code') ~ '^[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}$';
    begin perform public.join_household(current_setting('loom.refine.code'));raise exception 'Legacy endpoint accepted a short code';
    exception when raise_exception then if sqlerrm <> 'This invitation is invalid or has expired.' then raise;end if;end;
end;$$;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000302';
do $$ begin
    assert public.redeem_household_invitation(current_setting('loom.refine.old'))->>'status'='invalid','A replacement revokes the previous code';
    assert public.redeem_household_invitation(lower(current_setting('loom.refine.code')))->>'status'='joined','Case-insensitive short code works';
    assert public.redeem_household_invitation(current_setting('loom.refine.code'))->>'status'='invalid','Single use';
end;$$;
-- Any current member may delete completed groceries, including those added by another member.
select public.add_shopping_items(current_setting('loom.refine.home')::uuid,jsonb_build_array(
    jsonb_build_object('id','00000000-0000-4000-8000-000000000310','list_id',current_setting('loom.refine.list'),'name','Milk'),
    jsonb_build_object('id','00000000-0000-4000-8000-000000000311','list_id',current_setting('loom.refine.list'),'name','Bread'),
    jsonb_build_object('id','00000000-0000-4000-8000-000000000312','list_id',current_setting('loom.refine.list'),'name','Apples')));
update public.shopping_items set checked=true where id in ('00000000-0000-4000-8000-000000000310','00000000-0000-4000-8000-000000000311');
do $$ begin
    begin
        perform public.remove_completed_shopping_items(current_setting('loom.refine.home')::uuid,current_setting('loom.refine.list')::uuid,
            '{"00000000-0000-4000-8000-000000000310":2,"00000000-0000-4000-8000-000000000312":1}');
        raise exception 'Unchecked item deleted';
    exception when raise_exception then if sqlerrm <> 'shopping.message_this_item_changed_refresh_before_undoing_or_editing_it' then raise;end if;end;
    assert (select count(*) from public.shopping_items where removed)=0,'Invalid batch changes nothing';
    begin
        perform public.remove_completed_shopping_items(current_setting('loom.refine.home')::uuid,current_setting('loom.refine.list')::uuid,
            '{"00000000-0000-4000-8000-000000000310":1}');
        raise exception 'Stale version accepted';
    exception when raise_exception then if sqlerrm <> 'shopping.message_this_item_changed_refresh_before_undoing_or_editing_it' then raise;end if;end;
    begin
        perform public.remove_completed_shopping_items(current_setting('loom.refine.home')::uuid,current_setting('loom.refine.list')::uuid,'{}');
        raise exception 'Empty selection accepted';
    exception when raise_exception then if sqlerrm <> 'shopping.message_invalid_completed_selection' then raise;end if;end;
end;$$;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000301';
do $$ begin
    assert (select count(*) from public.remove_completed_shopping_items(current_setting('loom.refine.home')::uuid,current_setting('loom.refine.list')::uuid,
        '{"00000000-0000-4000-8000-000000000310":2,"00000000-0000-4000-8000-000000000311":2}'))=2;
    assert (select count(*) from public.shopping_items where removed and version=3)=2;
    assert (select count(*) from public.remove_completed_shopping_items(current_setting('loom.refine.home')::uuid,current_setting('loom.refine.list')::uuid,
        '{"00000000-0000-4000-8000-000000000310":2,"00000000-0000-4000-8000-000000000311":2}'))=2,'Lost-response retry is idempotent';
    assert (select count(*) from public.shopping_items where removed and version=3)=2,'Retry does not increment versions';
end;$$;
-- A restored item must survive an old bulk request.
update public.shopping_items set removed=false,checked=false where id='00000000-0000-4000-8000-000000000310';
do $$ begin
    begin
        perform public.remove_completed_shopping_items(current_setting('loom.refine.home')::uuid,current_setting('loom.refine.list')::uuid,
            '{"00000000-0000-4000-8000-000000000310":2,"00000000-0000-4000-8000-000000000311":2}');
        raise exception 'Restored item deleted';
    exception when raise_exception then if sqlerrm <> 'shopping.message_this_item_changed_refresh_before_undoing_or_editing_it' then raise;end if;end;
    assert (select not removed and not checked from public.shopping_items where id='00000000-0000-4000-8000-000000000310');
end;$$;
select set_config('loom.refine.revoked',public.create_short_household_invitation(current_setting('loom.refine.home')::uuid),true);
select public.revoke_household_invitation(current_setting('loom.refine.home')::uuid);
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000303';
do $$ begin
    assert public.redeem_household_invitation(current_setting('loom.refine.revoked'))->>'status'='invalid';
    begin perform public.revoke_household_invitation(current_setting('loom.refine.home')::uuid);raise exception 'Outsider revoked invitation';
    exception when raise_exception then if sqlerrm <> 'Only the household owner can invite members.' then raise;end if;end;
    begin perform public.remove_completed_shopping_items(current_setting('loom.refine.home')::uuid,current_setting('loom.refine.list')::uuid,
        '{"00000000-0000-4000-8000-000000000311":3}');raise exception 'Outsider deleted groceries';
    exception when raise_exception then if sqlerrm <> 'shopping.message_household_access_denied' then raise;end if;end;
    for i in 1..9 loop
        assert public.redeem_household_invitation('BADBAD')->>'status'='invalid';
    end loop;
    assert public.redeem_household_invitation('BADBAD')->>'status'='rate_limited','Failures persist within one window';
end;$$;
-- This separate statement verifies the failed attempts committed in the surrounding transaction.
reset role;
do $$ begin
    assert (select attempts from loom_private.invitation_attempts where user_id='00000000-0000-4000-8000-000000000303')=11;
    assert not has_table_privilege('authenticated','loom_private.invitation_attempts','SELECT');
    assert not has_function_privilege('anon','public.redeem_household_invitation(text)','EXECUTE');
    assert not has_function_privilege('anon','public.remove_completed_shopping_items(uuid,uuid,jsonb)','EXECUTE');
end;$$;
update loom_private.invitation_attempts set window_started_at=now()-interval '16 minutes'
where user_id='00000000-0000-4000-8000-000000000303';
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000303';
do $$ begin assert public.redeem_household_invitation('BADBAD')->>'status'='invalid','Window resets';end;$$;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000301';
select set_config('loom.refine.expired',public.create_short_household_invitation(current_setting('loom.refine.home')::uuid),true);
reset role;
update loom_private.household_invites set expires_at=now()-interval '1 second'
where token_hash=extensions.digest(current_setting('loom.refine.expired'),'sha256');
set role authenticated;
set request.jwt.claim.sub = '00000000-0000-4000-8000-000000000303';
do $$ begin assert public.redeem_household_invitation(current_setting('loom.refine.expired'))->>'status'='invalid';end;$$;
rollback;
