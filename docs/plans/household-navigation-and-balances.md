# Household navigation and balances proposal

Status: implemented on `feature/household-portal-and-balances`; the account migrations are deployed, with the approved Home/More follow-up undergoing final release checks. The decisions below define the implemented scope.

## Agreed scope

- Four household tabs: Home, Shopping, Balances and More.
- Home has square Add expense/Add reminder actions and today's reminders. More opens full Reminders, Feed and household details pages.
- Shopping behaves like a shared Google Keep checklist: inline entry, Enter to add the next item, tap to check off, completed items collapsed, optional details.
- Balances follows Tricount's familiar expense, balance and repayment flows. Priorities are who owes whom, unequal splits and recurring bills.
- Each household has an Everyday account and separate trip/event accounts. Balances and repayments are independent per account.
- Accounts include current household members only. A particular expense can involve a subset of the account's participants.
- Unequal splits support shares, exact amounts and percentages, alongside equal splits.
- One currency per account; no foreign-exchange conversion in this iteration.
- Follow Plannot's household/tenant selector and global My account pattern. Personal settings are accessible outside all households.
- Keep the existing brand, light/dark support, localization and KMP architecture. Offline work remains deferred.

## App structure and toolbar ownership

An authenticated PortalHost owns household selection and global account navigation. It retains the selected household while My account is open, including the originating tab and child stack. Account access also exists when the user has no household.

The compact portal bar shows the household selector at the start and the user's avatar at the end on household tab roots. It collapses for detail/edit flows, the household chooser and My account. Those Hosts provide their own back/close, title and relevant actions; never stack the portal bar above another full toolbar.

HomeHost, ShoppingHost, BalancesHost and MoreHost own their navigation, route-specific toolbars, screen ViewModels and sheets. The app shell owns the four-tab bar. It does not select every feature toolbar's title/actions through a global route switch.

My account contains profile/nickname, theme, notification preferences, analytics consent, privacy/terms, account deletion and sign-out. Household members, invitations, household configuration and account management remain household-scoped. A dedicated account feature owns global profile models and operations; household membership models remain in household. Settings owns preferences. Neither core nor shared navigation becomes a feature-model container.

## Home

- A small Today heading followed by due/overdue reminders in simple rows.
- Upcoming reminders appear below when useful; avoid filling the page with empty sections.
- Square Add reminder and Add expense quick actions following Plannot’s My account cards. Add expense opens the appropriate Balances flow with an explicit return to Home.
- Feed and household details are reached under More, keeping Home focused.
- Household management is reached through More and household selection through the selector; personal settings through the avatar.
- Preserve the friendly tone through short labels and occasional empty-state copy, without an introduction paragraph above every list.

## Shopping

- Open the last-used active list. A compact list-name selector exposes the other existing lists and list management.
- Start with the items: checkbox, item name and optional secondary quantity/note. Editing and checking off are distinct accessible actions.
- Put an inline Add item row after unchecked items. Enter submits and keeps focus ready for the next item. The input remains available after a failed request and shows progress/error at the action.
- Checking an item moves it into a collapsed Completed section; undo is available. Do not lose items when changing filters or lists.
- Categories, favourites, moving items and reusable suggestions stay secondary, in item/list actions or an optional detail sheet. Remove permanent filter/category controls from the default checklist.
- Keep existing multiple-list functionality. Do not require a separate form to enter a plain item name.
- Reordering should behave predictably and persist if included; assess existing ordering support before promising drag handles.

## Balances

- Remember the last-used account, initially Everyday. The account-name selector opens a concise list of Everyday and active trip/event accounts, with create/archive actions.
- Use two clear account views: Expenses and Balances. Recurring bills are a secondary account destination. Keep statements/statistics available through an account action rather than another large section on the landing page.
- Expenses: date-grouped rows with description, payer and total. Repayments are distinguishable entries in the same account history. Tap for a dedicated detail destination with edit/delete actions.
- Balances: participant totals plus actionable repayment suggestions, e.g. "You owe Ana €24". Record payment opens a prefilled review; saving records a repayment, it does not move money.
- One Add expense action. The dedicated expense form shows description, amount, payer and participants first. Equal is the default; Split opens a focused editor for equal/shares/amounts/percentages and a visible per-person preview.
- Keep date, category and recurrence secondary. Use a native date picker rather than requiring an ISO date string. Reuse concise bottom sheets for short choices; use full-screen destinations for expense detail and substantial split/edit flows.
- Retain existing monthly recurring-bill behavior and pause/resume/cancel controls. Show the next occurrence and avoid treating a future scheduled bill as an already incurred expense. Additional frequencies are outside this iteration unless separately agreed.
- Create a trip with name, currency and household participants. Archive completed trips without erasing expenses or repayments. No external guests, invitation-by-name identities or exchange-rate service in this scope.

## Back and state rules

1. Dismiss an open transient surface, then pop the active flow's child destination.
2. At the root of Shopping or Balances, Android system Back returns to Home. Only system Back at Home's root can leave the app.
3. Toolbar Back/Close returns to the preceding destination or the explicit origin; it never exits the app. Root tabs do not show a back arrow.
4. Switching peer tabs preserves their stacks and list/account selection and uses a peer transition, not a push animation.
5. A Home shortcut that opens a specific task returns to Home when the task closes. Opening Shopping as a tab uses the root rule above.
6. Opening My account or the household chooser and returning without changing context preserves the household flow. Cancelling a chooser must not clear the selected household.
7. Restoring a session or resuming the app must not reset forms or navigation. Household-specific streams and state stay keyed to the correct household.
8. iOS edge-back follows the same child-flow hierarchy; switching peer tabs never behaves as an edge-back pop.

## Density and display rules

Use one clear title per destination, shorter labels, fewer permanent controls and flat repeated rows. Keep amounts aligned for scanning. Tighten section gaps rather than reducing readable type or touch targets. Use one prominent action per screen. Avoid counts restated as prose, duplicate summaries, decorative empty cards and always-visible errors. Expand optional information when needed instead of displaying all features at once.

## Delivery sequence

1. Portal/account structure, host-owned toolbars and predictable Back behavior. Move Feed under More and provide four compact tabs.
2. Compact Today Home and the checklist-first Shopping flow.
3. Expense-account support, preserving existing household expenses, recurring schedules and repayments in Everyday. Accounts and participants belong to the expenses feature. Account isolation and financial invariants must be enforced in the backend, not only by UI filters.
4. Tricount-style expense/history, balance/repayment and unequal-split flows. Add weighted-share calculation using deterministic minor-unit rounding.
5. Density/accessibility review, light/dark screenshots, Android/iOS navigation validation and the final architecture/package audit.

The account change is domain/backend work as well as presentation work. Before implementation, inspect current policies, transaction functions and edit capabilities; add a new migration and backward-compatible API behavior as required. Do not silently rewrite prior money history, combine accounts' debts or duplicate recurring occurrences.

## Acceptance checks

- More → Feed/Reminders → Back returns More. More root → system Back returns Home. Home → Add expense → Save/Close returns Home.
- Shopping/Balances root → Android Back returns Home; root tabs have no toolbar Back arrow.
- Expense detail → split editor → Back returns through the same account; drafts survive resume and account-page visits.
- Global account works with zero households. Back/Close returns to the exact previous flow. Household chooser cancellation keeps selection.
- Shopping supports rapid keyboard entry, check/uncheck, collapsed completion, failure/retry and multiple-list selection.
- Existing financial records remain visible in Everyday. Trip expenses, participant balances, repayments and scheduled charges do not leak between accounts.
- Shares, exact amounts and percentages sum to the original integer-minor-unit total with deterministic rounding. Concurrent recurring execution does not create duplicate expenses.
- Fresh screenshots cover empty/populated states, long/localized text, large fonts and narrow viewports. Run relevant domain/database/Compose/platform checks and `tools/audit_project.py` before implementation commits.

## References

- Plannot: `PortalHost.kt`, `PortalToolbar.kt` and `ai/PATTERNS_NAVIGATION.md` in the reference repository. Reuse interaction and ownership patterns while retaining Loom's single-feature-module packaging.
- [Tricount expense and balance flows](https://help.tricount.com/articles/how-can-i-manage-my-tricounts-and-expenses).
- [Tricount custom splits](https://www.tricount.com/features/custom-split-expenses).
- [Google Keep lists](https://support.google.com/keep/answer/6395451?co=GENIE.Platform%3DAndroid).
