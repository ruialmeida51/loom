import { createClient } from "@supabase/supabase-js";

export function serviceClient() {
  const url = Deno.env.get("SUPABASE_URL");
  const keys = JSON.parse(Deno.env.get("SUPABASE_SECRET_KEYS") ?? "{}");
  const key = keys.default ?? Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!url || !key) throw new Error("Backend configuration is incomplete");
  return createClient(url, key, {
    auth: { persistSession: false, autoRefreshToken: false },
  });
}

export function response(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      "Content-Type": "application/json",
      "Cache-Control": "no-store",
    },
  });
}

export async function isWorker(request: Request): Promise<boolean> {
  const expected = Deno.env.get("LOOM_WORKER_SECRET");
  const received = request.headers.get("X-Loom-Worker-Secret");
  if (!received || received.length < 32 || received.length > 256) return false;
  if (!expected) {
    const { data, error } = await serviceClient().rpc(
      "verify_loom_worker_secret",
      { p_secret: received },
    );
    return !error && data === true;
  }
  if (expected.length < 32) return false;
  const encoder = new TextEncoder();
  const a = new Uint8Array(
    await crypto.subtle.digest("SHA-256", encoder.encode(expected)),
  );
  const b = new Uint8Array(
    await crypto.subtle.digest("SHA-256", encoder.encode(received)),
  );
  let difference = 0;
  for (let i = 0; i < a.length; i++) difference |= a[i] ^ b[i];
  return difference === 0;
}
