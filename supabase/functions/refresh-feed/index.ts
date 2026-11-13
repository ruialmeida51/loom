import { isWorker, response, serviceClient } from "../_shared/backend.ts";
import { defaultHosts, fetchFeed, parseFeed } from "../_shared/rss.ts";

Deno.serve(async (request) => {
  if (request.method !== "POST") return response({ error: "Use POST" }, 405);
  const database = serviceClient();
  const worker = await isWorker(request);
  let subscriptionId: string | undefined;
  let userId: string | undefined;
  if (!worker) {
    const token = request.headers.get("Authorization")?.replace(
      /^Bearer /i,
      "",
    );
    if (!token) return response({ error: "Please sign in" }, 401);
    const { data, error } = await database.auth.getUser(token);
    if (error || !data.user) {
      return response({ error: "Please sign in again" }, 401);
    }
    userId = data.user.id;
    try {
      subscriptionId = (await request.json()).subscription_id;
    } catch {
      return response({ error: "Invalid request" }, 400);
    }
    if (!subscriptionId || !/^[0-9a-f-]{36}$/i.test(subscriptionId)) {
      return response({ error: "Choose a feed subscription" }, 400);
    }
  }
  let query = database.from("feed_subscriptions").select(
    "id,household_id,url,last_fetched_at",
  );
  if (subscriptionId) query = query.eq("id", subscriptionId);
  else {query = query.or(
      `last_fetched_at.is.null,last_fetched_at.lt.${
        new Date(Date.now() - 30 * 60_000).toISOString()
      }`,
    ).order("last_fetched_at", { ascending: true, nullsFirst: true }).limit(
      10,
    );}
  const { data: subscriptions, error } = await query;
  if (error) {
    return response({ error: "Could not load feed subscriptions" }, 500);
  }
  const hosts = (Deno.env.get("RSS_ALLOWED_HOSTS") ?? defaultHosts.join(","))
    .split(",").map((host) => host.trim().toLowerCase()).filter(Boolean);
  let refreshed = 0;
  const failures: string[] = [];
  for (const subscription of subscriptions ?? []) {
    if (!worker) {
      const { data: membership } = await database.from("household_members")
        .select("user_id").eq("household_id", subscription.household_id).eq(
          "user_id",
          userId!,
        ).eq("active", true).maybeSingle();
      if (!membership) {
        return response({ error: "Household access denied" }, 403);
      }
      if (
        subscription.last_fetched_at &&
        Date.now() - Date.parse(subscription.last_fetched_at) < 60_000
      ) {
        return response({
          error: "This feed was just refreshed. Please try again in a minute.",
        }, 429);
      }
    }
    try {
      const articles = parseFeed(
        await fetchFeed(subscription.url, hosts),
        subscription.url,
      ).map((article) => ({
        ...article,
        household_id: subscription.household_id,
        subscription_id: subscription.id,
      }));
      if (articles.length) {
        const { error } = await database.from("feed_articles").upsert(
          articles,
          { onConflict: "subscription_id,guid", ignoreDuplicates: true },
        );
        if (error) throw new Error("Could not save feed articles");
      }
      const { error } = await database.from("feed_subscriptions").update({
        last_fetched_at: new Date().toISOString(),
        last_error: null,
      }).eq("id", subscription.id);
      if (error) throw new Error("Could not save feed status");
      refreshed++;
    } catch (error) {
      const message = error instanceof Error
        ? error.message.slice(0, 160)
        : "Could not refresh this feed";
      await database.from("feed_subscriptions").update({
        last_fetched_at: new Date().toISOString(),
        last_error: message,
      }).eq("id", subscription.id);
      failures.push(message);
    }
  }
  return response(
    { refreshed, errors: failures },
    failures.length && !worker ? 422 : 200,
  );
});
