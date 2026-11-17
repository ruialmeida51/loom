# Compact interactions audit — 17 November 2026

Scope: the [accepted request ledger](../plans/2026-11-17-compact-interactions.md), based on
4.0.5/main `7a11c2c`. Reviewed directly against the six groups in
[the Plannot-derived reviewer checklist](../../ai/agents/AGENTS_REVIEWER.agent.md) after fixes and
local validation. CI/native results are recorded below separately.

## Outcome and ownership

| Group | Final implementation and boundary checks |
| --- | --- |
| Shared components/theme | Bundled licensed Inter/Space Grotesk and one compact Material scale, semantic positive roles, generic labelled FAB/scroll clearance, vertical form-action option, emphasis colour on NavigationRow, consistent destructive-icon border and compact empty illustration. Touch targets remain 48dp. Core has no feature models or routes. |
| Account | Appearance, preferences and separate red Sign out/Delete account rows follow Privacy & Legal. Only menu expansion is local state; settings/profile/error/consent remain VM-owned. The existing deletion-information destination and opt-in analytics policy are preserved. |
| Home/Shopping/Tasks | Home quick actions and empty glyphs are smaller. Tasks uses the same labelled FAB as Money and moves feedback above it. The task footer stacks full-width Save/Cancel; date remains optional, compact and separated from its outlined remove icon. Shopping manager groups icon-led rows, marks current selection and retains overflow permissions. |
| Households | MemberPickerRow is the single shared household-owned member selector, including ID-based choices, optional current-user naming and payment colour. Existing household grouped rows gain a household icon. Household context and member models stay in that feature. |
| Money | Personal transfer wording, red debt/green money owed back, compact split typography/spacing and extra tab-to-content spacing. Account selectors/managers use grouped icon rows. Expense details exposes its feature-owned action menu through Host toolbar actions; ownership, busy disabling and full-width destructive confirmation are retained. Financial allocation/writes/permissions are unchanged. |
| Feed/data/DI/navigation | Query/cursor/page models and GetFeedPageUseCase belong to feed. Instant parsing is data-only. Filters use subscription identity, multi-source union and unread-before-limit. Generation guards reject stale first/append pages; append failure keeps rows/cursor for retry. All providers remain in shared/di and Hosts own routes/sheets. Reader pages stay stable until refresh or return from source management; Home keeps a bounded three-article live preview. |

Source management retains visible pending/failed status even with no articles, one row action for
address editing and a secondary overflow for refresh/remove. Existing More and household navigation
already uses the preferred grouped rows. Ordinary text/amount fields remain inputs.

## Review findings fixed before the final pass

| Finding | Fix and verification |
| --- | --- |
| Duplicate member-selector components would create competing patterns | Extended existing household MemberPickerRow and removed the new duplicate; shared expense-entry/payment flows and Android builds pass. |
| Mock cursor test expected a timestamp without PostgREST logical-filter quoting | Checked the installed SDK's escaping implementation and assert the exact quoted date/ID expression. The app uses typed query builders; no raw OR string construction was introduced. |
| Old follow-button UI regression targeted removed duplicate navigation | The flow now opens Manage feeds → Add a feed and retains the original validation/error-lifetime assertions. |
| Source controls would remain a separate three-button card style | Reused grouped icon rows plus overflow; the existing add/edit/remove UI flow passes. Pending/failure copy remains visible. |
| New selected checkmarks lacked an accessibility state | Added selected semantics to shopping-list rows and theme/member choices; existing group-management UI checks verify selected groceries. |
| Extracted money/account components left obsolete imports | Removed unused imports, callbacks and the unused new You resource; static audit and compilation pass. |

No unresolved blocking or should-fix findings remain in the scoped local diff.

## Verification

- Full local validation: **189 JVM tests**, zero failures/skips, including **43 shared Compose UI
  flow checks** and **19 feed unit/transport checks**. Existing flows cover splits, editing/deletion,
  task schedules/completion, settings, source management and draft/refresh/navigation retention.
- Feed pagination checks cover equal timestamps, multi-source + unread filtering, exact page ends,
  duplicate taps, stale first/append responses, retrying the same cursor and typed permission denial.
  HTTP mocks check household scope, authenticated bearer, server filters/order and bounded request
  size; those are transport checks, not proof of deployed RLS.
- Android debug app and instrumentation APK build. The six native checks include the new task FAB
  capture/save with the actual keyboard, alongside existing keyboard geometry, continuous entry,
  refresh retention, split draft/resume, nested Back and task-completion checks.
  All six native checks passed twice in final-revision CI; no local emulator or macOS/Xcode was available here.
- Disposable PostgreSQL: **18 migrations** and integration suites pass, including 20/20 cursor pages
  at equal timestamps, source/unread filters before pagination, publisher names from two sources on
  the same host, security-invoker metadata and outsider denial.
- Tooling: **7 Python + 28 Node checks**, legal-site build/check, release notes **363 characters**,
  final diff whitespace and architecture/resource audit pass (**656 Kotlin files**, 19 destinations,
  38 DTOs, 14 resource catalogs/620 strings and plurals).
- Sample-data captures inspect light/dark, narrow screens and 1.3× text: Home, Tasks/optional schedule,
  vertical task sheet, payment direction, split stepper/direct entry, Money, grouped settings,
  shopping/account selectors and source management. New feed captures exercise multiple pages and
  multi-source unread filtering. These are shared layout captures, not native-device verification.

## Backend

Applied **20261117174412_feed_reading_pagination** once to project `eeyfhmlyopuobtgdfbql`.
The migration adds household/date/ID and source/date/ID indexes plus the
`feed_subscriptions_with_publisher` view using `security_invoker=true`. Metadata confirms
`authenticated` SELECT and no `anon` SELECT. Underlying household policies are unchanged. No customer
records were read or mutated for verification, no financial writes changed and no Edge Function
redeployment was required.

Security advisors have no new finding. The existing
[leaked-password-protection configuration warning](https://supabase.com/docs/guides/auth/password-security#password-strength-and-leaked-password-protection)
remains outside this UX change.

## CI and delivery

Final app revision `a8fcb51` passed [the complete CI run](https://github.com/ruialmeida51/loom/actions/runs/44143887878):
shared/Android, disposable PostgreSQL/RSS checks, all six native Android flows and iOS Kotlin/Xcode
builds. The [independent PR run](https://github.com/ruialmeida51/loom/actions/runs/44143903388) also
passed all six native Android flows and iOS Kotlin/Xcode. Both use the final selection-accessibility
patch; its relevant local UI regressions and static audit pass too.

Merged [PR #15](https://github.com/ruialmeida51/loom/pull/15) as
`db7719e1e629ba43a194492bff529e3e20029ef3`.

**Loom 4.0.6 (56)** uploaded and distributed to the configured **internal-testers** Firebase group.
The [release workflow](https://github.com/ruialmeida51/loom/actions/runs/44147168578) completed
successfully, including pre-package tests, signing, Firebase upload/distribution, durable release
history and changelog archival. Its planner log confirms `Next Loom release: 4.0.6 (56)`; the
versioned release artifact is `loom-changelog-56`. The workflow summary contains the direct Firebase
release URL. The allocator assigned the version; no Play release or Git tag was created.

Final reviewer result: **pass**, with no unresolved scoped findings. Native Android runs use demo
ports; real-provider session expiry still needs an installed-account check as recorded in the
earlier audit. iOS Kotlin/Xcode build verification does not claim physical-device gesture testing.
