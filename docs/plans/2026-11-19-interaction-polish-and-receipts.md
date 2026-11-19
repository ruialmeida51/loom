# Interaction polish, receipt previews and session recovery

Base: main `743ce6a` after PR #21. This work is a separate change from the household-management
release. Keep all items below in scope through the final audit; do not replace unresolved items
with additional features.

## User requests and acceptance checks

| ID | Request | Acceptance |
| --- | --- | --- |
| FEEDBACK-01 | Deleting a shopping item leaves a snackbar visible indefinitely. | Custom Undo feedback expires, clears its pending operation and can appear again for a subsequent deletion. Undo still restores the intended item/group. Navigation, repeated actions and resume cannot leave stale feedback. |
| FEEDBACK-02 | Use custom snackbars consistently. | One shared visual component supports plain success and optional actions. Shopping no longer owns a stock Material snackbar host alongside custom success feedback. Timing/actions remain owned by feature presentation state. |
| SHOP-01 | Favourites have no visible destination. | Shopping exposes a labelled, accessible favourites entry point and a useful empty state. Saved favourites can be browsed and re-added without typing; current-list duplicates are handled clearly. Keep the checklist composer/keyboard geometry stable. |
| RECEIPT-01 | Show a photo thumbnail on expense details. | An existing receipt loads a bounded preview that opens the full viewer. Include matching loading/error/retry states; private receipt access stays behind Expenses' port. Retain a prepared draft during failed uploads. |
| RECEIPT-02 | Protect storage space by compressing before upload. | Verify the existing native compression pipeline, then tighten the upload budget while preserving receipt readability, orientation and metadata stripping. Bound decode memory and input/output bytes. Supabase stores the uploaded bytes; do not rely on server-side automatic compression or add a second stored thumbnail unnecessarily. |
| EXPENSE-01 | Category and date should always be visible when adding an expense. | Both fields appear in the normal form flow, without expanding More details. Preserve drafts, split/payer behavior, recurring editing and IME clearance. |
| MONEY-01 | Balances repeats the personal amount and the “X paid me” action sits awkwardly. | Give the personal net position and who-owes-whom rows distinct hierarchy. Keep settlement amounts on their relevant rows and use a clear, consistently placed Record payment action, with correct prefilled payer/recipient. Avoid repeating a payment sentence as a large action button. |
| TASK-01 | Replace the unusual Add date button. | Use an ordinary labelled optional date/schedule control consistent with the form. Show the selected date and an accessible clear action; maintain date/notification optionality and sensible spacing. |
| BUTTON-01 | Round and squircle action buttons are inconsistent. | Use one shared action-button implementation for primary, secondary and destructive variants, with a consistent theme shape, typography, spacing, progress and accessible touch height. Audit raw Material action-button calls in features/Hosts; preserve distinct navigation rows, selection chips, icon controls and FAB semantics. |
| DATE-01 | Date-dialog buttons are too small. | Calendar dialogs use two equally weighted actions across the available width. This is the user's explicit exception to vertically stacked full-width form-sheet footers. Labels may wrap and remain usable with larger text. |
| SESSION-01 | Background/foreground or screen locking crashes or loses the session with no recovery. | Identify the failing path using the exact R8 mapping where available; preserve credentials and the established identity through recoverable refresh failures, recover on resume, and allow recovery after interrupted startup. Genuine revocation/sign-out still clears protected state. Add lifecycle, concurrent refresh, offline/reconnect and cancellation regressions. |
| SYMBOL-01 | Crash reports remain obfuscated. | Explicitly verify Android Crashlytics mapping upload for minified releases and archive the exact mapping with version/source/map identity for Firebase and Play. Configure iOS dSYM generation/upload with documented Xcode inputs; validate the actual release build path. Never decode with a non-matching R8 map or claim unexecuted native checks passed. |
| AUDIT-01 | Audit the remaining buttons and overall polish. | Review light/dark, narrow widths, large text, real keyboard/system Back/resume and operation failures. Fix scoped button inconsistencies. Finish the Plannot-derived package/layer/resource/stability review and record evidence and any actual limits. |

## Findings verified before implementation

- ShoppingHost calls Material SnackbarHostState.showSnackbar with an Undo action and no explicit
  duration, while ShoppingScreen independently renders the custom SuccessSnackbar.
- Favourite flags are persisted and ranked in suggestion data, but the checklist has no favourites
  browsing action or visible destination.
- Receipt normalization already bounds the longest edge at 2560 pixels, writes JPEG at quality 85,
  and rejects uploads above 5 MiB. Details only render a preview when local photo bytes are already
  available, so an attached server receipt appears as a text/navigation row.
- LoomButton and LoomSecondaryButton explicitly use the theme's medium shape; DestructiveButton
  and feature-owned raw Material buttons can fall back to the default pill shape.

## Boundaries and validation

Keep favourite selection/reuse in Shopping, receipt preparation/download in Expenses data/use cases,
and money presentation in Expenses. Core owns only reusable feedback, buttons and generic layout.
Use typed errors, resource copy, immutable display collections, central annotated DI and named
project references. No production demo branches or business transformations in composition.

Add meaningful regressions for snackbar expiry/repeated Undo, favourites reuse, private receipt
preview/retry, bounded photo normalization and payment direction. Reuse existing form/navigation
coverage and add native checks where keyboard, image codecs or date dialogs need real behavior.
Do not broaden unrelated feature scope. Record final results in the audit and PR.

## Execution checklist

- [x] Timed custom shopping Undo and unified feedback.
- [x] Discoverable favourites with quick reuse.
- [x] Receipt detail thumbnail and bounded compression.
- [x] Visible expense category/date and clearer balance/payment hierarchy.
- [x] Familiar optional task date and equally weighted date-dialog actions.
- [x] Shared button implementation and raw-button guard; final scoped audit remains below.
- [x] Resolve resume/session crash and recovery; exact 4.0.10 retrace and meaningful regressions.
- [x] Implement Android mapping verification/retention and documented iOS dSYM release setup; native CI remains a delivery gate.
- [ ] Relevant tests/builds, screenshots and final architecture/stability audit.
- [ ] Changelog, reviewable PR and normal versioned Firebase delivery when authorized.

## Session report and symbol investigation

- Reported Android R8 map ID: `e78f31c029286a960e4d23fa99dc1e53309083f7cd277f8bd17e3ccdfa0b5455`. The supplied trace has no exception message beyond `xi0`; user confirmed 4.0.10; deployment history identifies build 60, source `d673ef9`.
- The local release mapping has a different ID and cannot safely decode this trace.
- Firebase 4.0.12 (62), source `743ce6a`, ran `uploadCrashlyticsMappingFileRelease`; the workflow currently archives mappings only for Play builds.
- iOS dSYM files are separate from this Android R8 trace. Existing Xcode release upload phase has no declared inputs and simulator CI never exercises it.
- Local polish validation before session work: 269 JVM tests (including 59 UI/navigation), Android app/test APK builds, 7 Python tests, 32 Node tests and architecture checks passed.

- The user confirmed this crash is from 4.0.10 (60), source `d673ef9`, which predates the session retention changes in 4.0.11. The still-unsafe member stream has been fixed in the current work and is covered through real SDK restoration states. The source/build-specific mapping investigation continues.

- Matching 4.0.10 mapping recovered from source `d673ef9` and verified against the exact reported `e78f…` R8 ID. `xi0` is `CommonError.NotAuthenticated`; `ma6.b` is `SupabaseAccess.userId`; `mf2.a` is `GetMembersStreamUseCase`; `pw1` is the Expenses view-model member-stream collector. This confirms the synchronous user lookup as the reported crash path. No reconstructed mapping was uploaded with a newly generated Crashlytics ID.
