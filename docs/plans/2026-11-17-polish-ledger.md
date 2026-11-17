# Loom polish: feedback, evidence and delivery ledger

Updated 17 November 2026. Baseline: Firebase 4.0.4 (54), merged PR #13.
This is the active follow-up ledger; earlier audits and plans remain historical evidence.
Statuses distinguish triage, implementation and verified checks. The implementation checkpoint below records progress; the release workflow and changelog record delivery. A proposal is not a shipped fix.

## Decisions from the user

- Keep the short, approximately 300 ms skeleton transition for first loads **and explicit pull-to-refresh**, like before. Background/resume updates preserve the current screen quietly. This supersedes 4.0.4's no-minimum-duration rule.
- Show the **current selected account position only**: who owes whom, outstanding amounts and recorded settlements. Monthly statement navigation is unnecessary for this use case. Individual expenses remain available to explain balances.
- Rework expense information hierarchy and feed cards. Another palette change is not the goal.
- Home news must scroll to the screen edges; its header can retain the regular gutter.
- Empty reminders/shopping need a clear illustrated empty state. Do not repeat add buttons: Home already has quick actions. Empty, loading and error states must remain distinct.
- The expense account selection sheet needs no Cancel button. Selection applies immediately and dismisses it.
- Prefer proper buttons and Plannot/feed-selector style grouped navigation rows throughout profile/settings. The user dislikes text buttons. Privacy and terms need a discoverable settings entry and brief introduction.
- Feed management must be obvious. A followed feed must remain discoverable even if its first refresh fails.
- Preserve every input, research source and unfinished task here. Do not mark research, speculation or a mockup as implemented.

## Active work

| ID | Problem / desired result | Evidence and acceptance | Status |
| --- | --- | --- | --- |
| RESUME-01 | Home sometimes claims membership was lost after resume | User screenshot: Reminders and Shopping show “You no longer have access to this household.” News remains visible; that does not prove a fresh news request succeeded. Reproduce with mocked SDK refresh failure, verify no anonymous household reads, recovery without logout/wizard reset, and genuine membership denial remains denied. | Fixed known failure paths; live-device symptom replay remains |
| RESUME-02 | A temporary read failure can permanently end a live section stream | `SupabaseAccess.watch` performs its first read before installing polling/realtime; `asApiResult` emits failure then completes. Check recovery without indefinite authorization retries. Preserve cancellation and scope. | Verified transport recovery; authorization stays terminal |
| LOAD-01 | Restore smoother first-load/refresh timing | User explicitly selected “Short skeleton transition, like before.” Apply in presentation, once per intentional load; no delay on subsequent realtime/polling emissions. Preserve drafts, selection, scroll and composer identity. Test fast/slow responses, failure and cancellation. | Verified timing, independent sections and focus retention |
| HOME-01 | News carousel is inset at both edges | Home applies a horizontal gutter to the parent list. Make the carousel viewport full width and give its items content padding; verify narrow and large text layouts. | Verified viewport bounds and capture |
| HOME-02 | Empty daily sections need useful visual feedback | Illustrated “nothing for today” state; no duplicate actions. If only one section is empty, keep the populated section useful. Never show an empty-success illustration for a failure. | Verified empty/error separation and captures |
| MONEY-01 | Expense rows/cards are ugly and difficult to scan | Strong title/total hierarchy, payer metadata, recognizable icon/avatar, compact grouping; avoid several equally prominent amounts. Compare real references and verify the authorized implementation with light/dark captures. | Implemented; UI flows and light/dark captures pass |
| MONEY-02 | Statements are date-heavy and difficult to read | Replace primary monthly statement workflow with current account position. Reuse existing balance/settlement calculations; no parallel financial truth or deletion of financial records. Audit More/navigation/deep links/exports when implementing. | Implemented; old links retain a compatibility redirect |
| MONEY-03 | Account picker has redundant Cancel | Immediate-selection sheet, selected checkmark, New account and Manage accounts only where useful. Dismiss by selection, Back, scrim or drag. | Implemented; selector flows pass |
| SETTINGS-01 | Profile/settings rely on scattered text actions | Adopt Plannot `OptionCardGroups` style: icon, label, optional value and chevron; full-row targets. Filled primary, outlined/tonal secondary, destructive button for dangerous actions. Include profile/preferences spacing. | Implemented; static/button and UI checks pass |
| SETTINGS-02 | Privacy/terms are hard to discover | Privacy & legal entry with short accurate introduction; policy, terms, optional analytics and account deletion remain accessible. Do not invent encryption or privacy guarantees. | Implemented; privacy/settings captures pass |
| FEED-01 | No clear way to manage followed feeds | Dedicated Manage feeds entry; list subscriptions independently of articles, including pending/failed/empty feeds. Retry, edit and remove with visible status. Do not require an article to exist before a source is visible. | Verified ViewModel and add/edit/remove UI flows |
| FEED-02 | Expresso is saved, fails refresh, then cannot be found | Reproduction URL: `https://rss.impresa.pt/feed/latest/expresso.rss`. Public endpoint returned HTTP 200 RSS on 17 Nov. Server default publisher allowlist omits `rss.impresa.pt`; deployed overrides have not been inspected. Current app inserts before refresh, wraps every refresh failure as `FeedError.RefreshFailed`, and streams articles only. Validate transport/parser separately, distinguish unsupported publisher from transient failure, preserve SSRF protection and subscription visibility. | Backend v3 deployed; parser and policy checks pass |
| FEED-03 | News cards lack useful structure | Readable headline, source/date hierarchy, useful article affordance; image only if supported by actual feed data. Home remains a secondary preview, full News and Manage feeds stay reachable. | Implemented; light/dark news and management captures |
| AUDIT-01 | Final architecture, copy, usability and regression audit | Review final diff with `ai/agents/AGENTS_REVIEWER.agent.md` after fixes/checks. Typed errors, feature-owned packages, resources, stable collections/keys, central annotated DI, demo separation. Record limitations honestly. | Final local audit complete; CI/platform checks required before delivery |

## Research notebook

These are primary documentation/marketing references, not a claim that the native apps were tested.
Recommendations are Loom-specific inferences; references do not prove Loom's runtime behavior.

- [Tricount overview](https://www.tricount.com/): expense entry, group balances and repayment are the central loop. Official balance illustration uses people, an amount and compact totals. Borrow this hierarchy, not banking promotions or unrelated features.
- [Tricount FAQ](https://help.tricount.com/articles/tricount-faqs): personal/group totals are distinct; reimbursements are excluded from spending totals; settlement suggestions reduce transfers. Confirms that “current position” and “amount spent” must not be conflated.
- [Tricount management](https://help.tricount.com/articles/how-can-i-manage-my-tricounts-and-expenses): expense/reimbursement management reference; retain expense traceability when simplifying the summary.
- [Revolut Group Bills](https://help.revolut.com/help/transfers/group-bills/question-group-bills/) and [settling up](https://help.revolut.com/help/transfers/group-bills/group-bills-settling-up/): a group has bills and a settlement action; externally paid amounts can be recorded. Loom records settlements and does not transfer funds.
- [Monzo Trends](https://monzo.com/help/monzo-perks/trends-spending-and-balance-web): spending overview and running balance serve different questions. Useful comparison; the user chose current position rather than adding a monthly analytics workflow.
- [Material carousel guidance](https://github.com/material-components/material-components-android/blob/master/docs/components/Carousel.md): uncontained layouts scroll to the container edge. Apply the full-width viewport principle in Compose Multiplatform, not Android RecyclerView classes.
- [Apple buttons](https://developer.apple.com/design/human-interface-guidelines/buttons), [sheets](https://developer.apple.com/design/human-interface-guidelines/sheets), [settings](https://developer.apple.com/design/human-interface-guidelines/settings): action hierarchy and focused temporary surfaces. This does not imply every picker needs a Cancel action or every workflow should be a sheet.
- Plannot source: `shared/features/account/presentation/.../screen/MyAccountScreen.kt`, `PrivacyAndAbout`, `OptionCardGroups`. User expressly authorized copying patterns. Prefer adapting this component to building another settings style.
- Supabase Kotlin 3.9.0 source jars, `Auth.kt`, `AccessToken.kt`, `AuthImpl.kt`: automatic refresh is enabled in Loom. During SDK `RefreshFailure`, `currentSessionOrNull()` is null while Loom intentionally retains the last domain identity. Default authenticated APIs may fall back to unauthenticated requests unless session validity is required. This is a source-supported failure path; an actual device trace has not yet established it as the screenshot's sole cause.
- [Supabase refresh reference](https://supabase.com/docs/reference/kotlin/auth-refreshsession) and [changelog](https://supabase.com/changelog.md), checked 17 Nov: use exact installed SDK source for API names where the reference still uses older `gotrue` nomenclature.

## Existing constraints carried forward

Home, Shopping, Balances, More; global account outside household scope; multiple households and
everyday/trip accounts; household members only and one currency per account. Keep unequal splits,
recurring bills, settlement recording, completed-shopping bulk selection, Keep-style Enter composer,
reminder feedback and refresh across data destinations. Offline support remains deferred.

Keep KMP, `com.eyther.lumbridge`, feature-owned domain/data/presentation packages, `stream()` names,
typed domain errors, separate DTO/mappers/data sources, localized resource strings, central Koin
annotations, named Gradle references and isolated demo fixtures. Follow Plannot's coding style.
Authorization remains server-enforced; never weaken RLS to mask a resume error or retry financial
writes automatically. Do not query hosted personal/financial records for this review.

Existing completed work and its verification are in `docs/usability-redesign-audit.md`,
`docs/household-refinement-audit.md`, `docs/implementation-audit.md` and earlier plans. Release rules
remain version bump/changelog on every release, Firebase `internal-testers` by default, GitHub
release/tag for an explicitly requested Play release. The later instruction authorizes implementing this pass and preparing a tested Firebase build while the user sleeps. Play publication was not requested.

## Delivery record

- App distribution is pending platform CI; the final audit and implementation evidence are recorded below.
- The feed migration and worker v3 are deployed, with permission metadata verified. No hosted personal or financial records were queried, and RLS was not weakened.
- The matching Firebase workflow/deployment and archived changelog will record the uploaded app version.


## Implementation checkpoint

The user authorized the full pass and standard Firebase delivery after validation. No further product
questions are pending. The initial table records triage; this checkpoint supersedes its open design labels.

- Authenticated reads wait for a fresh SDK session and avoid anonymous fallback. Transient read streams
  recover; authorization failures and financial writes do not enter the transport retry loop.
- First loads and explicit refresh use one short timing floor. Refresh skeletons preserve the underlying
  composition, focus, drafts and scroll. Home sections resolve independently. Matching row/article
  placeholders reduce initial layout jumps.
- Expense cards emphasize one total and the payer. Current position shows a personal summary, suggested
  repayments and member balances. Monthly statement navigation is removed; old saved routes and push
  links redirect to current balances. Existing financial records remain intact.
- The account picker marks the current account and has no Cancel footer. Profile/legal groups use
  reusable navigation rows and proper buttons. Engineering rules and the static audit reject TextButton
  regressions. Account settings have a privacy introduction; auth keeps compact legal links.
- Reading and source management have separate Screens, contracts, ViewModels and routes. Failed,
  pending and empty sources remain visible. Add/edit/refresh/remove have typed errors and feedback.
  Editing replaces the source transactionally so an old worker cannot attach articles from its old URL.
  Returning from management quietly reloads articles.
- Explicit News refresh fetches publishers with at most three concurrent requests, respects per-source
  rate limits and reloads articles. No-articles and no-unread states have different copy.
- Expresso was fetched with the actual server parser: **10 articles**, 12,204 bytes, all article links HTTPS.
  The inspected deployed v2 defaults omit `rss.impresa.pt`; the new defaults include that exact host and
  retain redirect, SSRF and size protections. Existing configured restrictions are preserved and extended only by the explicitly approved Expresso host. No secret values were read.
- Disposable PostgreSQL: **16 migrations** and integration checks passed, including stale edit rejection,
  household isolation, cascading removal and rejection of a late old-worker insertion. Deno checks and
  **6 RSS tests** passed. Session/timing/recovery and feed ViewModel tests pass. Final UI/platform checks
  and backend deployment remain to be recorded in the audit.
- No hosted personal/financial records were queried. Existing security metadata reports leaked password
  protection disabled. This pass does not change Auth policy or billing; the existing configuration
  follow-up is documented at https://supabase.com/docs/guides/auth/password-security#password-strength-and-leaked-password-protection.

Further primary references: [Feedly source management](https://docs.feedly.com/article/58-how-can-i-edit-my-feeds-sources),
[unfollowing](https://docs.feedly.com/article/77-how-can-i-remove-a-feed-or-sources) and
[Inoreader feed organization](https://www.inoreader.com/blog/2020/10/organize-and-customize-feeds.html).
They support treating subscriptions as entities independent of article availability. Loom adopts this
separation without adding the folders/rules/workspace complexity of a dedicated reader.


## Verified backend and code review

- Applied `20261117045258_feed_subscription_management` to the Loom Supabase project. Authenticated
  execution is granted, both anonymous function grants are false, subscription RLS remains enabled,
  and subscription realtime publication is present. Only schema/permission metadata was read.
- `refresh-feed` **v3 is ACTIVE**, retaining its existing manual user/worker authentication and
  `verify_jwt=false` configuration. The bundle includes the required publisher addition even with an
  older configured allowlist; every other configured restriction remains in force.
- The final local run passed **165 JVM tests**, Android debug and instrumentation APK assembly,
  the architecture/resource audit, **7 Python + 28 Node tooling tests**, legal-site validation,
  disposable PostgreSQL integration and **6 RSS tests**. Device execution and iOS compilation run
  in CI; desktop captures are shared Compose renders, not device verification.
- Storage review additionally found the SDK expiry default is relative to now. Secure session writes
  now encode defaults; legacy payloads without an absolute expiry force SDK refresh. Three storage
  regressions cover expired roundtrip, legacy omission and malformed data.
- Visual evidence includes empty Home, edge-to-edge News, expenses, current balances, settings and
  source management. The source-management UI flow adds, edits and removes a subscription; a
  skeleton regression checks that a focused draft survives the overlay.

The final review procedure and validation limits are in `docs/audits/2026-11-17-household-polish.md`.
Release completion must be taken from the matching Firebase workflow/deployment and changelog, not
inferred from a successful build.

### Platform CI follow-up

The first CI run passed Android/shared/database/RSS checks, all four native Android checks and the
complete iOS/Xcode build. The parallel PR emulator run exposed two missing synchronization checks:
it did not wait for the split sheet to leave composition before backgrounding, and asserted the
news title immediately after an asynchronous native Back event. The lifecycle log also shows
`RESUMED` preceding input focus returning from ActivityScenario's helper window. Native checks now
wait for sheet removal, actual window focus and the returned destination, with one Back event and
the same route/draft assertions. The corrected checks must pass before delivery.
