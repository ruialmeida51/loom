import { isWorker, response, serviceClient } from "../_shared/backend.ts";
import { cleanupReceipts } from "../_shared/receipts.ts";
import { deliver, type Delivery } from "../_shared/push.ts";

Deno.serve(async (request) => {
  if (request.method !== "POST" || !await isWorker(request)) {
    return response({ error: "Unauthorized" }, 401);
  }
  const database = serviceClient();
  const receiptCleanup = cleanupReceipts(database).catch(() => {
    console.error("Receipt cleanup will retry");
    return 0;
  });
  const { data, error } = await database.rpc("claim_notification_deliveries", {
    p_limit: 50,
  });
  if (error) {
    await receiptCleanup;
    return response({ error: "Could not claim household work" }, 500);
  }
  const deliveries = (data ?? []) as Delivery[];
  let sent = 0;
  for (let offset = 0; offset < deliveries.length; offset += 5) {
    await Promise.all(
      deliveries.slice(offset, offset + 5).map(async (item) => {
        let result: "sent" | "retry" | "dead" = "retry";
        let error: string | null = null;
        try {
          result = await deliver(item);
        } catch {
          error = "Push provider unavailable or not configured";
        }
        const { error: saveError } = await database.rpc(
          "finish_notification_delivery",
          { p_id: item.delivery_id, p_result: result, p_error: error },
        );
        if (saveError) {
          console.error(
            "Could not record notification delivery result",
            item.delivery_id,
          );
        }
        if (result === "sent") sent++;
      }),
    );
  }
  return response({
    claimed: deliveries.length,
    sent,
    receiptsRemoved: await receiptCleanup,
  });
});
