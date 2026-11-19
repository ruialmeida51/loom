# Household management, recurring bills and tab consistency

Planning baseline: main `8a1a647`, Loom 4.0.11 (61), after PR 20. Product discussion dated
18 November 2026 (Europe/Lisbon). This document records requested work and decisions; no
hosted database change has been made for this pass. Implementation started on
`feat/household-management-and-tab-consistency`.

## Confirmed product decisions

| ID | Decision | Source/status |
| --- | --- | --- |
| D-01 | Promoting a member adds a co-owner; the current owner keeps ownership. Both have owner permissions. | Confirmed |
| D-02 | Deleting a recurring bill removes its schedule and stops future entries, while keeping expenses/payments already recorded. | Confirmed |
| D-03 | An owner may delete a household with outstanding balances after a clear impact warning and typing the household name. | Confirmed |
| D-04 | Members can leave. A last owner must promote another owner before leaving; deleting the household is the explicit alternative. | Confirmed |
| D-05 | One six-character invitation code can invite several members during its 24-hour lifetime. Owners can revoke or replace it. Existing guess limits remain. | Confirmed |
| D-06 | Owners can remove members after confirmation. Their access ends, while historical financial records remain. | Confirmed |
| D-07 | Owners can demote another owner to member after confirmation. Protect the last active owner. | Confirmed Yes |
| D-08 | Members can leave or be removed with outstanding balances. Show the position in the confirmation and retain financial history. | Confirmed Yes |
| D-09 | Do not automatically pause recurring bills or add a Needs review state when a member leaves or is removed. | Confirmed No to automatic pausing/review |
| D-10 | Existing recurring bills continue unchanged after departure, retaining the payer and split, including future charges for former members. Do not require reassignment before leaving/removal. Remaining owners can edit them later. | Confirmed keep running unchanged |

All product questions for this pass are answered. Implementation is in progress.
“Expense profiles” is interpreted as Everyday/trip/event spending accounts, separate from the
global My account profile.

## Requested scope and acceptance checks

| ID | Work | Acceptance |
| --- | --- | --- |
| UI-01 | Align the five root tabs: Home, Shopping, Money, Tasks and More. | Shared outer gutters, toolbar-to-content and title-to-card spacing. Preserve the checklist's compact row density, Home's edge-to-edge feed and host-owned insets. |
| UI-02 | Use shared layout/header/management-row components and spacing tokens. | Loaded, empty, error, initial skeleton and refresh states follow the same geometry; check light/dark, narrow widths and large text. |
| BILL-01 | Add a proper recurring-bill editor for active and paused rules. | Edit title, amount, payer, split and billing date/day. Changes affect future generated entries; prior entries stay unchanged. Saving a paused rule does not resume it. |
| BILL-02 | Replace the crowded recurring-card button row with anchored overflow. | Compact amount/title/schedule/status card; menu offers Edit, Pause/Resume and Delete as appropriate. Recurring rules keep their own form VM and screen, with date/split pickers reused. |
| BILL-03 | Delete schedules, including old cancelled schedules, with confirmation. | Removed schedules no longer clutter the list or generate charges. Existing expenses and their recurrence/idempotency references remain valid. Failed writes retain target/version/error for retry. |
| MONEY-01 | Make the account selector/manager follow Shopping lists. | One immediate-selection sheet, current-account indicator, per-row Edit/Archive/Reopen/Delete overflow, visible archived section and one New account action. Remove the extra manager hop and redundant Close/Done controls. Preserve Everyday account protections. |
| INVITE-01 | Show the active invitation in More → Household details. | Owners can reopen the page/app and see the current code, expiry and Copy/Share actions. View never creates or rotates a code. Replace/Revoke are explicit and version-safe. |
| INVITE-02 | Make new codes reusable for 24 hours. | Existing members get an idempotent Already joined result; joining grants member access only. Expired/revoked codes and repeated guesses are rejected. Revoke the current shared invite on owner removal of a member so it cannot undo removal. |
| MEMBER-01 | Add member management under More → Household details. | Role badges and anchored member menus. Make owner is confirmed and explains owner powers; Make member is also confirmed. Last-owner protection is enforced transactionally for demotion, removal and leaving. |
| MEMBER-02 | Add Leave household and owner Remove member. | Confirm target, access changes and per-account balance impact. Outstanding balances do not block departure. Preserve member references/names and prior spending/payment attribution. Rejoining requires a valid subsequent invitation. |
| MEMBER-03 | Keep existing recurring schedules running after a member leaves/is removed. | Preserve payer/shares and future generation, including charges for the former member. No automatic pausing, Needs review state, departure blocker or silent split redistribution. Remaining owners can edit bills. New participant assignments still require current members; unchanged historical participant references may be retained on existing rules. Preserve old records and balances. |
| HOUSE-01 | Add owner-only Delete household under More's household management. | Household-name confirmation and an explicit summary of shared data/history/photos affected. Delete only this household; retain the global login/profile and other households. |
| STATE-01 | Recover navigation and permissions after household/member changes. | Leave/delete clears the selected household and opens the chooser with feedback. Other devices handle lost membership as a household change, without pretending the user signed out. Refresh roles after promotion/demotion. |
| DATA-01 | Add new migrations and scoped, version-aware RPCs. | Current membership/role checks, household locking, last-owner race protection, invitation secrecy/limits, ledger-history integrity and retry behavior verified in disposable PostgreSQL before hosted application. |
| AUDIT-01 | Finish the feature/layer, state, typography/layout and destructive-action audit. | Household models/policies stay in household; financial models/policies stay in expenses; dependencies remain acyclic. No business rules in composables or feature models in core. Typed errors, resources and central annotated DI throughout. |
| QA-01 | Validate the complete change and capture comparable screenshots. | Domain/VM/SQL/Compose UI checks, real Android Back/IME/resume scenarios and iOS compilation/native build. Compare the five tabs at matching viewport/font sizes. |
| RELEASE-01 | Changelog and normal Firebase release after validation. | Let the existing allocator select the next version; distribute to internal-testers. Play/tag only when explicitly requested. |

## Household permission and state matrix

| Action | Owner | Member | Important condition |
| --- | --- | --- | --- |
| View active member list and roles | Yes | Yes | Current household access required. |
| View/copy/share/create/replace/revoke active invite | Yes | No | Viewing is read-only; replacing/revoking targets the displayed invitation. |
| Make another member an owner | Yes | No | Adds a co-owner; does not demote the actor. Confirm the permission change. |
| Make another owner a member | Yes | No | Confirm and retain at least one active owner, including concurrent changes. |
| Remove another member/owner | Yes | No | Confirm access loss and balance impact; preserve history and last-owner protection. |
| Leave household | Yes | Yes | Last owner must promote someone first or use explicit household deletion. Debt and existing recurring bills do not block departure. |
| Delete household | Yes | No | Impact warning and household-name confirmation; outstanding balances allowed. |
| Access household after leaving/removal | No | No | Clear local selection/navigation; do not delete the person's global account. |

Preserved financial history must remain usable: keep former members' names and balances in historical
records and audit payment selection/validation so an otherwise-authorized remaining participant can
record a repayment involving them. This does not grant former members household access or make them
available for new expense assignments. Keep the existing payment authorization rules.

## Code findings that drive the work

- Root top content offsets differ: Home/More use 16 dp, Shopping/Money 8 dp and Tasks 12 dp.
  Title-to-card spacing is also owned separately. Shared tokens already exist; use shared geometry
  instead of replacing one set of scattered literals with another. Proposed rhythm: 16 dp outer/top
  and title-to-content spacing, 24 dp between sections, 8 dp between ordinary cards; retain dense
  checklist rows and any documented feature-specific spacing.
- RecurringExpenseDataSource exposes stream/controlSchedule only. There is no recurring edit/read/save
  workflow, so this is missing functionality rather than simply a paused-state button bug.
- Recurring cards put Pause/Resume and Cancel beside each other. Cancelled schedules remain visible,
  and the domain's existing canManage excludes them. Deletion needs its own authorized policy so
  old cancelled rules can also be removed.
- Recurring rules retain integer share allocations, while split_method/split_values live on expenses.
  A schedule editor must preserve the intended allocation method and migrate old schedules honestly;
  infer from a linked original record where reliable, otherwise show the existing exact amounts.
- Account selection and account management are two different sheets; Shopping combines selection
  and row actions in one. Unify the interaction without mixing the two domains' models.
- HouseholdDetailsState stores only the just-created invitation string, and closing its sheet clears
  it. The household port has no active-invitation read or role/leave/remove/delete operations.
- Invitation issuance already creates one active six-character code per household, valid for 24 hours,
  stored as a hash and consumed on first redemption. Add reusable semantics and protected recovery
  for new codes. Existing hashes cannot be reversed: keep old codes valid under their existing
  rules until expiry/revocation, and offer explicit replacement when their value cannot be shown.
- The database role model/is_owner helper already allow multiple active owners. New actions must
  enforce owner permissions rather than treating households.created_by as the sole permanent owner.
- Financial tables reference household_members and generated expenses reference recurring rules.
  Deactivate departing members rather than deleting their historical identity rows. Household
  deletion needs an explicit dependency/receipt-cleanup design; it is not a bare delete request.
- The current scheduled worker already sets active=false when a rule's payer, creator or split
  participant is inactive. Resume applies similar checks. Merely omitting a new pause operation
  would therefore still pause bills in the background, contrary to D-09/D-10. Update and test the worker,
  resume and edit policies together to retain existing payer/split references. A past creator who is no longer involved
  as payer/participant must not independently invalidate a bill; retain their historical attribution.

## Backend and interaction safeguards

- Household roles, invitations and lifecycle operations belong in household. Recurring controls and
  monetary rules belong in expenses. Coordinate cross-feature lifecycle effects transactionally on
  the backend without introducing a household → expenses → household dependency cycle.
- Check current database membership/role on every write, including demotion/removal/deletion.
  Serialize member/owner operations using a consistent household-first lock order. Concurrent owner
  exits/demotions cannot leave an existing household without an active owner.
- Keep invitation redemption hashed. Owner-only redisplay needs a protected recoverable-code path
  (for example managed encrypted secret storage); never expose secret tables or log codes.
  Concurrent Replace/Revoke targets a frozen invitation identity/version, not whichever code happens
  to be current when a delayed confirmation is submitted.
- Recurring edit/pause/resume/delete and scheduled generation must be version/lock safe. No edit
  rewrites historical entries, no deleted rule is generated later, and resume keeps the established
  no-backfill behavior. Preserve creator/owner authorization and active account validation; permit
  retained former-member payer/split references on existing schedules while validating new assignments
  against current members. Membership loss still revokes that former member's access and write permissions.
- Leaving/removing a person preserves past shares, payments and their attribution. Outstanding
  positions are shown per spending account/currency, never summed across different currencies.
  A remaining owner can manage bills created by a departed person. Do not add an automatic
  pause/review workflow rejected in D-09 or silently rewrite their payer/split.
- Household deletion revokes invitations/access, stops scheduled jobs, removes its scoped records,
  and queues private receipt/media cleanup durably. A partial network response must be safely
  retryable. No deletion of the global account/profile or data in other households.
- Use concise destructive labels and the shared filled/outlined controls. Brief confirmations use
  dialogs; a household deletion with an impact summary/name input can use a focused confirmation
  sheet. Keep progress/errors/target in the VM and block duplicate writes and busy dismissal.
- Initial/manual refresh keeps the requested 300 ms matching skeleton transition; resume/background
  updates stay silent. Preserve drafts, focus, selected account/list and scroll.

## Required regression cases

- Active and paused bills can both be edited; saving a paused bill leaves it paused. Concurrent
  edit/pause/delete/generation cannot create duplicate entries or rewrite previous expenses.
- Deleted active, paused and old cancelled schedules disappear while their existing expenses remain.
- Two owners trying to leave/demote/remove concurrently cannot leave an ownerless household.
  Stale local roles never authorize a server operation. A demoted owner loses privileged controls.
- Leaving/removal with a positive, negative or zero balance preserves the ledger, names and existing
  payment permissions. Show separate account/currency positions; verify historical repayment flows.
- Bills keep their existing payer/shares and generate after departure, including a former payer,
  split participant or creator. No hidden worker auto-pause or departure blocker. Owners can edit
  or resume existing rules with retained references; new participant assignments require current
  members, and former members do not regain access. Confirm no silent split changes or backfill.
- Multiple people can redeem one new invitation within 24 hours. Already-joined, expired, revoked,
  replaced, legacy single-use and rate-limited attempts have explicit, typed outcomes.
- Removing a member and redeeming the old shared code concurrently cannot restore their access.
  A subsequently issued invitation can intentionally allow rejoining.
- A household deletion removes only the confirmed household, including its financial history after
  the warning. Receipt cleanup survives the deletion and retries; other households/global account stay.
- Retry after a lost response is safe. Failed confirmations retain their target and typed error;
  app resume, external membership changes and household switching cannot submit against stale context.
- Five-tab screenshots compare initial, refreshed, loaded and empty states with matching spacing,
  insets, light/dark themes, narrow viewport and large text. Back/IME behavior stays consistent.

## Execution order

1. Product decisions and the permission/state matrix are complete; implement the accepted scope below.
2. Align shared root/header/manager geometry and unify the spending-account manager.
3. Add and test recurring read/edit/delete contracts and transactional backend changes, then wire
   the separate form/overflow UI.
4. Add/test household roles, lifecycle and invitation behavior, then wire Household details/More,
   code sharing and membership-aware navigation cleanup.
5. Run final audits, SQL/UI/native checks and screenshot review; apply required hosted migrations
   after validation and complete the normal versioned Firebase delivery.

## Sources inspected

- `shared/features/{home,shopping,expenses,tasks}/presentation/screen` and entrypoint MoreOverviewContent.
- RecurringExpenseDomain, RecurringExpenseDataSource, RecurringExpensesScreenViewModel/Screen.
- ExpenseAccountsBottomSheet, ManageExpenseAccountsBottomSheet, ShoppingListsBottomSheet.
- HouseholdDetailsState/ViewModel/Screen, HouseholdDataSource and household foundation/invitation SQL.
- `ai/PATTERNS_BOUNDARIES.md`, `ai/PATTERNS_UI.md`, `ai/PATTERNS_COMPONENTS.md`.
- Supabase changelog and current Vault documentation: https://supabase.com/docs/guides/database/vault.

## Execution progress

- Complete: UI-01/UI-02 shared root padding, shared static heading geometry and matching selector gutters.
- Complete: MONEY-01 single account selector/manager with an archived section and per-row actions.
- Complete: BILL-01–03 dedicated active/paused schedule editor, anchored actions and deletion retaining recorded expenses.
- Complete: INVITE-01–02 and MEMBER-01–03 persistent/reusable codes, co-owner/member operations and unchanged bills after departure.
- Complete: HOUSE-01 scoped household deletion with name confirmation and durable receipt cleanup.
- Complete: STATE-01 explicit exits/remote membership loss clear household selection while keeping login; connectivity failure retains context.
- Local validation: 255 JVM tests (including 57 Compose/navigation checks), Android app/test APK builds, 22 SQL migrations/integration suites and a real concurrent owner-exit test pass. Python 7 and Node 32 checks pass; legal pages build/check.
- Local review: compiler evidence records all restartable functions skippable in the five reviewed modules; source ownership audit passes. Screenshots compare all five roots.
- Native verification: all 12 Android emulator tests and the iOS/Xcode build passed in [PR CI](https://github.com/ruialmeida51/loom/actions/runs/44430999776). Device captures include paused-bill editing with the real keyboard and retained state across resume.
- Hosted backend: both migrations applied successfully as `20261119013329_recurring_bill_editing_and_deletion` and `20261119014955_household_lifecycle_and_reusable_invitations`. Repository filenames match that recorded history; SQL bodies are unchanged from the validated versions. Metadata, RPC grants, private-table access and Vault read restrictions were verified without modifying household data.
- Remaining delivery gates: CI for the final history/documentation alignment, PR merge and normal Firebase release. Actual delivery results are tracked on [PR #21](https://github.com/ruialmeida51/loom/pull/21).
- Future suggestions, outside this batch: saved split presets, task assignments, shopping-trip-to-expense capture.

[Final source audit](../audits/2026-11-18-household-management-and-tab-consistency.md).
