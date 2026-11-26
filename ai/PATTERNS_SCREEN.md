# Screens

Adapted from Plannot `ai/PATTERNS_SCREEN.md`; see [boundaries](PATTERNS_BOUNDARIES.md).
Reference style: authentication/presentation/screen/LoginScreen.kt.

Screen collects platformCollectAsStateOnStart, wires VM events and Handle… effects. Inner…Screen
renders individual values and callbacks. Hosts inject shared VMs, handle navigation and register sheets.

Use PullToRefreshContentContainer on fetched destinations with a real onRefresh and isRefreshing.
The Host consumes safe/toolbar/bottom-navigation insets; the nested container adds zero system insets.
The scrollable child owns content padding and IME clearance. Forms retain explicit reference-data retry.

Render exactly the relevant loading/error/empty/loaded state. A load failure retains prior content;
initial-load failure must not become an empty success. Explicit refresh shows a short matching skeleton transition while retaining loaded data and drafts underneath.
Background reloads after writes do not remove content. Actions show button/row progress, never skeletons.

Transient success uses SuccessSnackbar in the container's snackbarHost; the VM owns its lifetime.
Existing actionable Undo snackbars retain their action and lifecycle. Persistent errors remain dismissible/retryable.

Use full-screen details and create/edit forms for expenses and payments. Keep focused date/split
pickers and small settings forms in sheets, and dialogs for brief destructive confirmations. Delete is a filled or
outlined button, including confirmations, with progress and sibling disabling. List selectors dismiss on
selection; no redundant Done when there is no draft to commit.

## Pagination

Adapted from Plannot's pagination pattern, with keyset cursors instead of page numbers.

- Domain: a page is `CursorPageDomain<T, C>` (core `domain/pagination`); `nextCursor == null` is the
  end, including an exactly-full last page. A feature may keep its own page type only when it
  builds pages itself (the Money ledger merges expenses and repayments).
- State: the VM exposes `PaginationUi(hasMore, loadingMore, loadMoreError, enabled)`. `enabled` is
  false while the list as a whole loads, fails or shows another view.
- Screen: `PaginatedLazyColumn` with the `LazyListState` created in the Screen. The destination
  lays out its own rows; the component appends the loading row or the failed page's message with
  Try again, and calls `onLoadMore` near the end through `HandleOnLoadMore`, which keys its dedupe
  on the row count so a fling that stays at the end keeps paging.
- ViewModel: guard duplicate reads with the in-flight `Job` (or a flag set synchronously before
  the request). A failed page sets `loadMoreError` and paging stops until `onRetryLoadMore`; it
  never replaces the loaded rows with a full-screen error. Every first-page read cancels the
  in-flight page so an old page never lands on a new list.
