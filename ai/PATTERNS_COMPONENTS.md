# Components

Adapted from Plannot `ai/PATTERNS_COMPONENTS.md`; [design language](../docs/design-language.md).

| Need | Component / ownership |
| --- | --- |
| Primary action | core LoomButton, automatic loading disable |
| Secondary action | core LoomSecondaryButton, matching shape and touch height |
| Destructive action | core DestructiveButton / DestructiveIconButton, or DestructiveConfirmation |
| Data refresh container | core container/PullToRefreshContentContainer |
| Transient success | core snackbar/SuccessSnackbar; lifetime in VM |
| Actionable Undo | core snackbar/SuccessSnackbar optional action; bounded lifetime and target in the feature VM |
| Paged list | core pagination/PaginatedLazyColumn + PaginationUi; never a hand-rolled LazyColumn end check or "Load more" button |
| Row divider | core LoomDivider (outlineVariant); never a hand-coloured HorizontalDivider |
| Failed read with retry | core RetryNotice (ErrorNotice + secondary retry, default "Try again") |
| Saving indicator in a row | core ActionProgress in place of the row's control; never for content loading |
| Card surface | core LoomCard (shape, contentPadding, spacing); pass PaddingValues() for edge-to-edge pictures |
| Content skeleton | core LoadingLine/LoadingRow/LoadingRows/LoadingForm; feed owns its preview skeleton |
| Refresh preserving focus/geometry | core RefreshSkeletonContent; Home scopes it per section |
| Illustrated empty state | core IllustratedEmptyState, feature-owned copy/glyph |
| Root content layout | core layout/RootHeading and rootContentPadding align static titles/selectors and shared gutters |
| Shared section layout | core GroupedSurface, NavigationRow, SectionTitle; Privacy & Legal is the preferred row reference |
| Read-only metadata / identity | core InfoRow / PersonAvatar; feature owns labels, roles and display models |
| Primary list creation | core ActionFab in the refresh container's Scaffold slot; matching FloatingActionContentPadding |
| Contextual list actions | core ActionMenu + rememberActionMenuState when a related row opens it; feature owns permissions, confirmation and feedback |
| Selector | core SelectorButton, label-adjacent caret |
| Forms / sheets | core FormBottomSheet; omit actions for immediate-selection content, feature owns pure content, Host owns controller |
| Brief dialog footer | core DialogActions, equally weighted Cancel and Save/confirm |
| Optional task date | tasks TaskDueDate + TaskDateUi (feature-owned) |
| Checklist / bulk selection | shopping row/action components (feature-owned) |

A reusable component has no ViewModel, UseCase or route. Transient picker drafts belong to the
component state holder; the committed value/error belongs to the owning feature VM. Core remains generic.

Settings, account actions, source management and list/account selection use compact icon-led rows.
Group related rows with quiet dividers; show selected state or a concise status. A destructive row
uses an explicit destructive title/icon and error colour. Overflow menus hold secondary actions,
whose destructive confirmation uses the shared filled destructive control beside outlined Cancel.
Confirm sign out and destructive shared actions; disable dismissal/duplicate writes while busy. Keep
typed failure feedback in context for retry. Routine completion and reversible archive/restore stay direct.

FormBottomSheet uses a fixed vertical footer: full-width primary Save above full-width outlined
Cancel, with shared small spacing. Fields scroll separately. Forms and draft selections say Save; read-only views say Close. Immediate-selection
managers omit Cancel/Done and use an explicit creation action where needed. Localized labels may wrap.
Labelled actions share Plannot's pill shape and at least a 48dp touch target through DefaultLoomButton.
The public primary/secondary/destructive variants delegate their appearance to that one renderer.
Loading preserves the label's measured layout instead of inserting a spinner beside it. Brief date,
time and confirmation dialogs use equally weighted horizontal DialogActions; form-sheet footers
remain vertically stacked. Immediate row-action sheets may omit the footer entirely.

NavigationRow supports emphasisColor and trailingContent for an anchored caret/popup or progress.
SelectorButton's menu slot anchors its popup to the caret. Transient expansion stays in the component;
committed selection and errors stay in the feature VM. MemberPickerRow remains household-owned because
it understands members and the current user. PersonAvatar is decorative beside an accessible name;
features choose semantic emphasis. InfoRow is read-only and must not imply a navigation action.

Keep contextual action labels concise: use Delete when the dialog already names the target; put
names, scope and consequences in the title/body. Use short verbs such as Revoke and Discard in
context, and distinguish destructive Cancel from dismissal with Keep. Accessibility labels on
standalone icons still identify the target.
