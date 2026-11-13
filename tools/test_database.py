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
    subprocess.run(["docker", "run", "-d", "--name", container, "-e", "POSTGRES_PASSWORD=loom-test-only", IMAGE], check=True, capture_output=True)
    for attempt in range(60):
        if subprocess.run(["docker", "exec", container, "pg_isready", "-U", "postgres"], capture_output=True).returncode == 0:
            break
        time.sleep(0.5)
    else:
        raise RuntimeError("PostgreSQL did not start")
    sql("""
        create role anon nologin;
        create role authenticated nologin;
        create role service_role nologin bypassrls;
        create schema auth;
        create table auth.users(id uuid primary key,email text);
        create function auth.uid() returns uuid language sql stable as $$select nullif(current_setting('request.jwt.claim.sub',true),'')::uuid$$;
        create function auth.jwt() returns jsonb language sql stable as $$select coalesce(nullif(current_setting('request.jwt.claims',true),''),'{}')::jsonb$$;
        grant usage on schema auth to anon,authenticated,service_role;
        grant execute on function auth.uid(),auth.jwt() to anon,authenticated,service_role;
    """)
    migrations = sorted((ROOT / "supabase/migrations").glob("*.sql"))
    for migration in migrations:
        sql(migration.read_text())
    sql((ROOT / "supabase/tests/household_security.sql").read_text())
    print(f"PostgreSQL: {len(migrations)} migrations applied; household, ledger, recurrence, statement, and access tests passed.")
finally:
    subprocess.run(["docker", "rm", "-f", container], capture_output=True)
