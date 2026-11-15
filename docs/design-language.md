# Loom screen and interaction language

Loom helps two or more people run a home. The current feature set is enough; this pass makes everyday tasks easier to find and finish. The existing warm copy, gold accent and light/dark palettes stay.

## Information hierarchy

- Root toolbars show the household selector and global profile avatar. A child Host replaces that toolbar with Back, title and contextual actions. A back arrow returns within the flow. The owning scaffold applies and consumes safe-area padding once.
- A page may have one short, friendly introduction beneath the toolbar. Do not repeat the destination name as an eyebrow, another large title and a paragraph.
- Use an open, divider-separated list for repeated items. A surface groups related settings or navigation rows; a card is for a distinct summary, never an empty wrapper.
- A section heading sits outside its group. Show a few useful previews on Home and provide a clear route to the whole feature.
- Empty sections explain what happens next. Loading placeholders occupy the space of the content being loaded. An error appears where the failed action happened and keeps the input available for retry.
- Use 16dp screen gutters on working lists, 16dp internal grouped-surface padding and 20dp container corners. Repeated rows use compact spacing while retaining touch targets; separate sections by 16–24dp. Text and controls can grow; avoid fixed heights for copy.

## Navigation and editing

- Home, Shopping, Balances and More are peer tabs. Switching tabs preserves each stack and does not play a push/pop slide. Android Back from Shopping/Balances/More root returns Home; only Home root exits. More opens the full Reminders, Feed and household details destinations.
- A forward destination enters from the trailing edge. Returning reverses it, with the departing destination above the returning one. Both destinations have opaque backgrounds. Use a short tween, not a bouncing spring.
- Authentication, household selection, creation and joining remain separate destinations with separate ViewModels. Initialization and retrying a session refresh preserve identity and the current draft. Only an actual unauthenticated session leaves the flow.
- Short contextual tasks open a bottom sheet: shopping item/list edits, reminder creation, feed subscription, repayments and nickname editing. Forms share a scrollable field area and a fixed action footer. Each destination owns one sheet controller; switching a form replaces it rather than stacking windows.
- Expense entry is a dedicated screen: description and amount first, then payer and a split summary. A separate split destination supports equal shares, weighted shares, exact amounts and percentages. Date, category and monthly recurrence sit under More details. Account creation and expense details also have distinct destinations.
- Keep dialogs for brief confirmations such as cancelling a recurring expense. Disable competing dismissal/actions while a save is in flight. Sheet insets belong to the shared layout, not individual form content.

Home starts with two square quick actions for adding an expense or reminder, followed by Today and coming-up reminders. All reminders opens its own full page; Home remains a preview. Quick actions use the compact icon/card pattern from Plannot’s My account screen.

Shopping opens on the selected checklist with one inline item field. Enter submits the item and keeps the keyboard available for the next one. Completed items remain collapsed below. List selection and optional editing use sheets; errors stay beside the failed input.

Balances shows the selected Everyday/trip/event account above expense history and the Balances view. The latter leads with who pays whom and prefilled repayment actions. Accounts never combine currencies or debts. Recurring bills and statements sit behind contextual actions, keeping routine expense entry clear.

## References and what we adopt

These are flow references, not copied branding, assets or new feature commitments:

- [Plannot component patterns](https://github.com/Plannot/plannot-app/blob/main/ai/PATTERNS_COMPONENTS.md), visual layouts and navigation: host-owned sheet controllers, pure feature content, grouped rows, resource-backed copy and feature-owned ViewModels.
- [Bring: items and lists](https://www.getbring.com/help-center-main-categories/items-lists): the active list is the main working surface, item entry is immediate, and less frequent item operations stay contextual.
- [Splitwise: splitting an expense](https://kb.splitwise.com/balances-and-expenses/what-are-different-ways-i-can-split-an-expense): description and amount first; equal split as the default; other split methods are explicit choices rather than a wall of controls.
- [Todoist: Quick Add](https://www.todoist.com/help/todoist/features/use-task-quick-add-in-todoist-va4Lhpzz): start with the task itself and keep optional details subordinate.
- [Material: bottom sheets in Compose](https://developer.android.com/develop/ui/compose/components/bottom-sheets): a single modal surface with coordinated state, dismissal, scrolling and keyboard clearance.

## Ownership and review

Feature-specific forms, rows, prepared display values and errors stay in their owning feature. Core only owns generic layout, input, motion and sheet infrastructure used by unrelated features. Entry-point hosts connect ViewModels, typed sheet requests and navigation. Preview ports remain under `app/demo`.

Before release, review light and dark captures, a narrow viewport, larger text, empty/loading/error states, keyboard-visible save actions, back navigation and background/resume with a partially completed form. Shared Compose tests cover what can be exercised on the build host; Android system bar/IME and iOS gestures also need on-device confirmation.
