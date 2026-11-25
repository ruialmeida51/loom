import { createClient } from "@supabase/supabase-js";
import { cleanupPictures, cleanupReceipts } from "./receipts.ts";

function fixture(paths: string[], failRemoval = false) {
  const calls: { path: string; body: Record<string, unknown> }[] = [];
  const database = createClient(
    "https://receipt-fixture.example.test",
    "test-service-key",
    {
      auth: { persistSession: false, autoRefreshToken: false },
      global: {
        fetch: async (input, options) => {
          const path = new URL(String(input)).pathname;
          const body = JSON.parse(String(options?.body ?? "{}"));
          calls.push({ path, body });
          if (
            path.endsWith("claim_receipt_deletions") ||
            path.endsWith("claim_picture_deletions")
          ) {
            return Response.json(
              paths,
            );
          }
          if (
            path.endsWith("finish_receipt_deletions") ||
            path.endsWith("finish_picture_deletions")
          ) {
            return new Response(
              null,
              { status: 204 },
            );
          }
          if (
            path === "/storage/v1/object/expense-receipts" ||
            path === "/storage/v1/object/pictures"
          ) {
            return failRemoval
              ? Response.json({ message: "Unavailable", statusCode: "503" }, {
                status: 503,
              })
              : Response.json([]);
          }
          throw new Error("Unexpected receipt request");
        },
      },
    },
  );
  return { database, calls };
}

Deno.test("receipt cleanup deletes actual objects before acknowledging the queue", async () => {
  const { database, calls } = fixture(["home/account/expense/photo.jpg"]);
  if (await cleanupReceipts(database) !== 1) {
    throw new Error("One receipt should be removed");
  }
  if (
    calls[0].body.p_limit !== 20 ||
    calls[1].path !== "/storage/v1/object/expense-receipts" ||
    calls[2].body.p_success !== true
  ) throw new Error("Claim, Storage delete and acknowledgment must be ordered");
});
Deno.test("failed Storage cleanup keeps the queue retryable", async () => {
  const { database, calls } = fixture(["home/account/expense/photo.jpg"], true);
  if (
    await cleanupReceipts(database) !== 0 ||
    calls.at(-1)?.body.p_success !== false
  ) throw new Error("Failure must be retried");
});
Deno.test("empty receipt cleanup never calls Storage", async () => {
  const { database, calls } = fixture([]);
  if (await cleanupReceipts(database) !== 0 || calls.length !== 1) {
    throw new Error("Empty cleanup must be cheap");
  }
});
Deno.test("picture cleanup removes replaced pictures from their own bucket", async () => {
  const { database, calls } = fixture(["profiles/user/old.jpg"]);
  if (await cleanupPictures(database) !== 1) {
    throw new Error("One picture should be removed");
  }
  if (
    !calls[0].path.endsWith("claim_picture_deletions") ||
    calls[1].path !== "/storage/v1/object/pictures" ||
    !calls[2].path.endsWith("finish_picture_deletions") ||
    calls[2].body.p_success !== true
  ) throw new Error("Pictures use their own queue and bucket");
});
