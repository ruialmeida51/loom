# Loom screen and interaction language

Loom helps roommates and couples run a home. Follow Material 3 for the shared visual language,
Tricount for contextual expense splitting, Keep for continuous checklist entry and Todoist for
progressive disclosure during capture. The [research](research/usability-and-design-system.md)
records the comparisons and [redesign brief](plans/redesign-research-brief.md) records the scope.

## Foundations

- Use a complete semantic Material color scheme in light and dark. Loom adapts the original
  Lumbridge warm palette; use surface-container roles for grouped content rather than ad hoc fills.
- Use bundled Inter for body/controls and Space Grotesk for headings, following Plannot. The shared
  scale uses 20sp sheet titles, 16sp item titles, 14–15sp body copy and 12sp metadata; headings are
  22–28sp. Money uses tabular numerals. Text scales and wraps; never give copy a fixed height.
  Bundled fonts retain their OFL notices under core composeResources/files/font-licenses.
- Use the named 4/8/12/16/24/32dp spacing scale. Working lists use 16dp phone gutters; related rows
  have compact internal spacing and unrelated sections have 24dp separation where appropriate.
- Shared surfaces use Material shapes, with large shapes for groups/cards and medium shapes for
  nested controls. Open rows use quiet separators; cards represent distinct summaries or articles.
- Preserve at least 48dp touch targets, including visually small checkboxes and stepper icons.
  Density comes from hierarchy and grouping, not smaller hit areas.
- Use resource strings/plurals for UI copy, errors, dates and accessibility. Publisher names and
  user content remain data. Keep useful phrases without repeating the page title or instructions.
- Use the Privacy & Legal compact icon-led row pattern for account actions, preferences and
  list/account/feed management. Group related rows with dividers and a selected/status cue. Keep one
  clear row action; overflow menus hold secondary actions. Sign out and Delete account have separate
  red rows. Inputs remain inputs rather than being disguised as navigation.
- Debt amounts/status use Material error roles; money owed back uses Loom's semantic green roles in
  light and dark. Keep personal names and textual cues so colour is never the only distinction.

## Navigation and editing

- Home, Shopping, Money, Tasks and More are peers. Preserve each tab's stack. Peer changes do not play
  push/pop slides. Android Back from a non-Home root returns Home; only Home root exits.
- Root toolbars contain household selection and the global profile. Selectors use a label-adjacent
  caret and generous touch areas. Child Hosts own Back, title and contextual actions. Global account
  remains outside household scope; sign-out belongs there.
- Forward and backward navigation use one direction policy, including system/predictive Back and
  toolbar Back. The departing destination stays above the returning one with an opaque background.
- The owning Host applies and consumes system/toolbar/bottom-navigation insets once. Scrollable
  forms own their IME clearance. The primary action remains reachable with the keyboard open.
- Use one sheet for a short contextual task: split editing, shopping edits, task creation,
  feed subscription, repayment or nickname editing. Fields scroll above a fixed action footer.
  Close or replace a sheet rather than stacking sheets.
- Task/payment sheets put full-width primary and Cancel buttons in a vertical footer. Optional task
  date controls stay compact, with a gap before the consistently outlined 48dp remove-date control.
- Use full destinations for expense capture/details, account creation and browsing full history.
  Dialogs are for brief confirmations. Disable competing dismissal/actions during a save.
- Cancel, swipe and Back discard only an unapplied sheet draft; they do not discard the parent form.
  Preserve inputs, focus and scroll on resume, identity refresh and retry.
- Login, registration, verification and household selection/create/join remain separate workflows.
  Session initialization/refresh must not temporarily present a signed-out or empty-household flow.

## Data readiness and feedback

- Show matching skeletons during first load and explicit refresh. Sections can become useful
  independently; shopping or optional news must not hold tasks behind a page-wide skeleton.
- Pull-to-refresh briefly shows matching skeletons, with a single approximately 300 ms minimum transition. Retain the existing data, selection, draft and scroll underneath.
  Realtime/polling changes remain silent. A failed refresh keeps prior content and an actionable error.
- Apply Plannot's monotonic helper once for initial/explicit loading only. Resume and background updates never add a timing floor.
- Mutation progress belongs to its button/row, never a page skeleton. Confirm successful recurring
  task completion with a transient snackbar even when the next occurrence remains visible.
- Destructive actions use filled/outlined destructive buttons, including accessible icon buttons,
  with progress and sibling disabling. Menus may open a brief confirmation; never use a text Delete.
- Reference reads may reuse short-lived in-memory snapshots keyed to the current user and scope.
  This is not offline synchronization or financial authorization: writes remain transactional and
  server-validated. Keep cache policy in data and preserve coroutine cancellation.

## Daily workflows

Home starts with two compact quick actions with square icon areas, followed by one Tasks
section with up to three pending items, a three-item preview of the selected shopping list and a
secondary News preview. Each section has an explicit route to the full destination. Optional task dates
combine localized relative and absolute dates in the household zone. Empty tasks/shopping use a compact illustrated state without duplicate add actions; errors remain distinct.
Tasks allows title-only capture. Date, repeat and household notification are optional progressive controls; removing the date also removes repeat and notification intent. A dated task does not imply a notification. Existing reminders retain their schedule and notification behavior. Completed tasks remain collapsed and can be reopened.
Tasks and Money share one labelled add FAB outside the skeleton overlay, with matching list clearance.
News is manually scrolled, never auto-advances, and does not gate household content.

The full reader offers multi-select source pills (empty selection means All feeds) and Unread.
Read 20 articles per page with an explicit Load more action; append failures retain existing rows
and retry the same cursor. Fetch source/unread filters before pagination. Reading is a stable snapshot
until refresh or returning from Manage feeds, while Home keeps a live three-article preview.
Feed management uses compact publisher/status rows with an edit action and overflow Refresh/Remove;
pending or failed subscriptions remain visible even without articles.

Expense capture shows amount and description first, even while a new form's reference data loads.
Loading/retry never overwrites early typing. Keep payer/date/equal-split defaults and one compact
split summary. Optional category/date/recurrence live under More details. A single split sheet
supports equal, weighted shares, exact amounts and percentages with +/- and direct entry. Show
participant results there instead of repeating them on the default form. Existing expense edits
must load the actual record before accepting changes. A failed save retains the complete draft.

Shopping is the working checklist. Enter submits a row and leaves the next draft focused above
IME. Serialize writes in entry order; preserve composer identity and checkbox/text geometry through
pending/acknowledged rows. Failures keep per-row retry/removal. Server-owned positions preserve order.
Completed items stay collapsed and support Select/Select all. Confirmed bulk deletion freezes record
versions; concurrent restoration or editing rejects the whole request. List selectors need no Done.

Balances owns separate Everyday/trip/event accounts and who-pays-whom views. Never combine currencies
or debts across accounts. The active picker stays simple; Manage accounts owns archived history,
Reopen and Delete. A trip/event deletion removes its records for everyone and requires its creator or
household owner; Everyday remains the built-in account. Contextual actions expose recurring bills
and current balances. Expense rows prioritize title and total, then payer, personal share and the actual
expense date, without redundant Today or daily section headings.
Expense details expose Edit/Delete in their Host toolbar menu; the brief destructive confirmation
uses a full-width button. Repayments use personal wording and named Paid by/Received by selection rows.

More retains Feed and household details. Profile/preferences have explicit section
spacing. Avoid importing unrelated personal-finance calculators while daily usability is the priority.

## Components, ownership and review

Follow the [component inventory](../ai/PATTERNS_COMPONENTS.md) and [patterns skill](../.agents/skills/loom-patterns/SKILL.md).
Core owns only generic layout, tokens, input, motion and sheet infrastructure. Features own their
forms, rows, prepared display values, errors and business models. Hosts connect typed navigation,
ViewModels and sheets. Preview fixtures/ports stay in `app/demo`.

Use stable item keys and immutable presentation collections. Serialize realtime reads and suppress
equal snapshots; stale responses must not undo acknowledged shopping changes. Shimmer reads its
animation in drawing rather than recomposing its entire skeleton every frame.

Finish with the [reviewer procedure](../ai/agents/AGENTS_REVIEWER.agent.md) after validation and fixes.
Review light/dark, narrow screens, large text, empty/loading/error, real IME, system/toolbar Back,
sheet cancellation and background/resume with a draft. Desktop captures validate shared layout;
Android system bars/IME and iOS gestures require native verification. Keep those limits explicit.
