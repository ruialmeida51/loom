# Loom screen and interaction language

Loom helps two or more people run a home. The current feature set is enough; this pass makes everyday tasks easier to find and finish. The existing warm copy, gold accent and light/dark palettes stay.

## Information hierarchy

- The toolbar identifies the destination and household; a back arrow means return within that flow. It owns the top and horizontal safe-area insets. The outer scaffold consumes its padding once.
- A page may have one short, friendly introduction beneath the toolbar. Do not repeat the destination name as an eyebrow, another large title and a paragraph.
- Use an open, divider-separated list for repeated items. A surface groups related settings or navigation rows; a card is for a distinct summary, never an empty wrapper.
- A section heading sits outside its group. Show a few useful previews on Home and provide a clear route to the whole feature.
- Empty sections explain what happens next. Loading placeholders occupy the space of the content being loaded. An error appears where the failed action happened and keeps the input available for retry.
- Use 20dp screen gutters, 16dp internal padding, 20dp container corners, 12–16dp between related elements and 20–24dp between sections. Text and controls can grow; avoid fixed heights for copy.

## Navigation and editing

- Bottom tabs are peer destinations. Switching tabs preserves each stack and does not play a push/pop slide.
- A forward destination enters from the trailing edge. Returning reverses it, with the departing destination above the returning one. Both destinations have opaque backgrounds. Use a short tween, not a bouncing spring.
- Authentication, household selection, creation and joining remain separate destinations with separate ViewModels. Initialization and retrying a session refresh preserve identity and the current draft. Only an actual unauthenticated session leaves the flow.
- Short contextual tasks open a bottom sheet: shopping item/list edits, reminder creation, feed subscription, repayments and nickname editing. Forms share a scrollable field area and a fixed action footer. Each destination owns one sheet controller; switching a form replaces it rather than stacking windows.
- The expense sheet puts description and amount first, then payer and split controls. Equal splitting is the default; custom share fields appear only for an explicitly chosen split method. Categories and recurrence are secondary details.
- Keep dialogs for brief confirmations such as cancelling a recurring expense. Disable competing dismissal/actions while a save is in flight. Sheet insets belong to the shared layout, not individual form content.

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
