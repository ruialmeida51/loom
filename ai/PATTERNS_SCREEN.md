# Screens

Adapted from Plannot `ai/PATTERNS_SCREEN.md`; see [boundaries](PATTERNS_BOUNDARIES.md).
Reference style: authentication/presentation/screen/LoginScreen.kt.

Screen collects platformCollectAsStateOnStart, wires VM events and Handle… effects. Inner…Screen
renders individual values and callbacks. Hosts inject shared VMs, handle navigation and register sheets.

Use PullToRefreshContentContainer on fetched destinations with a real onRefresh and isRefreshing.
The Host consumes safe/toolbar/bottom-navigation insets; the nested container adds zero system insets.
The scrollable child owns content padding and IME clearance. Forms retain explicit reference-data retry.

Render exactly the relevant loading/error/empty/loaded state. A load failure retains prior content;
initial-load failure must not become an empty success. Explicit refresh substitutes matching skeletons.
Background realtime updates do not remove content. Actions show button/row progress, never skeletons.

Transient success uses SuccessSnackbar in the container's snackbarHost; the VM owns its lifetime.
Existing actionable Undo snackbars retain their action and lifecycle. Persistent errors remain dismissible/retryable.

Use sheets for forms/pickers and dialogs only for brief destructive confirmations. Delete is a filled or
outlined button, including confirmations, with progress and sibling disabling. List selectors dismiss on
selection; no redundant Done when there is no draft to commit.
