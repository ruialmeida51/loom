# Nested shopping and expense receipts

Accepted: Keep-style nested checklist items; one receipt photo per expense, from camera/gallery.
Baseline: main fb13e69 / Loom 4.0.7 (57). Offline support remains deferred.

| ID | Scope | Status |
| --- | --- | --- |
| SHOP-01 | Swipe right under the preceding parent; left to move out, one nesting level | Complete |
| SHOP-02 | Menu/accessibility alternatives; stable grouped ordering and aligned row geometry | Complete |
| SHOP-03 | Parent checking includes children; children remain individually checkable; move/delete/Undo/bulk selection preserve groups and concurrency | Complete |
| PHOTO-01 | Optional single receipt on expense capture/edit/details, native gallery/camera, bounded compressed JPEG | Complete |
| PHOTO-02 | Private household/account-authorized Storage, atomic attachment/replacement, confirmed removal | Complete |
| PHOTO-03 | Upload progress/retry retains the draft; financial save is not duplicated; full-photo zoom | Complete |
| PHOTO-04 | Remove replaced/deleted/orphaned objects through Storage API cleanup, including expense/account deletion | Complete |
| SHEET-01 | Latest user correction: full-width vertical sheet actions, Save above Cancel; shared component/rules and narrow/IME checks | Shared/JVM/APK checks complete; native CI pending |
| AUDIT-01 | Resources, typed errors, feature packages, annotated DI, isolated demo ports, final Plannot review | Complete |
| DELIVERY-01 | Disposable SQL/RLS, feature/UI/native/platform checks, deploy additive migration/worker and Firebase release | CI and Firebase pending; local checks/backend complete |

Reference: Google Keep Help, “Make a list” (support.google.com/keep/answer/6395451), verified 17 November.
Keep permits one indent level and reverses it by dragging left; the first item cannot be indented.
Use the row menu as an alternative to the gesture. Removing a parent keeps its children; moving a
parent moves the group. Shared destructive deletion keeps its existing confirmation.

Supabase changelog and current Storage/Kotlin/RLS docs were reviewed. Modern SDK transports support
the current TLS requirements. Validate against local fixtures and hosted schema metadata, not hosted
household records. Every schema change is additive; do not rewrite an applied migration.

## Verification and backend

207 JVM tests pass, including 49 shared Compose flows. Android app and instrumentation APKs build.
The disposable PostgreSQL suite applies all 19 migrations and tests nesting, atomic conflicts, bulk
parent removal/promotion, private photo access, tombstones and queued cleanup. 7 Python, 28 Node
and 9 Deno tests pass; source ownership audit and legal build/check pass. Android emulator and
iOS execution/compilation remain required CI gates before merge; physical iOS camera is unverified.

Supabase project eeyfhmlyopuobtgdfbql records migration 20261118025837
(shopping_nesting_and_expense_receipts); the CLI-created local file matches the recorded version.
household-worker v2 is ACTIVE with its existing secret authentication, JWT setting and schedule.
Private bucket size/MIME settings and app/service-role grants were verified after deployment.
No customer household content was read or changed for verification.

See the final audit. Delivery status and exact Firebase version will be recorded on the PR after
CI passes and the release workflow completes; no Play upload is part of this default release.

Latest steering, 18 November: vertical bottom-sheet actions supersede the prior side-by-side footer decision. Read-only Close and immediate-selection manager behaviour remain as specified. Native screenshot review also replaced the misleading RSS receipt glyph with a generic photo icon.
