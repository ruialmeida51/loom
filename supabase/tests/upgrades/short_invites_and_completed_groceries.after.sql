begin;
set role authenticated;
set request.jwt.claim.sub='00000000-0000-4000-8000-000000000292';
do $$ begin
    assert public.redeem_household_invitation(repeat('a',64))->>'status'='joined','A valid legacy invitation survives migration';
end;$$;
rollback;
-- The old endpoint still supports a legacy client using its previously issued token.
begin;
set role authenticated;
set request.jwt.claim.sub='00000000-0000-4000-8000-000000000292';
select public.join_household(repeat('a',64));
do $$ begin assert exists(select 1 from public.household_members where user_id=auth.uid() and household_id='00000000-0000-4000-8000-000000000293');end;$$;
rollback;
