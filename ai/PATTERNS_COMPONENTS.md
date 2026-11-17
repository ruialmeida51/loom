# Components

Adapted from Plannot `ai/PATTERNS_COMPONENTS.md`; [design language](../docs/design-language.md).

| Need | Component / ownership |
| --- | --- |
| Primary action | core LoomButton, automatic loading disable |
| Destructive action | core DestructiveButton / DestructiveIconButton, or DestructiveConfirmation |
| Data refresh container | core container/PullToRefreshContentContainer |
| Transient success | core snackbar/SuccessSnackbar; lifetime in VM |
| Actionable Undo | existing Shopping host snackbar |
| Content skeleton | core LoadingLine/LoadingRow/LoadingRows/LoadingForm; feed owns its preview skeleton |
| Refresh preserving focus/geometry | core RefreshSkeletonContent; Home scopes it per section |
| Illustrated empty state | core IllustratedEmptyState, feature-owned copy/glyph |
| Shared section layout | core GroupedSurface, NavigationRow, SectionTitle |
| Selector | core SelectorButton, label-adjacent caret |
| Forms / sheets | core FormBottomSheet; feature owns pure content, Host owns controller |
| Optional task date | tasks TaskDueDate + TaskDateUi (feature-owned) |
| Checklist / bulk selection | shopping row/action components (feature-owned) |

A reusable component has no ViewModel, UseCase or route. Transient picker drafts belong to the
component state holder; the committed value/error belongs to the owning feature VM. Core remains generic.
