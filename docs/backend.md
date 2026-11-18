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

## Google sign-in

Google is Loom’s primary sign-in option. Supabase is still the account/session authority; the app opens the system browser and exchanges the returned authorization code using PKCE. No Google client secret or server key belongs in the app. The verifier and session use encrypted native storage, and a callback arriving before the application graph starts is queued until its authentication source is ready.

In [Supabase → Authentication → URL Configuration](https://supabase.com/dashboard/project/eeyfhmlyopuobtgdfbql/auth/url-configuration), allow the exact redirect `com.eyther.lumbridge://auth/callback`. Android registers that scheme/host/path and forwards initial/new intents. iOS registers the scheme and forwards SwiftUI `onOpenURL` events. The project’s Google provider was already enabled and its anonymous authorization request redirects to Google using `https://eeyfhmlyopuobtgdfbql.supabase.co/auth/v1/callback`; retain that URL in the Google web OAuth client’s authorized redirects.

The native callback allowlist was added in the dashboard on 2026-11-14. A real-device Google account handoff still needs a final live check; CI and SDK tests do not impersonate a Google account. Neither sign-in nor profile details opt a user into Firebase Analytics.

## Authentication email delivery

Password sign-in does not send an email. **Email me a sign-in code** explicitly requests a code for an existing account; it sets `createUser = false` so registration and its terms step cannot be bypassed. New accounts must verify their email; the app accepts a code and can resend the signup confirmation. An `email_not_confirmed` password sign-in failure opens that verification screen. Resending preserves the challenge purpose: existing-account sign-in requests another login code, while registration resends the signup confirmation.

Both email templates must include `{{ .Token }}`. In [Authentication → Email Templates](https://supabase.com/dashboard/project/eeyfhmlyopuobtgdfbql/auth/templates), paste [confirmation.html](../supabase/templates/confirmation.html) into **Confirm signup**, with subject **Verify your Loom email**, and [sign_in.html](../supabase/templates/sign_in.html) into **Magic Link**, with subject **Your Loom sign-in code**. The repository config enables the same templates and confirmations for local development. Codes are entered in the app, so these flows do not depend on an email link returning through a website or native deep link.

The 2026-11-14 investigation found `invalid_credentials` sign-ins and `user_repeated_signup` requests for already confirmed accounts. Supabase deliberately accepts repeated signup requests without sending a fresh confirmation or revealing that an account exists. Use password sign-in or the explicit sign-in-code action for an existing account.

Configure a production sender under [Authentication → SMTP Settings](https://supabase.com/dashboard/project/eeyfhmlyopuobtgdfbql/auth/smtp). Supabase’s built-in sender only delivers to organization team addresses and currently permits two messages per hour; it does not provide production delivery guarantees. Use a verified sending domain with an SMTP provider, and keep its credentials only in Supabase. Never put them in the app, GitHub client configuration, or chat. Follow the provider’s domain verification instructions and turn off link tracking for auth email.

Check [Auth logs](https://supabase.com/dashboard/project/eeyfhmlyopuobtgdfbql/logs/auth-logs) for `email_address_not_authorized`, rate-limit or delivery failures. A successful API request confirms acceptance, not inbox delivery; verify delivery with a real address and the provider’s delivery log after configuring SMTP. The app keeps these SDK codes in data, mapping them to typed domain failures and localized messages.

These hosted Auth settings are separate from SQL migrations. The connected Supabase tools currently expose database/log operations but not Auth configuration; no hosted template or SMTP change is implied by editing `config.toml`.

## Android push

1. Register the Android application **`com.eyther.lumbridge`** in your Firebase project.
2. Place its `google-services.json` in `androidApp/`. The file is gitignored; the Google Services plugin is enabled when it exists.
   CI reads the same JSON from the encrypted repository Actions secret `GOOGLE_SERVICES_JSON`.
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

The default allowlist includes BBC, The Guardian, Hacker News, Ars Technica, FeedBurner, NASA, and Expresso feed hosts. Add another trusted publisher by setting `RSS_ALLOWED_HOSTS` to the complete comma-separated list. Expresso is also included when an older configured list omits it; other configured restrictions are preserved. Redirect destinations also need to be listed. Article HTML is reduced to plain text before storage and display.

## Operations

- Monitor `cron.job_run_details`, Edge Function logs, and notification delivery state for failures.
- Authenticated database and HTTP operations require a connection; the dedicated preview holds its sample households in memory.
- Supabase Auth’s leaked-password protection is currently a project setting to enable in the [password security configuration](https://supabase.com/docs/guides/auth/password-security#password-strength-and-leaked-password-protection). The app requires at least 12 characters at registration.
- Security advisor INFO messages about archived prototype tables do not describe the active app schema. New unused-index notices are expected before the app has traffic.

## Shopping and recurring schedules

`shopping_lists_and_recurring_controls` creates per-household lists, backfills existing items into Groceries, and creates a default list for new households. Shopping writes use membership-checked RPCs and server-owned versions so stale edits/undo cannot overwrite an intervening change. Batch insertion is transactional and retry-safe. Archived lists keep their items; at least one list must remain active.

Recurring schedules are readable by household members and manageable by their payer, creator, or the household owner. Pause/cancel stop future entries, resuming schedules the next future monthly occurrence, and cancellation retains expense history. Recorded-payment dates use the household timezone and cannot be in the future. Schedule changes increment a version only when values change, preserving compatibility with the minute worker.

## Tasks and existing reminder clients

Apply the household-tasks migration before distributing a build with the Tasks tab. The migration
renames the existing table to `public.tasks`, retaining its row policies, scope trigger, grants and
realtime publication. Existing records keep notifications enabled. New tasks may omit a date, and
Loom explicitly disables notifications by default. A notification or repeat requires a date.

`public.reminders` remains a security-invoker compatibility view restricted to dated, notifying
tasks. Existing mobile clients and push queue workers continue to use that view; legacy realtime
subscriptions fall back to the existing polling path. `complete_reminder` delegates to the new
locked/expected-date `complete_task` RPC. Keep both compatibility endpoints until old builds are
retired. The local SQL suite tests old/new completion, membership isolation, daylight saving and
notification queue cancellation using disposable fixtures, never hosted household records.

## Shopping groups and receipts

`shopping_items.parent_id` supports one same-household/list nesting level. The group RPC freezes all
affected versions before applying parent/child changes; legacy single-item writes are reconciled by
triggers. Bulk completed removal returns the current list rows, including children promoted by parent
deletion, so clients adopt the trigger-generated versions immediately.

Receipt metadata is owned by expenses. `expense_receipts` keeps a versioned private object link (or
a tombstone after removal). `save_expense_receipt` accepts the original expected version and a
validated household/account/expense/photo UUID path. The `expense-receipts` bucket is private, accepts
JPEG only and limits each object to 5 MiB. Current members with expense access can read; the expense
creator, payer or household owner can manage where account access permits. No public URLs or service
credentials are included in the app. Uploads use unique immutable paths without upsert.

The existing authenticated `household-worker` also drains a server-only cleanup queue through the
Storage API. Replaced/deleted photos are queued immediately; unattached uploads are collected after
24 hours. Cleanup leases and link commits coordinate in short database transactions; the lock does
not cover network I/O. Failed deletions remain retryable. Preserve the existing worker authentication
and schedule when deploying this update. Storage metadata deletion alone does not remove a blob.
