#!/usr/bin/env python3
"""Run migrations and authorization/ledger integration tests in a disposable PostgreSQL container."""
from pathlib import Path
import subprocess
import time
import uuid

ROOT = Path(__file__).resolve().parents[1]
IMAGE = "postgres:17-alpine@sha256:d3244e8ce3f165ee3c93d9c4a4b1d0fe48b3a89e2287b717fdcbbde92d405065"
container = "loom-tests-" + uuid.uuid4().hex[:10]


def sql(value: str) -> None:
    result = subprocess.run(
        ["docker", "exec", "-i", container, "psql", "-U", "postgres", "-v", "ON_ERROR_STOP=1", "-q"],
        input=value, text=True, capture_output=True,
    )
    if result.returncode:
        raise RuntimeError(result.stdout + result.stderr)


try:
    subprocess.run(["docker", "run", "-d", "--name", container, "--tmpfs", "/var/lib/postgresql/data:rw,size=256m", "-e", "POSTGRES_PASSWORD=loom-test-only", IMAGE], check=True, capture_output=True)
    for attempt in range(60):
        if subprocess.run(["docker", "exec", container, "pg_isready", "-h", "127.0.0.1", "-U", "postgres"], capture_output=True).returncode == 0:
            break
        time.sleep(0.5)
    else:
        raise RuntimeError("PostgreSQL did not start")
    sql("""
        create role anon nologin;
        create role authenticated nologin;
        create role service_role nologin bypassrls;
        create schema storage;
        create table storage.buckets(id text primary key,name text,public boolean default false,file_size_limit bigint,allowed_mime_types text[]);
        create table storage.objects(id uuid primary key default gen_random_uuid(),bucket_id text references storage.buckets(id),name text,owner_id text,created_at timestamptz default now(),metadata jsonb,unique(bucket_id,name));
        alter table storage.objects enable row level security;
        grant usage on schema storage to anon,authenticated,service_role;
        grant select on storage.objects to anon;
        grant select,insert,update,delete on storage.objects to authenticated;
        grant all on storage.buckets,storage.objects to service_role;
        create schema auth;
        create table auth.users(id uuid primary key,email text);
        create function auth.uid() returns uuid language sql stable as $$select nullif(current_setting('request.jwt.claim.sub',true),'')::uuid$$;
        create function auth.jwt() returns jsonb language sql stable as $$select coalesce(nullif(current_setting('request.jwt.claims',true),''),'{}')::jsonb$$;
        grant usage on schema auth to anon,authenticated,service_role;
        grant execute on function auth.uid(),auth.jwt() to anon,authenticated,service_role;
    """)
    migrations = sorted((ROOT / "supabase/migrations").glob("*.sql"))
    for migration in migrations:
        name = migration.stem.split("_", 1)[1]
        before = ROOT / "supabase/tests/upgrades" / f"{name}.before.sql"
        after = ROOT / "supabase/tests/upgrades" / f"{name}.after.sql"
        if before.exists():
            sql(before.read_text())
        sql(migration.read_text())
        if after.exists():
            sql(after.read_text())
    for test in sorted((ROOT / "supabase/tests").glob("*.sql")):
        sql(test.read_text())
    print(f"PostgreSQL: {len(migrations)} migrations applied; household, ledger, recurrence, statement, and access tests passed.")
finally:
    subprocess.run(["docker", "rm", "-f", "-v", container], capture_output=True)
