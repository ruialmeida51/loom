# Confirmations and details audit — 17 November 2026

Scope: [accepted request ledger](../plans/2026-11-17-confirmations-and-details.md), based on
4.0.6/main `ef81428`. Review uses the six groups in the
[Plannot-derived reviewer checklist](../../ai/agents/AGENTS_REVIEWER.agent.md).

## Implementation and ownership

| Group | Final implementation and boundary checks |
| --- | --- |
| Core components/theme | Generic equal-width sheet actions, secondary button, confirmation progress/error handling, caret popup slots, read-only metadata and decorative avatars. No feature models, routes or business rules in core. Medium button shapes, semantic success colours and accessible touch targets. |
| Account | Sign out opens a feature-owned confirmation before the existing Host callback. Cancel preserves the session. Appearance expansion is local; committed theme/error state remains in the VM. |
| Shopping and Tasks | Single grocery/task deletion requires request/confirm; concurrent taps and dismissal while saving are blocked. Typed failures retain the target for retry, and grocery failure restores its prior version. Existing bulk/list confirmations and Undo remain. Task completion stays direct with existing progress and feedback. |
| Household | Prepared member rows belong to household presentation, with named members and owner/current-user cues. Read-only currency/time-zone rows and a contextual invitation action replace loose text. The code shows its existing one-use/24-hour lifetime. Revocation confirms active-code invalidation and preserves the code/error on failure. Empty member groups are not rendered. |
| Money | Expense display models and mapper stay in expenses. The screen groups total/personal share, actual localized date/payer/account and named allocations. Payer green is explicitly labelled; allocations are not described as outstanding debt. Toolbar Edit/Delete remain. Recurring cancellation uses the shared confirmation without changing financial policy. Split steppers and direct entry retain precise money arithmetic. |
| Hosts/navigation/data/DI | All 13 sheet callers use the shared footer, while registration and controller ownership remain in entrypoint Hosts. Theme/member/split menus attach to their actual caret. No data contracts, DI registrations, backend functions, migration or authentication policy changed. |

Forms/draft selections use **Save**; cancellation uses **Cancel**. Read-only sheets use **Close**,
immediate-selection managers retain only the explicit creation action, and dangerous confirmations
retain their action verb. The accepted interpretation of “full width horizontal buttons” is two
equal-width buttons filling one footer; localized labels may wrap without unequal button heights.
Routine checking, optional unsaved-date removal, reversible archive/restore and failed-entry dismissal
remain direct actions. The existing account-deletion row opens the support-assisted legal workflow;
it does not delete an account on tap.

## Review findings resolved

- Popup menus were positioned against full-width rows; theme, member and split menus now anchor to
  the caret. Shared UI checks verify popup placement, not just its visibility.
- Confirmation actions could have different heights when labels wrapped at 1.3× text; shared action
  rows now size both controls to the same height and centre wrapped labels.
- Household loading/error could leave an empty member group; the group now requires members.
- Invitation copy referred to an old lifetime; the resource name and displayed 24-hour lifetime now
  match existing backend policy. No backend behavior was edited.
- The previous vertical task/payment convention is superseded in AGENTS, component guidance and the
  design-language guide by this requested horizontal footer.

## Validation

- The full **196-test JVM suite** passes: **47 shared Compose flows**, including sign-out Cancel/Confirm,
  recurring-task deletion, invitation revocation and split-footer/menu geometry. Feature tests cover
  deferred writes, duplicate confirmation, busy dismissal, typed failure, version rollback and retry.
- Android debug and instrumentation APKs build. The added seventh native check exercises actual
  system Back to cancel task deletion and sign out without leaving the current destination. Native
  execution and iOS Kotlin/Xcode verification remain CI checks; no local emulator/macOS was available.
- **7 Python + 28 Node checks**, legal-site build/check and final diff whitespace pass. The resource
  audit checks feature/package ownership, typed errors, isolated demo ports, direct dependencies,
  central annotated DI and named project references. Unused per-form labels were removed after the
  shared footer replaced them; final resource compilation and Android builds verify those removals.
- Shared sample-data captures inspect light/dark, narrow screens and 1.3× text. They include expense
  details, household details/code, anchored theme/split menus, form footers and task/sign-out
  confirmations. These are layout evidence, not native-device verification.

CI receipts and the actual allocated Firebase version are recorded in the PR after its reviewed
source commit, avoiding unrelated delivery-only commits on main.

Native checks use the isolated demo ports and do not claim verification of live-provider sessions.
iOS compilation/Xcode checks run on CI; this Linux environment cannot verify physical iOS gestures.
No hosted household records were read or changed for this UI work.

Final reviewer result: **pass**, with no unresolved findings in this scope. Native execution and
iOS verification are required before merging; actual CI and delivery results belong to the PR.
