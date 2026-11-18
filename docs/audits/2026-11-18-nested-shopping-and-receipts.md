# Nested shopping and expense receipts: final review

Reviewed the final implementation against main `fb13e69`, using
`ai/agents/AGENTS_REVIEWER.agent.md` and the Plannot-derived pattern guides after fixes and validation.
The accepted scope and references are preserved in the
[request ledger](../plans/2026-11-17-nested-shopping-and-receipts.md).

## Findings resolved

- Shopping group writes now freeze the complete affected family before any change. Parent completion,
  moving, child aggregation and Undo use an atomic RPC; stale sibling versions reject the whole batch.
  Parent deletion promotes surviving children. Bulk removal returns current list rows so clients adopt
  promotion-generated versions immediately, while feedback counts only selected deletions.
- Group ordering and eligible nesting parents are derived in the shopping domain and cached per
  snapshot. UI indentation does not change root checkbox/composer geometry. Menu and accessibility
  actions provide alternatives to swiping. One same-household/list nesting level is enforced in SQL.
- Receipt capture retains a private form draft and stable expense/photo identities. Once financial
  save succeeds, upload retry skips that write. Failed preparation leaves the financial form usable;
  failed upload can retry or finish without the optional photo. Pending writes block duplicates.
- Storage uploads are authenticated JPEGs with unique paths and no upsert. Link changes use original
  version tokens, including removal tombstones, so a stale delete cannot remove a replacement.
  A committed photo can be reconciled after a lost response without another upload or write.
- Private Storage policies and receipt RLS enforce current household/account access. Only an allowed
  expense manager can attach/remove a photo; service-only cleanup uses the Storage API. Link commits
  coordinate with cleanup leases, queued objects cannot be reattached, and failed cleanup retries.
- Camera/Gallery source actions share an overflow-anchored menu. Native launchers live at the Screen
  root; only camera-created temporary files are removed. Action progress preserves content. The
  viewer has a dedicated Host/Screen/contract/VM and a component-owned zoom/pan state holder.
- Persisted deletion retains confirmation, busy dismissal protection and typed retry errors. Copy and
  accessibility labels use expense/shopping resources. Feature models, DTOs, mappers, ports and media
  adapters stay in their feature packages; registration remains in annotated central DI. Sample data
  and image fixtures remain in `entrypoint/app/demo` or tests.

The initial native run found test synchronization issues: the receipt test searched Money before its
initial skeleton finished, and a task test tried to hide a dialog's IME using the underlying Activity
window. The former now waits for content; the latter verifies the visible sheet Save button directly
with the keyboard open. The real photo picker, normalized draft preview and swipe gesture succeeded.
These test fixes were compiled and the final source audit was repeated before the CI rerun.

## Validation

- **206 JVM tests pass**, including **49 shared Compose flows**. New coverage exercises real horizontal
  gestures, Undo/outdent, grouped domain rules, receipt viewer/removal, failed upload retry without
  duplicate expense writes, original version conflicts and authenticated immutable Storage requests.
- Android app and instrumentation APKs build. Native CI adds actual swipe geometry and Android system
  Photo Picker capture, draft retention, saving and opening the receipt, alongside the existing seven
  device flows. CI must execute these nine checks before merge; local APK compilation is not device
  execution. Existing API/Compose deprecation warnings remain outside this scope.
- Disposable PostgreSQL applies **19 migrations** and tests group atomicity, legacy-client cascades,
  bulk parent deletion/promotion, household/outsider access, receipt tombstones and cleanup permissions.
  These fixture tests do not impersonate users against the hosted project or exercise the real Storage
  HTTP service; the Android picker test uses isolated demo ports.
- **7 Python, 28 Node and 9 Deno tests pass**; production Edge Functions type-check. Legal-site
  build/check, final whitespace check and ownership audit pass: **716 Kotlin files**, **20 destinations**,
  **42 DTOs**, **14 catalogs / 667 resources**.
- Shared captures cover light shopping groups and the dark receipt viewer at narrow width/larger
  text. Android's merged manifest adds no broad media-read or camera permission; iOS declares its
  camera purpose. Linux cannot run physical iOS camera flows; iOS Kotlin/Xcode checks remain CI gates.

## Hosted backend

Migration **20261118025837** is applied to `eeyfhmlyopuobtgdfbql`, and **household-worker v2** is
ACTIVE. The local migration filename matches the recorded hosted version. Its existing worker secret
authentication, JWT setting and cron schedule are preserved. Post-deploy metadata checks confirm a
private JPEG-only 5 MiB bucket, receipt RLS, no direct client metadata writes and service-only cleanup.
No customer household records were read or modified for validation.

The advisor reports the existing disabled leaked-password protection setting and an informational
notice for the deliberately client-inaccessible cleanup table with RLS and no client policy. No new
security warning was introduced. Privacy and deletion guidance now explain photos and physical file
cleanup; opt-in analytics receives no photo bytes, object paths or financial contents.

Final reviewer result: **pass**, with no unresolved scoped findings. Native execution and iOS CI must
pass before merging. Exact CI runs, source SHA and Firebase release are recorded on the PR after
delivery. Physical camera/live-provider verification remains a stated limit.

Final reviewer result: **pass**, with no unresolved findings in scope. Native Android execution and
macOS Kotlin/Xcode builds are required CI gates before merge; physical iOS camera remains unverified.
CI and exact Firebase delivery receipts belong to the PR, without a delivery-only main commit.
