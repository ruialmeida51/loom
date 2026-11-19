# Interaction polish and session recovery audit

Scope: [the execution checklist](../plans/2026-11-19-interaction-polish-and-receipts.md), based on
main after PR #21. This preserves the complete polish request and the subsequently reported
resume crash. Backend household-management migrations were already deployed with PR #21;
this change requires no new migration.

## Confirmed crash

The reported build is **4.0.10 (60)**, source `d673ef9e4f4d26fde90114e0fcea9df2f64c6223`.
Rebuilding that exact source/version recovered a mapping whose `pg_map_id` equals the reported
`e78f31c029286a960e4d23fa99dc1e53309083f7cd277f8bd17e3ccdfa0b5455`.
The initial 4.0.12 reconstruction was rejected because its ID differs.

The matching retrace identifies:

```text
CommonError.NotAuthenticated
  SupabaseAccess.userId
  CommonHouseholdDataSource.streamMembers
  GetMembersStreamUseCase.invoke
  ExpensesScreenViewModel member stream
```

Android's SDK lifecycle callback stops automatic refresh and publishes `Initializing` in the
background. The member stream synchronously looked up the current SDK user before returning its
flow. That lookup escaped `asApiResult` and crashed the view-model coroutine during resume.
Identity lookup now suspends through authenticated readiness and runs inside the cold, protected
member flow. Genuine sign-out is a typed stream failure; cancellation is preserved.

The [retrace](evidence/2026-11-19-resume-crash-retraced.txt) and
[verified identity](evidence/2026-11-19-resume-crash-identity.json) are retained as review evidence.
Reconstructed random Crashlytics IDs were never uploaded as if they belonged to the original APK.

## Final behavior and ownership

| Area | Change and responsibility |
| --- | --- |
| Shopping feedback | One shared custom snackbar. Shopping's VM owns the eight-second Undo timer, frozen target, revision/cancellation guards and navigation cleanup. Repeated removal can show feedback again. |
| Favourites | Shopping owns per-list favourite selection and duplicate policy; a labelled entry opens a compact, immediate-action sheet for browsing, reuse and un-favouriting. Presentation uses immutable display lists and stable item keys. |
| Receipt thumbnails | Expenses loads a private preview separately from an editable upload draft. Metadata/photo jobs have cancellation and revision guards; failed replacement retains the draft and existing preview. The thumbnail opens the viewer. |
| Receipt storage | Expenses data prepares metadata-stripped JPEGs with a 1 MiB upload limit, staged quality/dimension reduction and bounded native decode. Existing photos up to 5 MiB remain readable. A two-entry, household-scoped memory cache shares preview/viewer downloads without a second stored thumbnail. Supabase stores the submitted original bytes. |
| Expense capture | Date/category are always visible. Form inputs and split/payer/save behavior remain in Expenses' VM/use cases. |
| Balances | Compact transfer rows show direction, counterparty and red/green amounts. Record payment is a compact row action. The personal total is omitted when it merely repeats the single personal transfer, retained when it summarizes several transfers. Member totals are an optional disclosure. |
| Task dates | A normal optional Due date row replaces the oversized Add date action; notification time uses the same row pattern. Clear controls retain separate accessible touch targets. |
| Buttons | All labelled primary, secondary and destructive actions delegate to one Plannot-derived pill renderer with 48 dp minimum height and label-preserving progress. Brand identity, icon controls, selection chips, navigation rows and FAB semantics remain explicit. |
| Dialogs and sheets | Date/time and brief confirmation dialogs have equal-width actions. Form sheets keep vertically stacked full-width Save/Cancel actions. Immediate-selection sheets omit redundant footers. |
| Session recovery | Authentication owns explicit recovery and silent foreground repair through separate use cases. Resume repairs only an already-open session, preserving household/drafts. A stalled startup offers Try again after 15 seconds or a refresh failure. The SDK remains the only token-refresh owner; an active job is awaited, not raced. Genuine token revocation returns to login. |
| Symbols | Android explicitly uploads mappings and checks packaged R8/Crashlytics IDs plus planned version before distribution. Firebase and Play retain exact versioned symbols/packages. iOS uses documented Xcode inputs and checks UUID-matched device Release archives, synchronous upload and symbol retention. |

## Plannot-derived review

Reviewed all six groups in [the reviewer procedure](../../ai/agents/AGENTS_REVIEWER.agent.md):
shared components; authentication/account; Home/Shopping/Tasks; household interfaces; money and
receipts; navigation/data/DI/platform integration. Reference components were Plannot's
`DefaultButton`, primary/outline wrappers and `AppSnackbar`; Loom-specific conventions in
`AGENTS.md` and `ai/` take precedence.

- Domain policies and feature models remain in their owning feature. Core contains only generic
  controls, feedback and authenticated transport readiness. No feature aliases or repositories.
- Transport adapters retain SDK/raw-error handling. UI stores typed, resource-backed messages;
  it does not inspect exception strings. Request/response models retain separate files/mappers.
- All registrations remain in annotated `shared/di` modules. New recovery ports/use cases are
  registered centrally. Gradle dependencies use named project references, including native tests.
- Screen lifecycle effects dispatch VM events. Resume/session/financial/list policies are outside
  composition. Inner screens receive prepared values and callbacks. Production has no demo branch.
- Shared action geometry, destructive confirmations, loading feedback, immutable collections and
  stable record keys were checked. Receipt byte/draft ownership is separate from image decoding.
- Initial/explicit-refresh skeleton timing is retained; mutations use action progress. Background
  session repair does not add a skeleton or reset a destination. Offline data support remains deferred.
- An audit guard rejects raw feature Material buttons and stock snackbar hosts. Dialog and shared
  action rendering remain in their focused core packages.

## Validation and limits

The complete local gate passed **279 JVM tests**, including **60 shared UI/navigation checks**,
and built both Android debug application and test APKs. This includes the foreground-repair,
startup retry and confirmed crash regressions. The final identity-job cancellation fix passed the affected authentication/UI suites and both APK builds again. Python tooling has
**11 passing tests**, Node tooling **32**, and architecture, workflow syntax and whitespace checks pass.

Meaningful regressions cover bounded/repeated/stale Undo, favourite reuse, preview versus draft
state, failed receipt replacement, private authenticated download/cache eviction, legacy receipts,
new upload budget, payment direction/summary visibility, session-startup UI, concurrent recovery,
persisted rotated tokens, offline/background cancellation and genuine token revocation. The
exact formerly throwing member-stream construction is exercised while the SDK is initializing
and after a confirmed sign-out.

Android instrumentation adds actual ProcessLifecycleOwner Home/foreground, screen lock/unlock
and activity recreation with the production auth configuration and local test transport. It also
checks noisy native-photo compression. These checks are separate from the shared UI previews.
CI executes all 14 native checks, builds/checks a minified APK without distributing it, and builds the iOS device
Release archive in addition to the simulator configuration.

See [screenshots](../screenshots/2026-11-19-interaction-polish/README.md) for light/dark, narrow,
large-text, receipt, favourites, date-dialog and five-tab layout evidence. The final Compose metric
snapshot is attached under `evidence/`; compiler counts are diagnostic, not proof of frame-time behavior.

**Status:** local implementation/review passes, with native CI and release delivery pending. Results
will be appended before delivery. macOS/device checks and real crash-event delivery are not implied by local JVM
results. A consenting physical installation is still needed to verify delivery of a real crash in
Firebase, and APNs/physical iOS camera flows remain separate device checks.
