"""Exercise competing authenticated owner exits against the same real household row lock."""
from concurrent.futures import ThreadPoolExecutor
import subprocess
import threading
import time
import uuid


def run(container: str) -> None:
    command = ["docker", "exec", "-i", container, "psql", "-U", "postgres", "-v", "ON_ERROR_STOP=1", "-Atq"]

    def query(statement: str) -> str:
        result = subprocess.run(command, input=statement, capture_output=True, text=True, timeout=15)
        if result.returncode:
            raise RuntimeError(result.stderr)
        return result.stdout.strip()

    first, second = str(uuid.uuid4()), str(uuid.uuid4())
    household = query(f"""
        insert into auth.users(id,email) values('{first}','owner-a@example.test'),('{second}','owner-b@example.test');
        set request.jwt.claim.sub = '{first}';
        select public.create_household('Concurrent owners','EUR','UTC');
    """)
    query(f"insert into public.household_members(household_id,user_id,role) values('{household}','{second}','owner');")
    locker = subprocess.Popen(command, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    try:
        locker.stdin.write(f"begin; select 1 from public.households where id='{household}' for update; select 'locked';\n")
        locker.stdin.flush()
        while locker.stdout.readline().strip() != "locked":
            if locker.poll() is not None:
                raise RuntimeError("Household lock fixture exited before the race")
        barrier = threading.Barrier(3)

        def leave(actor: str, label: str):
            barrier.wait(timeout=10)
            return subprocess.run(command, input=f"""
                begin;
                set role authenticated;
                set request.jwt.claim.sub = '{actor}';
                select public.change_household_member('{household}','{actor}','leave',1,'{uuid.uuid4()}') /* loom-owner-exit-{label} */;
                commit;
            """, capture_output=True, text=True, timeout=15)

        with ThreadPoolExecutor(max_workers=2) as workers:
            attempts = [workers.submit(leave, first, "a"), workers.submit(leave, second, "b")]
            barrier.wait(timeout=10)
            deadline = time.monotonic() + 10
            while time.monotonic() < deadline:
                blocked = query("select count(*) from pg_stat_activity where query like '%/* loom-owner-exit-%' and wait_event_type='Lock';")
                if blocked == "2":
                    break
                time.sleep(0.03)
            else:
                raise AssertionError("Both owner exits must be waiting on the same household lock")
            locker.stdin.write("commit;\n\\q\n")
            locker.stdin.flush()
            locker.communicate(timeout=10)
            results = [attempt.result(timeout=15) for attempt in attempts]
        assert sum(result.returncode == 0 for result in results) == 1, "Exactly one competing exit should succeed"
        failure = next(result for result in results if result.returncode != 0)
        assert "household.last_owner" in failure.stderr, failure.stderr
        assert query(f"select count(*) from public.household_members where household_id='{household}' and active and role='owner';") == "1"
        winner = query(f"select user_id from public.household_members where household_id='{household}' and active and role='owner';")
        query(f"set role authenticated; set request.jwt.claim.sub='{winner}'; select public.delete_household('{household}','Concurrent owners','{uuid.uuid4()}');")
        print("PostgreSQL: competing owner exits serialized; exactly one active owner retained.")
    finally:
        if locker.poll() is None:
            try:
                locker.communicate("rollback;\n\\q\n", timeout=5)
            except (subprocess.TimeoutExpired, ValueError):
                locker.kill()
                locker.wait(timeout=5)
