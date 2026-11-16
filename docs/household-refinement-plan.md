# Household usability refinement

Base: merged PR #11, Loom 4.0.2 (`32b7b19`).

## Delivery checklist

- [x] Completed groceries: individual selection, Select all, household-wide bulk deletion with concurrency checks and feedback.
- [x] Identical checkbox/text geometry for composing, pending and saved groceries; preserve Enter order and keyboard focus.
- [x] Split sheet: accessible minus/plus controls and direct numeric entry for shares, amounts and percentages.
- [x] Shared Plannot-style pull-to-refresh on data destinations; no manual refresh-households button. Preserve form drafts.
- [x] Explicit refresh skeletons, minimum visible duration, silent background updates and failure recovery.
- [x] Reminder completion progress and success feedback, including recurring occurrences.
- [x] Home: one Reminders section, relative + absolute dates in the household zone, up to three pending groceries and View all.
- [x] My account group spacing; sign-out only on My account.
- [x] Household chooser initial skeleton and refresh feedback.
- [x] Six-character, expiring/revocable invitations with guess protection; retain valid existing invitations during migration.
- [x] Shopping list chooser: lists and New list, no Done footer.
- [x] Expenses: clearer amount/title/payer/date/personal share hierarchy.
- [x] Destructive filled/outlined buttons throughout, documented and audited.
- [x] Plannot-derived pattern guides, local skill and final reviewer checklist.
- [x] Research notes, focused behavior tests, platform verification, new migration deployment and final package/style audit.
- [x] Changelog, PR and CI; release version allocated by existing workflow.

Read-only destinations include Home, Shopping, Balances, Reminders, Feed, recurring expenses,
expense details, household details, household chooser, My account and Insights. Static More/legal
screens and editable forms do not invent network refresh gestures; forms keep explicit retry
for failed reference data without discarding user input.

## Research decisions

- [Google Keep checklist behavior](https://support.google.com/keep/answer/6358550?co=GENIE.Platform%3DAndroid&hl=en):
  append entries in a stable order and separate completed rows. Selection changes deletion targets,
  not completion state. Keep one composer node across writes.
- [Tricount custom splits](https://www.tricount.com/features/custom-split-expenses):
  edit each participant's share in context and show resulting amounts before committing.
  Loom adds accessible steppers without removing text entry.
- [Tricount expense management](https://help.tricount.com/articles/how-can-i-manage-my-tricounts-and-expenses):
  retain the shared ledger and payer context. Loom's compact expense cards make the total and the
  current user's share distinguishable without making every row a paragraph.
- [Material snackbars](https://m3.material.io/components/snackbar/overview):
  acknowledge a completed action without blocking the next task. Errors remain actionable;
  recurring completion confirms the occurrence even when the next occurrence remains visible.
- Plannot source: `ai/PATTERNS_*`, `ai/agents/AGENTS_REVIEWER.agent.md`,
  `PullToRefreshContentContainer`, `DefaultScaffoldContainer`, shimmer and snackbar components.
  Adapt to Loom's feature packages, central DI, household zones and localized resources.

These are implementation decisions inferred from the cited patterns, not claims that Loom is a
pixel-for-pixel copy of those apps. Verification and remaining platform checks are recorded in [the final audit](household-refinement-audit.md).
