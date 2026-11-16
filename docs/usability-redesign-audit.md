# Usability redesign implementation audit

16 November 2026. Reviewed against `origin/main` at `a2fa148`, following the
[research brief](plans/redesign-research-brief.md) and [baseline research](research/usability-and-design-system.md).

## Implemented behavior

- New expense capture accepts amount/description immediately. Loading/retrying references preserves
  early typing. Account/member/record reads start independently; financial preflight reads also run
  concurrently. Editing still waits for the actual record and failed saves preserve the draft.
- A grouped payer/split summary replaces the repeated participant breakdown. The summary includes
  participant count. The split sheet uses one method selector, compact participant rows, full-size
  +/- targets and direct input, including decimal percentages. Cancel/Back retain the parent draft.
- Read streams publish promptly. First missing content uses skeletons; manual refresh retains
  loaded content, composer identity, selection and scroll. Background reads stay silent. The updated
  engineering/design guides replace the previous forced-wait/refresh-skeleton policy consistently.
- A 30-second in-memory snapshot can seed expense-reference reads. Keys include current user and
  household scope; snapshots expire and account mutations invalidate them. Ordinary account/member
  reads, explicit retry and financial preflight remain fresh. Existing transactional RPC validation
  remains authoritative; no schema change or migration is required for this pass.
- Reminders, shopping and news have independent Home subscriptions, loading, refresh and typed-error
  states. One failing section cannot hide another. Initial failures keep their retry action; errors
  dismiss independently when prior content exists. The household date refreshes with reminder reads.
- Home uses compact action tiles with square icon areas, grouped reminders, a three-item shopping
  preview and manually scrolled news cards. Home and More use the same full Feed Host and return to
  their own parent. BBC World/NASA suggestions open an editable URL; subscription needs confirmation.
- Material typography, shared spacing, semantic surfaces/shapes and a complete light/dark color
  scheme replace the partial palette. Colors adapt original Lumbridge, including fixed color roles.
  Expense history uses open rows, readable metadata and localized calendar headings. Feed dates use
  the household zone; calendar resources are generic core infrastructure reused across features.

## Final package and pattern review

**Pass for the implemented scope.** No new blocking package/boundary findings remain.

- Household models/identities remain in household; expense accounts/splits remain in expenses.
  Home owns its reminder-preview aggregate and prepared state. Feed owns catalog/source/article
  models, adapters and presentation. Core additions are generic snapshot, date and layout utilities.
- New data models, ports, implementations and mappers have matching separate files/packages.
  Domain imports no data/presentation. Feature dependencies are explicit, named and acyclic.
- All new DI registrations live in `shared/di`. Catalog providers are in its shared graph module so
  production and isolated previews resolve the same required contracts without a live backend.
- Screens collect state and pass individual values/callbacks. Hosts retain navigation/sheet ownership;
  components only keep transient picker visibility. No conditional demo path or production fixture
  import was added. DTOs, RLS/RPC policy and release automation are unchanged.
- Errors remain typed through domain/presentation and map to feature resources. UI copy, plurals,
  accessibility labels and calendar names use resources. Publisher names/addresses are catalog data.
- Private input reducers remain pure; coroutine cancellation is preserved. Removed duplicate member
  collection, obsolete form projections, old Home aggregation and unused palette constants.

## Verification

- **144 shared JVM tests pass**, including **34 Compose UI tests** and a navigation-serialization test. New coverage checks independent
  reference reads, early typing, fresh retry, failed-save draft retention, snapshot expiry/scope,
  Home loading isolation, editable suggestions and Feed Back/scroll behavior.
- Android debug and instrumentation APKs build. Shared screenshots cover light/dark, narrow layouts,
  larger text, forms, split methods, errors, household/account flows and retained existing features.
- `python3 tools/audit_project.py` and diff/relative-document-link checks pass.
- Native CI runs the updated API 35 keyboard/composer geometry, refresh, system Back and resume
  scenarios, plus iOS simulator compilation/Xcode build. The PR checks are authoritative for those
  results; desktop captures do not establish native system-bar/IME or iOS gesture behavior.
- No production records were used for verification. Suggested source URLs both returned HTTP 200
  with valid RSS during validation; availability can change independently of Loom.

## Current captures

- [Home light](screenshots/home-light.png) and [dark](screenshots/home-dark.png).
- [Expense capture](screenshots/expense-form-light.png), [split sheet](screenshots/split-shares-light.png)
  and [360px split with 1.4× text](screenshots/split-shares-narrow.png).
- [Expense history](screenshots/money-light.png) and [news preview](screenshots/home-news-light.png).

## Verification limits and follow-up

Actual device timing/frame traces and live API timings have not been measured. No numeric speed or
frame-rate claim follows from source review or virtual-time tests. iOS Back/swipe/IME behavior still
needs hands-on verification after its build succeeds. Full offline synchronization remains deferred.

The news preview is the first concrete version of the proposed secondary entry point. Regional
starter catalogs and a hide-news preference can follow usage feedback. Sources are optional and do
not silently become household subscriptions.

Existing same-day ledger ordering still uses a stable record key after the calendar date. A later
change should carry server creation times through the expense/repayment read models if within-day
chronology is needed; that is separate from the row-layout and capture changes in this pass.
