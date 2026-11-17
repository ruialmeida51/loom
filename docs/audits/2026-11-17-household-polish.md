# Household polish audit — 17 November 2026

Final source review against `origin/main` at `f8cfbb6d4d294601d0b3ed41670dc6f39334e54a`,
using the Plannot-derived `ai/agents/AGENTS_REVIEWER.agent.md` checklist after fixes and validation.
The feedback/decision/source ledger is [here](../plans/2026-11-17-polish-ledger.md).

**Verdict: pass for source and local validation.** No outstanding blocking findings in this change.
Android device execution and iOS compilation remain release gates in CI. A build is not an upload receipt.

## Resolved findings

| Area | Finding and final behavior |
| --- | --- |
| Authentication storage | A missing absolute expiry could use the SDK's relative-to-now default. Writes encode defaults; legacy payloads missing expiry are expired for SDK refresh. Expired roundtrip, missing expiry and malformed payload tests pass. |
| Resume reads | SDK refresh failure exposes no current SDK session while domain identity is retained. Scoped live reads await SDK initialization/recovery and sufficient token lifetime; PostgREST/Functions require a session. Concurrent reads do not force independent token rotations. Genuine permission failures remain failures. |
| Live stream recovery | Initial/later transport errors formerly completed the stream permanently. Read streams opt into bounded-backoff retries for connection/server availability failures only. Failure values and cancellation are preserved; authorization and writes never enter this retry policy. Realtime channels are removed during cleanup. |
| Loading | The user's short initial/manual skeleton transition is restored in presentation. Resume/realtime updates have no floor. Refresh covers the existing composition instead of disposing focused inputs or changing list geometry. Home resolves sections independently; initial placeholders match row/carousel grouping. |
| Money presentation | Expenses show one prominent total and payer. Current position shows personal status, proposed transfers and member balances. Repayment formatting/relevance is prepared in expense UI models rather than computed in the screen. Personal zero balance does not imply everyone else is settled. |
| Statement navigation | Monthly navigation is removed. Saved legacy routes and push links redirect to current balances. The existing insights compatibility code and historical database records are retained deliberately; no financial history is rewritten. |
| Account picker | Immediate selection closes the picker, current selection is marked, and Cancel is removed. New account and Manage accounts remain meaningful actions. |
| Home | The news viewport reaches both screen edges; headers/items retain content gutters. Empty reminders/shopping use illustrated cards without duplicate add actions. Errors do not become empty success states. |
| Feed management | Subscriptions are independent of articles. Reading and management have separate Screen/contract/ViewModel/routes. Pending, failed and empty sources remain visible. Add/edit/refresh/remove support progress, typed errors and feedback. Returning to reading reloads quietly. |
| Feed refresh/edit | Manual News refresh fetches publishers with at most three concurrent requests; recent-source throttles do not block other sources. A transactional address replacement prevents stale editors and an old worker from attaching the previous publisher's articles. |
| Publisher support | Expresso is explicitly supported, including older configured allowlists. Other configured restrictions, redirect validation, HTTPS/userinfo/port restrictions, byte limits and XML protections remain intact. The actual public feed parsed into 10 HTTPS-linked articles locally. |
| Settings/legal/buttons | Plannot-style grouped navigation rows, icon cues, proper action buttons and a settings privacy introduction replace scattered text actions. Auth keeps compact legal links. The static audit now rejects TextButton regressions. Analytics wording makes no anonymity guarantee. |

## Architecture and style

- New subscription models, ports, DTOs, mappers, UseCases, state and components live in `features/feed`.
  Repayment UI models/components live in `features/expenses`. Household context remains in household.
- Core additions are generic session/transport infrastructure and reusable empty/refresh presentation.
  No household/financial models or feature imports were added to core.
- Hosts own navigation and sheet controllers. Separate feed destinations do not switch between hidden
  screen modes. Common form editing variants share pure content; production never branches on demo mode.
- All new DI registrations are in `shared/di`, using annotated providers and ViewModel contracts.
  Named Gradle project references and the acyclic feature graph remain intact.
- Raw SDK/HTTP/SQL/error text is classified in data. Above it, failures remain typed `AppError` values
  and presentation maps them to resources. Transport models have separate files and wire annotations.
- UI lists and derived repayment/article rows are prepared outside composition. Feed date preparation
  takes its timezone as state input; reducers do not read the device timezone. Fields/drafts stay private
  to ViewModels and mirror through `observeWith`.
- Existing financial write transactions, ownership/version checks, integer money and scope rules are
  unchanged. No automatic financial write retries, RLS relaxation or offline queue was introduced.

## Verification

- Full local run: **165 JVM tests, zero failures/skips**, including shared Compose UI flows. Subsequent
  focused captures exercise the same source-management and news-navigation assertions.
- Android debug app and instrumentation APK assemble successfully. Native device tests cover keyboard
  entry/alignment, refresh draft retention, lifecycle/Back and the added nested feed-management Back path.
  Building the test APK does not claim those device tests ran locally.
- Disposable PostgreSQL: **16 migrations** and integration tests pass. New cases verify stale edit refusal,
  household isolation, atomic article removal, anonymous denial and rejection of late old-worker inserts.
- Deno checks pass; **6 RSS parsing/security/configuration tests** pass. The actual Expresso URL returned
  12,204 bytes and 10 articles through `fetchFeed`/`parseFeed`.
- Tooling: **7 Python + 28 Node tests**, legal-site build/check and release-note size validation pass.
- Static architecture/resource audit passes: feature ownership, package paths, domain/core boundaries,
  DI placement, named project references, typed errors, stream naming, separate wire models and resources.
- Shared Compose captures cover Home/empty Home/news, money/current balances, profile/privacy and source
  management; news also has light/dark captures. These are sample data, not hosted household records.

## Backend deployment

Applied `20261117045258_feed_subscription_management` to project `eeyfhmlyopuobtgdfbql`.
Permission metadata confirms authenticated execution, no anonymous execution on public/private
functions, RLS enabled and subscription realtime publication enabled. `refresh-feed` **v3 is ACTIVE**.
Its existing manual user/worker authentication and gateway configuration are retained.

No hosted personal or financial records were queried or mutated for verification. New edit/delete
behavior runs only when a member invokes it; deploying its definition did not remove subscriptions.

## Verification limits and unrelated existing configuration

The reported device's original intermittent resume event has no captured network trace. Mocked SDK
states and transport tests establish and cover concrete failure paths; they do not prove that every
possible cause of that screenshot is eliminated. Native CI uses demo ports; actual Supabase token
expiry on a customer's device remains a follow-up check after installation.

Security advisors are unchanged: leaked-password protection is disabled in existing Auth configuration.
No Auth-policy/billing change was made. [Supabase's configuration guidance](https://supabase.com/docs/guides/auth/password-security#password-strength-and-leaked-password-protection).

Offline synchronization remains deferred. No new product decisions are pending. Distribution is gated
on CI and recorded by the Firebase workflow/deployment and archived changelog.
