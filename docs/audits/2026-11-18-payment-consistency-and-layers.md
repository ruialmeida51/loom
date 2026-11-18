# Payment consistency, feature boundaries and state audit

Baseline: main `6d1a800`, following PR 19 / Firebase 4.0.10 (60).
Reviewed the implementation against AGENTS.md, the Loom patterns skill, and all ten sections of
[the Plannot-derived reviewer checklist](../../ai/agents/AGENTS_REVIEWER.agent.md) after fixes and
shared/platform validation. [Accepted scope](../plans/2026-11-18-payment-consistency-and-layer-audit.md).

## Resulting flows

- Tapping either financial record opens its full-screen details. Both share the toolbar Edit/Delete
  component; a card's overflow still provides the same direct actions.
- Record payment and Edit payment use one full-screen form with a private, entry-scoped draft and
  a fixed full-width Save action. The focused date picker stays a sheet; Back dismisses the picker
  without discarding the form. A successful Save is terminal, preventing repeated submission.
- Payment details remain readable for members without edit permission. Archived accounts retain
  their balances without offering a payment write. Confirmations retain their original target/version
  and typed failure until retry or cancellation.
- Returning to details refreshes silently, including a return during an outstanding receipt read.
  Explicit pull-to-refresh and initial loads retain the requested 300 ms skeleton transition.

## Findings resolved

Severity below describes the original finding. Paths identify the owning fix; line numbers can move.

| Fix location | Category / original severity | Finding and final change |
| --- | --- | --- |
| `authentication/presentation/viewmodel/SessionScreenViewModel.kt:31` | Layers / blocking | LauncherHost performed restore/persistence/sign-out and discarded failures. Authentication's Session VM now coordinates the existing domain UseCases, and the Host only composes routes, graphs and platform links. |
| `expenses/presentation/viewmodel/PaymentFormScreenViewModel.kt:28` | Ownership / should-fix | The overview VM owned the payment draft and sheet lifecycle. Payment form and details now have dedicated Screens, contracts, VMs and typed Hosts; obsolete overview inputs/events and the payment sheet were removed. |
| `expenses/presentation/viewmodel/ExpenseDetailsScreenViewModel.kt:251` | State / blocking | Delete read a changing public expense target and closed on failure. It now freezes the confirmed version and keeps failure feedback inside the confirmation; payment deletion follows the same rule. |
| `expenses/presentation/viewmodel/ExpensesScreenViewModel.kt:175` | State / should-fix | Account deletion closed on failure. The target is retained and the confirmation receives its error. |
| `authentication/presentation/viewmodel/SessionScreenViewModel.kt:72` | Concurrency / should-fix | Household selection had no failure handling or protection against late device writes. Selection is immediate in memory, persistence is serialized and cancellable, and failure remains visible. |
| `household/domain/usecase/RestoreHouseholdUseCase.kt:20` | Cancellation / blocking | A late canceled membership read could clear another session's saved choice. Cancellation is checked before selection cleanup; a race test covers a late empty response after switching users. |
| `authentication/presentation/viewmodel/SessionScreenViewModel.kt:124` | Confirmation / should-fix | Sign-out confirmation closed before its request, with no progress/error path. It now stays open and disables competing actions while busy, preserving the session and typed error on failure. Cancel clears the error. |
| `expenses/presentation/viewmodel/ExpenseDetailsScreenViewModel.kt:218` | Lifecycle / should-fix | Screen startup refreshed details again and returns could flash or stay stale. VM startup owns initial loading; Hosts send a silent return event, queued when an earlier read is still active. |
| `expenses/presentation/viewmodel/ExpenseDetailsScreenViewModel.kt:178` | Concurrency / should-fix | An old receipt read could replace a newly saved photo/version. Mutation invalidates earlier reads; a late-result regression test verifies the new receipt stays visible. |
| `expenses/presentation/viewmodel/contracts/ExpensesState.kt:125` | Presentation / should-fix | Account management calculated permission decisions during composition. Prepared immutable account/permission collections now supply the sheet; repayment action availability is prepared too. |

The session coordinator subscribes for its VM lifetime: a confirmed sign-out while the UI is
backgrounded clears cached portal identity before subscribers return. Same-user refresh/reconnection
keeps the household and setup flow; an identity change cancels the previous restore/selection work.
Cold-start authentication remains an explicit restoring state, never a speculative signed-out screen.

## Feature and layer ownership

Payment/ledger models, data adapters, monetary policy and presentation remain under expenses.
Household context, membership and selection stay in household. Authentication presentation depends
explicitly on household for session coordination; the feature dependency graph remains acyclic.
Core gained no feature models, routes or business policies. Entrypoint owns typed routes, navigation
and sheet controllers. All annotated providers remain in shared/di and bind VM contracts; Gradle
uses named project references. Production code has no demo branch or sample graph imports.

The new settlement read filters household, account and record ID. It reuses the existing response
DTO and mapper; raw SDK/PostgREST failures stay in data and cross upward as typed AppError.
Current membership and version checks remain server-authoritative. No schema or hosted migration
change is required. The existing repayment integration tests verify authorization, stale deletion,
idempotent retries, hidden tombstones and the zero-sum ledger.

## Compose stability evidence

The actual Compose compiler reports show 38/38 restartable functions skippable in expenses,
15/15 in authentication and 31/31 in entrypoint. [Recorded counts](evidence/2026-11-18-payment-compose.json).
No compiler behavior flag was changed. Reports were enabled with a temporary Gradle init script:

```groovy
allprojects { project ->
    project.plugins.withId("org.jetbrains.kotlin.plugin.compose") {
        def compose = project.extensions.getByName("composeCompiler")
        compose.reportsDestination.set(project.layout.buildDirectory.dir("reports/compose-stability"))
        compose.metricsDestination.set(project.layout.buildDirectory.dir("reports/compose-metrics"))
    }
}
```

Checked immutable presentation collections, stable record keys, private form inputs, VM/store
lifetimes and pure state reducers. Payment details use an immutable display model; forms receive
individual values/callbacks. Standard collections/domain objects and LocalDate still have parameters
whose stability is not inferred; strong skipping handles those by identity. No blanket stability
annotations or standard-library wrappers were added. Skippability is not a device frame-time profile
and is not proof that every possible source of jank has been removed.

## Verification

- 244 JVM tests, including 54 entrypoint UI/navigation checks: passed.
- Android debug APK and Android instrumentation APK: compiled successfully.
- Added a native payment flow covering real IME clearance, date-sheet Back, background/resume,
  editing, refreshed details and return navigation. Execution is a PR CI gate alongside the
  existing native checks; iOS framework/Xcode validation also runs in CI.
- Disposable PostgreSQL: all 20 migrations and integration tests passed. MockEngine verifies the
  new payment query's exact household/account/record scope.
- 7 Python tooling tests, 32 Node release tests, legal-site generation/validation and project audit:
  passed. Release notes remain within Play's 500-character limit.
- Visually checked [light/dark, read-only and narrow payment screens](../screenshots/2026-11-18-payment-flows).

**Final review: pass for shared behavior, feature/layer ownership and platform compilation.**
Native execution and iOS results are recorded by the PR's required CI checks before delivery.
The first emulator run reached payment details after passing keyboard/date-Back/resume checks,
but its assertion ran during the intentional initial skeleton. The destination helper now waits
for that destination's skeleton to finish. Device evidence also prompted matching the payment
skeleton's label/value rows and inter-card spacing to the loaded details.
The earlier reported production resume crash still needs a captured device stack for attribution;
this audit does not claim a universal crash/jank fix. Offline behavior remains deferred.
