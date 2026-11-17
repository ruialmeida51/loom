# Compact interactions and feed browsing

Baseline: Loom 4.0.5 (55), main `7a11c2c`. User authorized implementation on 17 November 2026.
This ledger preserves the whole request and records validation separately from implementation.

## Accepted work

| ID | Request | Implementation direction | Status |
| --- | --- | --- | --- |
| HOME-01 | Home glyphs take too much space | Smaller quick-action icons/tiles and compact illustrated empty cards; retain touch targets | Verified locally; final CI pending |
| FEED-01 | Filter one or several feeds with pills | Multi-select subscription chips, All feeds reset, retain unread filter; filter by subscription ID | Verified locally; final CI pending |
| FEED-02 | Too much feed information at once | Bounded server pages, stable date/ID cursor, explicit Load more, independent page retry; Home preview stays short | Verified locally; final CI pending |
| TASK-01 | Add task should be a FAB like Add expense | Shared labelled add FAB and matching bottom content clearance | Verified locally; final CI pending |
| MONEY-01 | Recording who pays whom is jarring | Compact named sender/receiver selection rows with clear direction and personal labels | Verified locally; final CI pending |
| MONEY-02 | Owes red, gets back green; more personal | Accessible semantic status colours with text cues, You labels and personal payment actions | Verified locally; final CI pending |
| MONEY-03 | Expense edit/delete buttons are too large | Toolbar overflow menu; preserve ownership/busy rules and destructive confirmation | Verified locally; final CI pending |
| MONEY-04 | Delete expense should be full width | Full-width destructive confirmation action; no duplicate large details-page buttons | Verified locally; final CI pending |
| MONEY-05 | More space after Expenses/Balances tabs | Clear content gap before the first row/card without enlarging each row | Verified locally; final CI pending |
| TYPE-01 | Change typography and audit sizes/hierarchy | Bundle Plannot's licensed Inter/Space Grotesk fonts; define one restrained Material type scale and remove local size overrides | Verified locally; final CI pending |
| TYPE-02 | Split sheet too big, hard to scan | Compact total/method header, smaller consistent participant type and spacing; preserve +/- and direct entry | Verified locally; final CI pending |
| TASK-02 | Task form has tiny footer actions and huge date button | Stacked full-width primary/secondary buttons and compact optional date control | Verified locally; final CI pending |
| TASK-03 | Space between date and trash; mismatching white trash border | Explicit gap, consistent outline role in both themes, retain 48 dp hit areas | Verified locally; final CI pending |
| ACCOUNT-01 | Use compact icon/colour menu rows like Privacy & Legal | Reuse NavigationRow/GroupedSurface for settings and account actions | Verified locally; final CI pending |
| ACCOUNT-02 | Separate red Delete account row | Separate destructive navigation row preserving the existing deletion-information destination | Verified locally; final CI pending |
| ACCOUNT-03 | Separate red Sign out row | Separate destructive action row; sign-out remains only in My account | Verified locally; final CI pending |
| AUDIT-01 | Consistent components, typography, feature/package ownership | Final Plannot-derived source review after fixes and validation; resources, typed errors, DI and demo isolation remain required | Verified locally; final CI pending |

## Defaults used

- Start News with 20 articles and load another 20 on demand. Selected source pills combine with OR;
  Unread combines with that selection. Empty selection means All feeds. Failed/pending sources still
  appear, even with no articles. Reset paging when the effective filter changes; reject stale requests.
- Sorting is newest first with ID as a deterministic tie-breaker. Pagination must bound network reads,
  not merely hide an already-loaded full history. Preserve loaded rows on load-more failure.
- Use the existing named-user and household boundaries. No hosted personal/financial records are
  queried for validation; use mock transport, demo UI and disposable PostgreSQL fixtures.
- Colours supplement “owes/gets back/paid by/received by” text; they never carry meaning alone.
- Task date/repeat/notification semantics and confirmation/ownership rules stay intact.
- Offline synchronization remains deferred. Preserve the 300 ms initial/manual skeleton transition
  and silent resume/realtime updates. Do not use skeletons for mutations or loading the next page.

## Reused references

Plannot source: `ThemeTypography.kt` (Inter/Space Grotesk), `OptionCard.kt`, `MyAccountScreen.kt`.
Font OFL notices are copied with the bundled font assets. Loom's Privacy & Legal grouped rows are
explicitly the user's preferred reference. Existing research remains in
`docs/research/usability-and-design-system.md` and the earlier polish ledger.

## Validation and delivery

Shared compilation, 43 Compose flow checks and 19 feed checks pass. Disposable PostgreSQL passes
18 migrations and its integration suites, including equal-date cursors, source/unread filtering,
publisher labels and outsider denial. The full 189-test JVM suite, both Android APK builds and final reviewer audit pass.
Two independent CI runs also pass shared/database/RSS checks and all six native Android flows.
The final selection-accessibility patch passes the relevant existing UI regressions; its final
CI and Firebase delivery are pending. See the [audit](../audits/2026-11-17-compact-interactions.md). A compiled preview is not native-device verification.

Applied `20261117174412_feed_reading_pagination` to the existing project; metadata confirms an
invoker view, authenticated SELECT and anonymous denial. Security advisors have no new findings;
the pre-existing leaked-password-protection configuration warning remains outside this UX pass.

The latest row-pattern request is applied to Appearance/notification preferences, separate account
actions, named payment selectors, shopping-list selection, active/managed spending accounts and
feed management. Form text fields remain normal inputs. Existing household and More navigation
already use grouped rows; no redundant screen mode or navigation abstraction was added.

Reader policy: stable pages until explicit refresh or return from Manage feeds; Home stays live.
This prevents background publisher insertions from moving the user's current reading position.
