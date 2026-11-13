-- Set loom.project_url='https://<project-ref>.supabase.co' in this SQL session first.
-- Worker credentials stay in Vault; only their hash is stored for verification.
do $$declare
    v_url text:=rtrim(current_setting('loom.project_url',true),'/');
    v_secret text;
    v_existing text;
begin
    if v_url is null or v_url !~ '^https://[a-z0-9]{20}\.supabase\.co$' then
        raise exception 'Set loom.project_url to the intended Supabase HTTPS API URL first.';
    end if;
    select decrypted_secret into v_secret from vault.decrypted_secrets where name='loom_worker_secret';
    if v_secret is null then
        v_secret:=encode(extensions.gen_random_bytes(32),'hex');
        perform vault.create_secret(v_secret,'loom_worker_secret','Loom scheduled worker credential');
    end if;
    if length(v_secret)<32 then raise exception 'Worker secret must contain at least 32 characters.';end if;
    insert into loom_private.worker_auth(singleton,secret_hash) values(true,extensions.digest(v_secret,'sha256'))
    on conflict(singleton) do update set secret_hash=excluded.secret_hash;
    select decrypted_secret into v_existing from vault.decrypted_secrets where name='loom_worker_url';
    if v_existing is null then
        perform vault.create_secret(v_url||'/functions/v1/household-worker','loom_worker_url','Loom household worker URL');
    elsif v_existing<>v_url||'/functions/v1/household-worker' then raise exception 'Existing worker URL points to another project.';
    end if;
    select decrypted_secret into v_existing from vault.decrypted_secrets where name='loom_feed_url';
    if v_existing is null then
        perform vault.create_secret(v_url||'/functions/v1/refresh-feed','loom_feed_url','Loom RSS refresh URL');
    elsif v_existing<>v_url||'/functions/v1/refresh-feed' then raise exception 'Existing feed URL points to another project.';
    end if;
    perform cron.unschedule(jobid) from cron.job where jobname in('loom-household-minute','loom-feed-refresh');
end;$$;
select cron.schedule('loom-household-minute','* * * * *',$job$
  select net.http_post(url:=(select decrypted_secret from vault.decrypted_secrets where name='loom_worker_url'),
    headers:=jsonb_build_object('Content-Type','application/json','X-Loom-Worker-Secret',(select decrypted_secret from vault.decrypted_secrets where name='loom_worker_secret')),
    body:='{}'::jsonb,timeout_milliseconds:=120000);
$job$);
select cron.schedule('loom-feed-refresh','*/30 * * * *',$job$
  select net.http_post(url:=(select decrypted_secret from vault.decrypted_secrets where name='loom_feed_url'),
    headers:=jsonb_build_object('Content-Type','application/json','X-Loom-Worker-Secret',(select decrypted_secret from vault.decrypted_secrets where name='loom_worker_secret')),
    body:='{}'::jsonb,timeout_milliseconds:=120000);
$job$);
