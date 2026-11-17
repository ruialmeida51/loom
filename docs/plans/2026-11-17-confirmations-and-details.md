# Confirmations, details and sheet consistency

Baseline: Loom 4.0.6 (56), main ef81428. Accepted on 17 November 2026.

| ID | User request | Implementation / audit scope | Status |
| --- | --- | --- | --- |
| SAFE-01 | Confirm sign out | Request/cancel/confirm before invoking the existing sign-out callback | Locally verified; CI tracked in PR |
| SAFE-02 | Confirm deleting tasks and other dangerous actions | Task and single-grocery delete, invitation revocation; retain existing expense/account/list/feed/bulk-delete and recurring-stop confirmations | Locally verified; CI tracked in PR |
| UI-01 | Pragmatic additional colour | Semantic roles for payer/personal share, household owner/current member and selected/completed states; retain text cues and light/dark contrast | Locally verified; CI tracked in PR |
| MONEY-01 | Improve expense details | Amount/title hierarchy, localized date and contextual metadata, grouped named participant shares; prepare display values outside composition | Locally verified; CI tracked in PR |
| SHEET-01 | Modernise split buttons | Consistent modern footer, input/stepper shapes and selection styling; preserve precise amounts and +/- behavior | Locally verified; CI tracked in PR |
| SHEET-02 | Audit every bottom sheet for full-width horizontal actions | One shared equal-width footer; optional single full-width action for immediate-selection/read-only sheets. Optional layout clarification requested; default side by side | Locally verified; CI tracked in PR |
| COPY-01 | Align Save / Done wording | Save for committing forms and draft selections, Cancel for discarding, Close for read-only views. Dangerous confirmations keep explicit Delete/Sign out/Revoke labels | Locally verified; CI tracked in PR |
| HOME-01 | Improve household details | Household summary, readable currency/timezone, grouped members with role/current-user cues and contextual invitation presentation | Locally verified; CI tracked in PR |
| MENU-01 | Popups should open beside the clicked arrow | Anchor theme/member/split menus to their caret; inspect existing overflow and exposed-field anchors | Locally verified; CI tracked in PR |
| AUDIT-01 | Keep patterns and all inputs tracked | Resources, typed errors, feature-owned display/confirmation state, Host ownership, Plannot-derived final review, captures and relevant checks | Locally verified; CI tracked in PR |

Confirmation protects shared/destructive actions and sign out. Routine checking, optional unsaved date
removal, reversible archive/restore and local failed-entry dismissal retain direct interaction.
No backend or financial-policy change is planned. No hosted household records are needed for testing.
Release notes and version allocation use the existing release skill; Firebase remains the default.
CI/delivery receipts will be recorded in the PR so the source audit remains tied to its reviewed commit.

The [final source audit](../audits/2026-11-17-confirmations-and-details.md) records the 196 shared
checks, relevant Android builds and remaining native/iOS validation. No accepted item is deferred.
