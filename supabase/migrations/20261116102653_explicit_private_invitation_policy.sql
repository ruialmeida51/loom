-- Make the private table's existing default-deny behavior explicit, matching other worker tables.
create policy server_only_invitation_attempts on loom_private.invitation_attempts
    to authenticated using (false) with check (false);
