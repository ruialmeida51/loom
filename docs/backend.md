# Supabase setup

Loom’s existing project is [the Loom dashboard](https://supabase.com/dashboard/project/eeyfhmlyopuobtgdfbql). App configuration uses its project URL and **publishable** API key. The repository includes these public client values in `config/loom-public.properties`. Override them in the gitignored `loom.properties`, or set `SUPABASE_URL` and `SUPABASE_PUBLISHABLE_KEY` in your build environment. Server keys are never included in client configuration.

The source contains schema, policies, transaction functions, worker queues, and Edge Functions. `loom_private` and `loom_legacy` must stay outside the Data API’s exposed schema list; only `public` is needed.

## CLI and migrations

Use Supabase CLI **3.1.0**, or inspect the help for another version before changing commands. CLI v3 targets local databases by default and requires an explicit hosted target for hosted mutations.

```sh
supabase start
supabase db push
```

For a hosted deployment, link the intended project and use `supabase db push --remote` after reviewing the migration list. The first migration retires the old prototype. Its table names are specific to the unused Loom prototype; review it when adopting this repository for another existing database.

## Functions and scheduled work

Deploy `refresh-feed` and `household-worker`. Both implement their own authentication; their platform JWT gateway setting is disabled so new API-key formats and server scheduling work consistently. The feed function validates the user’s token and membership. Worker calls require a high-entropy secret verified with a server-only database RPC.

Apply `supabase/schedules.sql` after setting the SQL session variable `loom.project_url` to the project’s HTTPS API URL. It creates/retains a random secret in Vault, registers its hash, stores the two function URLs in Vault, and schedules household work every minute and feed refresh every 30 minutes. No worker secret is committed or returned.

The worker creates due recurring expenses, saves closed-month statements, and processes push deliveries. Duplicate runs preserve expense/notification identity. The feed worker processes the oldest subscriptions first, with bounded batches and fetch timeouts.

## Android push

1. Register the Android application **`com.eyther.lumbridge`** in your Firebase project.
2. Place its `google-services.json` in `androidApp/`. The file is gitignored; the Google Services plugin is enabled when it exists.
3. Store `FCM_PROJECT_ID`, `FCM_CLIENT_EMAIL`, and `FCM_PRIVATE_KEY` as Supabase Edge Function secrets.
4. Enable notifications in Loom on a physical device. Android 13+ asks for notification permission.

Firebase is used for Android push transport. Household data, authorization, and scheduling stay in Supabase.

## iOS push

1. Set your Apple signing team in Xcode and enable Push Notifications for **`com.eyther.lumbridge`**.
2. Store `APNS_TEAM_ID`, `APNS_KEY_ID`, and `APNS_PRIVATE_KEY` as Edge Function secrets.
3. Set `APNS_BUNDLE_ID=com.eyther.lumbridge` and choose `APNS_ENVIRONMENT=sandbox` for development-signed builds or `production` for distribution builds.
4. Enable notifications in Loom on a physical device. Its AppDelegate passes the APNs token into the shared registration flow.

Provider credentials are entered through Supabase secrets settings or a gitignored secrets file; do not send them in chat or commit them. See `supabase/functions/.env.example` for the complete variable list.

## RSS publishers

The default allowlist includes BBC, The Guardian, Hacker News, Ars Technica, FeedBurner, and NASA feed hosts. Add another trusted publisher by setting `RSS_ALLOWED_HOSTS` to the complete comma-separated list. Redirect destinations also need to be listed. Article HTML is reduced to plain text before storage and display.

## Operations

- Monitor `cron.job_run_details`, Edge Function logs, and notification delivery state for failures.
- Authenticated database and HTTP operations require a connection; the dedicated preview holds its sample households in memory.
- Supabase Auth’s leaked-password protection is currently a project setting to enable in the [password security configuration](https://supabase.com/docs/guides/auth/password-security#password-strength-and-leaked-password-protection). The app requires at least 12 characters at registration.
- Security advisor INFO messages about archived prototype tables do not describe the active app schema. New unused-index notices are expected before the app has traffic.

## Shopping and recurring schedules

`shopping_lists_and_recurring_controls` creates per-household lists, backfills existing items into Groceries, and creates a default list for new households. Shopping writes use membership-checked RPCs and server-owned versions so stale edits/undo cannot overwrite an intervening change. Batch insertion is transactional and retry-safe. Archived lists keep their items; at least one list must remain active.

Recurring schedules are readable by household members and manageable by their payer, creator, or the household owner. Pause/cancel stop future entries, resuming schedules the next future monthly occurrence, and cancellation retains expense history. Recorded-payment dates use the household timezone and cannot be in the future. Schedule changes increment a version only when values change, preserving compatibility with the minute worker.
