import type { SupabaseClient } from "@supabase/supabase-js";

interface StorageQueue {
  bucket: string;
  claim: string;
  finish: string;
  label: string;
}

/** Delete through the Storage API so both object metadata and the actual file are removed. */
async function cleanupQueue(
  database: SupabaseClient,
  queue: StorageQueue,
): Promise<number> {
  const { data, error } = await database.rpc(queue.claim, { p_limit: 20 });
  if (error) {
    console.error(`Could not claim ${queue.label} cleanup`);
    return 0;
  }
  const paths = (data ?? []) as string[];
  if (!paths.length) return 0;
  const { error: removalError } = await database.storage.from(queue.bucket)
    .remove(paths);
  const { error: saveError } = await database.rpc(queue.finish, {
    p_paths: paths,
    p_success: !removalError,
  });
  if (removalError || saveError) {
    console.error(`${queue.label} cleanup will retry`);
  }
  return removalError ? 0 : paths.length;
}

export function cleanupReceipts(database: SupabaseClient): Promise<number> {
  return cleanupQueue(database, {
    bucket: "expense-receipts",
    claim: "claim_receipt_deletions",
    finish: "finish_receipt_deletions",
    label: "Receipt",
  });
}

/** Replaced and orphaned profile/household pictures: only the current one is ever kept. */
export function cleanupPictures(database: SupabaseClient): Promise<number> {
  return cleanupQueue(database, {
    bucket: "pictures",
    claim: "claim_picture_deletions",
    finish: "finish_picture_deletions",
    label: "Picture",
  });
}
