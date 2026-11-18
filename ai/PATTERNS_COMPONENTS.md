# Components

Adapted from Plannot `ai/PATTERNS_COMPONENTS.md`; [design language](../docs/design-language.md).

| Need | Component / ownership |
| --- | --- |
| Primary action | core LoomButton, automatic loading disable |
| Secondary action | core LoomSecondaryButton, matching shape and touch height |
| Destructive action | core DestructiveButton / DestructiveIconButton, or DestructiveConfirmation |
| Data refresh container | core container/PullToRefreshContentContainer |
| Transient success | core snackbar/SuccessSnackbar; lifetime in VM |
| Actionable Undo | existing Shopping host snackbar |
| Content skeleton | core LoadingLine/LoadingRow/LoadingRows/LoadingForm; feed owns its preview skeleton |
| Refresh preserving focus/geometry | core RefreshSkeletonContent; Home scopes it per section |
| Illustrated empty state | core IllustratedEmptyState, feature-owned copy/glyph |
| Shared section layout | core GroupedSurface, NavigationRow, SectionTitle; Privacy & Legal is the preferred row reference |
| Read-only metadata / identity | core InfoRow / PersonAvatar; feature owns labels, roles and display models |
| Primary list creation | core ActionFab in the refresh container's Scaffold slot; matching FloatingActionContentPadding |
| Contextual list actions | core ActionMenu + rememberActionMenuState when a related row opens it; feature owns permissions, confirmation and feedback |
| Selector | core SelectorButton, label-adjacent caret |
| Forms / sheets | core FormBottomSheet; feature owns pure content, Host owns controller |
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

FormBottomSheet uses a fixed equal-width horizontal footer: outlined Cancel and primary Save. Fields
scroll separately. Forms and draft selections say Save; read-only views say Close. Immediate-selection
managers omit Cancel/Done and use an explicit creation action where needed. Localized labels may wrap.
Buttons share the theme's medium shape and at least a 48dp touch target.

NavigationRow supports emphasisColor and trailingContent for an anchored caret/popup or progress.
SelectorButton's menu slot anchors its popup to the caret. Transient expansion stays in the component;
committed selection and errors stay in the feature VM. MemberPickerRow remains household-owned because
it understands members and the current user. PersonAvatar is decorative beside an accessible name;
features choose semantic emphasis. InfoRow is read-only and must not imply a navigation action.
