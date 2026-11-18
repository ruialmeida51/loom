# Concise contextual action labels

Reviewed against main `6a512eb` with `ai/agents/AGENTS_REVIEWER.agent.md` after validation.

Grocery, task, shopping-list, expense, spending-account and receipt actions now say **Delete**.
Bulk-delete confirmation also says Delete; the selection toolbar retains its useful count.
Invitation and draft-photo actions say Revoke and Discard. Recurring cancellation uses Cancel/Keep
so the two choices remain distinct. Targets, consequences and scope stay in dialog titles/body;
standalone accessibility descriptions and account-deletion navigation retain their context.

Resources remain in their owning features. The bulk confirmation has its own resource, separate
from the counted toolbar action. The engineering rules, component guide, design language and
reviewer checklist record the convention. Existing UI selectors were updated; no new tests were
added for this copy change. Business logic, typed errors and confirmation guards are unaffected.

Validation: all 49 existing shared UI flows and the Android debug build pass; 7 Python and 28 Node
checks, resource/package audit (669 strings/plurals) and diff whitespace pass. Native Android and
iOS/Xcode checks run in CI before merge. Backend checks were not repeated for this UI-only change.

Final reviewer result: **pass**, with no unresolved findings in this scope. CI and Firebase delivery
receipts are recorded on the PR after they complete.
