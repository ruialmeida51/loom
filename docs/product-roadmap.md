# Product direction and roadmap

Loom helps roommates and couples manage everyday life together: what needs buying, who paid, what is due, and whose turn it is. Both audiences use the same household model. Household preferences, saved splits, and task assignments adapt the experience to the people living together.

This document records the agreed direction. Planned capabilities below are not yet implemented unless identified as existing. The README describes the current app.

## Product principles

- Make frequent actions quick: add groceries, correct an entry, record a bill, complete a reminder.
- Give household members a clear shared view of purchases, responsibilities, expenses, and changes.
- Support both equal contributions and arrangements such as a couple's 60/40 split. Let people choose their arrangement.
- Keep the active household visible. Lists, expenses, bills, tasks, and household notes belong to that household.
- Make actions across features work together, with the user confirming amounts, participants, and payment details.
- Keep feature-owned models and policies in their domains, following the existing architecture.

## Existing foundation

The app already supports multiple households, creating and joining households, invitations, household switching, multiple named shared shopping lists with editing, undo, favorites, and reuse, reminders with daily/weekly repeats, expense splitting and repayments, monthly recurring expense schedules with pause/resume/cancel controls, monthly statements/statistics/CSV, RSS subscriptions, and light/dark/system themes.

The live app currently requires a connection. Sample-household changes are held in memory. Real push delivery still requires Firebase/APNs configuration as described in [backend setup](backend.md).

## Delivered: everyday shopping workflows

1. Edit item names, quantities, and categories; undo accidental removals and changes.
2. Add multiple named shopping lists within each household. New households start with a Groceries list; members can add lists such as Pharmacy, DIY, or Weekend barbecue.
3. Rename and archive lists, move items between lists, and keep purchased items available for easy reuse.
4. Add frequent/recent items and quick entry of several items, reducing repeated typing.
5. Keep the household and selected list visible, with straightforward saving feedback, failures, and retry actions.
6. Add a quick household switcher with distinct icons/colors and restore the last selected household when it remains accessible.

The hierarchy is household → shopping list → item. Store tags or filters can follow when useful; a new store does not require a new household. Named lists are shared with the household initially.

Monthly recurring-expense management was delivered alongside shopping: payment dates determine the monthly anchor, schedules can be paused/resumed/cancelled, and existing ledger entries remain intact. Production/demo separation, resource-backed UI text, named Gradle references, and an automated ownership audit are also in place.

## Delivered: focused navigation and spending accounts

Home, Shopping, Balances and More are the root tabs. Home has square Add expense/Add reminder shortcuts and today’s reminders; More opens the full Reminders, Feed and household details pages. Shopping starts with inline checklist entry and collapsed completed items. Plannot-style portal navigation keeps global My account outside households and preserves the underlying flow.

Each household has an Everyday account plus independent trip/event accounts for household members. Accounts use one currency each. Dedicated expense, detail and split screens support equal shares, weighted shares, exact amounts and percentages. Expenses can be edited or deleted with version checks; saved statement revisions remain intact. Repayment suggestions, recurring bills, reports and CSV exports use the selected account.

## Following delivery: complete money and bill management

- Add repayment corrections and a visible expense/repayment change history. Keep balances consistent and preserve already-issued statement revisions.
- Add transaction search, receipt attachments, and saved split presets such as equal shares or a couple's 60/40 arrangement.
- Manage recurring bills: review upcoming entries, change schedules, pause/cancel, and support additional recurrence intervals.
- Distinguish an expected bill from a recorded payment. Variable bills need an amount confirmed before recording what was paid.
- Show expected monthly spending and each member's contribution alongside actual recorded expenses.
- Connect a completed shopping trip to an expense: enter the receipt total, confirm the payer and participants, then record the split and include it in reporting.

These capabilities extend the current monthly recurring expense schedules with pause/resume/cancel controls; they do not imply automatic bank payments.

## Following delivery: coordinate shared responsibilities

- Assign reminders to a person, support snoozing, and add monthly/custom recurrence.
- Offer optional chore rotation and completion history so responsibilities are visible.
- Make Home actionable with today's tasks, upcoming bills, shopping shortcuts, and the current user's balance.
- Add a household activity timeline for relevant changes.
- Provide notification preferences by activity, quiet hours, and digest options.
- Complete household management with invite links/QR codes, pending invitation controls, member management, leaving, and ownership transfer.

Member departures must preserve historical expenses and outstanding balances. Switching or leaving a household must not mix its information with another household.

## Further improvements

- Widgets and shortcuts for groceries and reminders.
- Household notes for important contacts, instructions, and announcements.
- RSS subscription management, folders, search, and saved articles.
- Meal planning that can add ingredients to a shopping list.
- Deeper spending forecasts and category budgets, guided by regular household use.

Receipt scanning/OCR, pantry inventory, banking integrations, and full chat remain later candidates. Receipt attachments precede automated scanning.

## Deferred: offline functionality

Offline use is explicitly deferred. Do not include local-first storage, queued offline edits, or conflict resolution in the current delivery scope. Connected flows should still explain connection failures and provide retry actions.

Revisit offline access and editing as a separate future initiative.

## Architecture boundaries

Shopping lists and items belong to shopping; household selection and membership belong to household; global profiles belong to account; spending accounts, money, splits, and bill policies belong to expenses; reminders and chore policies belong to reminders; RSS remains in feed. Cross-feature use cases may depend on other feature domains. Core stays limited to generic infrastructure and utilities.

Backend changes must use new migrations, preserving the existing migration history and financial records.
