# Payment flow consistency and layer audit

Baseline: main 6d1a800, Loom 4.0.10 (60), PR 19 merged and Firebase distribution successful.
The user requested another feature/package, layer-responsibility and Compose stability audit, then
asked to align payment editing with expense details. Full-screen record details and forms are the
selected pattern; sheets remain focused date, split and account pickers.

| ID | Accepted work | Status |
| --- | --- | --- |
| FLOW-01 | Payment card → full-screen details → toolbar Edit/Delete, matching expenses | Complete; see final audit |
| FLOW-02 | Full-screen record/edit payment form with a private draft; focused date picker sheet | Complete; see final audit |
| FLOW-03 | Remove payment form state/actions/sheets from the overview VM and navigation | Complete; see final audit |
| FLOW-04 | Refresh details after edits without losing content or showing a refresh skeleton | Complete; see final audit |
| LAYER-01 | Move startup/household restoration, persistence and sign-out out of composables | Complete; see final audit |
| LAYER-02 | Review feature ownership, typed errors, DTOs/mappers, central annotated DI and domain policies | Complete; see final audit |
| STABLE-01 | Check prepared immutable collections, stable keys, state lifetimes and compiler stability reports | Complete; see final audit |
| STATE-01 | Retain confirmation targets and typed errors on failed destructive actions | Complete; see final audit |
| QA-01 | Relevant domain, VM, UI, navigation, native and build checks; final reviewer audit | Shared/build checks pass; native/iOS CI gates follow |
| DELIVERY-01 | Changelog, PR and next Firebase release after checks | Pending |

Concrete findings so far:
- LauncherHost restores and persists household selection in UI effects/coroutines, and discards failures.
- ExpensesScreenViewModel mixes the ledger overview and payment draft lifecycle.
- Expense-details deletion clears its confirmation after a failed request, losing contextual retry.

Domain models stay in their owning features. Navigation, platform effects and resource resolution
remain presentation responsibilities; money rules, permissions, validation and data access do not
belong in composables. Offline support remains deferred.

Additional audit fixes: account-deletion failures keep their confirmation; account management and
repayment action permissions are prepared before composition; sign-out confirmation remains busy
until its request completes; obsolete payment-sheet state, files and events are removed. Late
receipt reads cannot overwrite a newly saved receipt. See the final audit for verification results.

[Final audit and verification](../audits/2026-11-18-payment-consistency-and-layers.md).
