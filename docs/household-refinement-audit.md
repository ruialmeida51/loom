# Household refinement audit

Base: merged PR #11 / `32b7b19` (Loom 4.0.2). Scope is tracked in
[the delivery checklist and research notes](household-refinement-plan.md).

The final review follows [the Plannot-derived reviewer procedure](../ai/agents/AGENTS_REVIEWER.agent.md).
It covers the final diff and surrounding code after fixes. The automated project audit also checks
ownership, dependencies, resources, DTOs, DI and typed-error boundaries across the project.

## Delivered behavior

| Area | Result |
| --- | --- |
| Shopping | Select completed items individually or all at once; confirm one atomic household-wide removal. Version conflicts and restored items abort the batch; retry after a lost response is idempotent. |
| Entry stability | Input, pending and saved rows share 48dp leading geometry and the same text column. Serial writes, stable IDs and the single composer remain intact. Unused category/suggestion projections no longer run on every keystroke. |
| Refresh | Home, Shopping, Balances, Reminders, Feed, My account, household chooser/details, expense details, recurring expenses and Insights use the shared refresh container. Explicit refresh shows skeletons; background and post-save reloads stay silent. Drafts, selected targets and previous data survive failure. |
| Reminder feedback | Per-row completion/removal progress, duplicate-tap protection and VM-timed success feedback. A daily/weekly completion acknowledges the occurrence while the next one stays pending. |
| Home | One Reminders section with household-zone relative/absolute dates. Up to three pending groceries from the same selected checklist as Shopping; View all opens that root. No empty shopping card. |
| Splits and balances | Shares, currency amounts and percentages have bounded minus/plus controls and direct input. Invalid text drafts survive a step attempt. Expense cards emphasize total, title, payer and personal share. |
| Account/choosers | Explicit heading/card spacing; initial household and profile skeletons; sign-out only on My account. Household refresh is a gesture and the shopping selector has no Done footer. |
| Invitations | Six case-insensitive, unambiguous letters/numbers, single use, 24-hour expiry, replacement/revocation and ten attempts per user per 15-minute window. Failed attempts persist. Older builds retain their legacy endpoint and valid legacy tokens remain redeemable. |
| Rules | Adapted Plannot guides, local patterns skill, reviewer checklist and an automated destructive-text-button check. Destructive actions use filled/outlined controls, including accessible icon buttons on compact rows. |

## Audit findings fixed

- Equalized decorative/clickable checkbox sizing and removed TextField decoration padding from the composer.
- Removed unused shopping-state grouping/suggestion work from the typing path.
- Kept the composer and pending writes keyed through refresh; froze bulk-confirmation versions.
- Kept read errors separate from action errors where background reads previously erased failures.
- Separated explicit refresh from post-save reloads and retained old data on refresh failure.
- Moved split UseCase resolution outside the atomic state reducer; bounded steppers do not erase invalid text.
- Replaced destructive text buttons and retained confirmation progress/sibling disabling during writes.
- Gave invalid bulk selections a specific domain error/resource rather than the add-batch error.
- Reused themed Loom cards and compacted Home previews; removed the narrow reminder row's wide action text.
- Replaced the blank initial profile avatar with a skeleton.
- Made the invitation counter inaccessible to clients with RLS and an explicit deny policy. Return statuses
  are translated into typed failures only in the data mapper so failed attempts do not roll back.

No current-change blocking or should-fix findings remain after the recheck.

## Ownership and style

Shopping owns bulk-removal policy, requests, state and previews. Reminders owns date models,
formatting resources and completion feedback. Expenses owns step rules, split state and ledger models.
Household owns invitation models/errors/ports. Home aggregates the owning features without moving
models into core. Core contains only generic refresh, feedback, button, icon and card components.
All annotated providers remain in `shared/di`; Gradle dependencies use named project references.
New wire models have separate files and `@SerialName` fields. Demo ports stay in `app/demo`.

New UI copy, plural forms, date patterns and accessibility labels live in feature resources. The
current catalog is English; resource-based formatting supports future translations without rewriting
screens. No raw server-status/error checks were added above data.

## Verification

- 138 shared JVM tests pass, including selected-list previews, partial Home failures, household-zone/year
  boundaries, bounded split values, preserved invalid drafts, refresh failures, selected targets,
  recurring completion, RPC scope/version serialization and typed bulk errors.
- All 32 Compose UI tests pass and exercise selection/Select all/deletion, root navigation from previews, account
  geometry, narrow reminder completion feedback, existing forms, sheets and Back/resume flows.
- Disposable PostgreSQL applies all 15 migrations and runs authorization, financial, recurrence,
  invitation, bulk-removal and upgrade tests. Hosted checks read only schema/permission metadata.
- Android debug/test APKs, common metadata and minified release APK/AAB builds are checked locally.
- Android emulator tests additionally check 30 continuous entries with the real keyboard, checkbox/text
  geometry, pull-to-refresh skeletons and draft retention, split steppers, system Back and resume.
  The PR's Android device and iOS jobs are the authoritative platform results; this Linux workspace
  has no emulator acceleration or Xcode.
- `tools/audit_project.py`, Python/Node release-tool tests, workflow lint and `git diff --check` are run.

The two new migrations are deployed as `20261116102214_short_invites_and_completed_groceries`
and `20261116102653_explicit_private_invitation_policy`; local filenames match hosted history.
Public RPC wrappers are invokers; private definers have a fixed search path and membership checks.
Anonymous execution and direct client access to invitation counters are denied.

The hosted security advisor has no new findings. Its existing
[leaked-password-protection setting](https://supabase.com/docs/guides/auth/password-security#password-strength-and-leaked-password-protection)
remains disabled; this configuration issue predates the change.

**Result:** pass for the scoped architecture/style/behavior audit. Check the PR's platform statuses
for native Android and iOS verification of the current revision. Preview screenshots use dedicated
demo data and do not substitute for device checks.
