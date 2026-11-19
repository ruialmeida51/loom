-- Disposable PostgreSQL has no Supabase-managed Vault extension. This API double exercises
-- invitation lifecycle, access and cleanup SQL; hosted verification checks the real Vault extension.
create schema vault;
create table vault.secrets(id uuid primary key default gen_random_uuid(), secret bytea not null);
revoke all on schema vault from public,anon,authenticated,service_role;
revoke all on vault.secrets from public,anon,authenticated,service_role;
create function vault.create_secret(value text) returns uuid language plpgsql as $$
declare secret_id uuid;
begin
    insert into vault.secrets(secret) values(extensions.pgp_sym_encrypt(value, 'disposable-vault-test-key')) returning id into secret_id;
    return secret_id;
end;
$$;
create view vault.decrypted_secrets as select id,
    extensions.pgp_sym_decrypt(secret, 'disposable-vault-test-key') as decrypted_secret from vault.secrets;
revoke all on function vault.create_secret(text) from public,anon,authenticated,service_role;
revoke all on vault.decrypted_secrets from public,anon,authenticated,service_role;
