import type { SupabaseClient } from "@supabase/supabase-js";

/** Delete through the Storage API so both object metadata and the actual file are removed. */
export async function cleanupReceipts(
  database: SupabaseClient,
): Promise<number> {
  const { data, error } = await database.rpc("claim_receipt_deletions", {
    p_limit: 20,
  });
  if (error) {
    console.error("Could not claim receipt cleanup");
    return 0;
  }
  const paths = (data ?? []) as string[];
  if (!paths.length) return 0;
  const { error: removalError } = await database.storage.from(
    "expense-receipts",
  ).remove(paths);
  const { error: saveError } = await database.rpc("finish_receipt_deletions", {
    p_paths: paths,
    p_success: !removalError,
  });
  if (removalError || saveError) console.error("Receipt cleanup will retry");
  return removalError ? 0 : paths.length;
}
