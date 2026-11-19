# Household management and tab consistency audit

Base: main `8a1a647`, following Loom 4.0.11 (61). Scope and all ten confirmed product decisions are
recorded in the [plan](../plans/2026-11-18-household-management-and-tab-consistency.md).

## Final source review

Reviewed the intended diff after fixes and local validation using `ai/agents/AGENTS_REVIEWER.agent.md`.
Source review: **pass**. Native Android execution, iOS/Xcode and hosted migration application passed.
Final history-alignment CI and Firebase delivery results are tracked on the delivery PR.

- **Household ownership:** member/role/lifecycle/invitation models, ports, typed errors, resources and
  use cases remain in Household. Member identities retain active/version facts. Domain membership
  policy prepares owner/leave permissions; composables receive prepared rows and callbacks.
- **Financial ownership:** schedule forms, allocations, positions and their display state stay in
  Expenses. Entrypoint composes Expenses' balance preview into a Household confirmation. No reverse
  Household → Expenses dependency, financial model in core or new repository/layer module was added.
- **Data boundaries:** separate request/response files, explicit wire fields and separate mappers.
  Only adapters inspect SDK/SQL failures. Clients call authenticated invoker RPCs; private definers
  check current database membership/roles with fixed search paths and explicit grants.
- **DI/demo:** all registrations remain central annotated Koin providers. New preview adapters live
  in entrypoint/app/demo; production sources contain no sample-data conditionals. Build references
  remain named and the dependency graph is acyclic.
- **State:** schedule drafts are private inputs mirrored through observeWith. Dates/splits retain
  their transient sheet lifecycle. Confirmation targets, versions, request IDs, name input and typed
  errors survive failed writes. Busy guards prevent duplicate submission/dismissal. Subscription
  generations and pending-write filters prevent older reads replacing completed mutations.
- **Session:** explicit exits and authoritative membership loss clear the remembered household under
  the existing selection mutex; login remains. Network/read failures retain context, and an old
  household event cannot clear a newer selection. Household stream construction does not require a
  synchronously available SDK user during recovery.
- **UI:** shared root gutters and 48 dp static/selector heading geometry; one account manager with an
  archived section; compact recurring overflow; vertically stacked full-width confirmation actions,
  concise labels and anchored menus. All new copy/accessibility text is resource-backed. The active
  invitation lives on Household details rather than auto-opening a modal whenever it refreshes.

## Backend and regression findings resolved

- The old scheduled worker deactivated bills when the payer, creator or a participant became inactive.
  It now retains their payer/split and future charges, as explicitly requested. Existing rules may
  retain former participants on edits/resume; newly assigned participants must be current members.
- Schedule tombstones preserve expense foreign keys and occurrence uniqueness. Active, paused and old
  cancelled rules can be deleted without removing recorded expenses. Edits preserve paused state,
  intended split metadata and previous ledger entries; resume retains no-backfill behavior.
- Historical names, repayments and account/expense corrections remain usable after departure without
  granting former members access or allowing unrelated inactive identities into new assignments.
- New six-character invitations are reusable for 24 hours. Redemption remains hashed/rate-limited;
  redisplay uses Supabase Vault and is owner-only. Operation receipts contain invitation IDs, never
  raw codes. Legacy hash-only codes retain their original lifetime/consumption rules.
- Household-first locks serialize membership changes and redemption. Last-owner protection survives
  concurrent exits. Departure revokes shared codes so they cannot immediately undo removal. Current
  roles authorize writes and disable already-open privileged confirmations when ownership is lost.
- Household deletion removes its explicit scoped dependency graph, including all account currencies,
  while retaining other households/global profiles. Existing receipt triggers queue durable object
  cleanup before financial rows disappear. Lost successful responses are safely retryable.
- The preview chooser now opens a remaining household after deleting its prior root; this fix remains
  confined to the dedicated demo navigation.

## Local verification

- 255 JVM tests passed, including 57 Compose/navigation flows, schedule draft/failure cases,
  owner changes, financial previews, typed-name deletion and session access-loss/recovery.
- Android debug application and instrumented-test APKs built successfully.
- 22 migrations and all disposable PostgreSQL suites passed. A two-session test holds both owner
  exits on the same household lock, then verifies exactly one succeeds and one owner remains.
- SQL covers reusable/legacy invitations, frozen targets, access denials, paused edits, old cancelled
  deletion, retained multi-currency positions, future generation for former members, repayments,
  financial corrections, idempotent deletion and durable receipt cleanup.
- Release tooling: 7 Python and 32 Node tests passed; legal pages built and checked. Release notes
  remain within Play's 500-character limit. The existing allocator/distribution workflow is unchanged.
- `python3 tools/audit_project.py` passes package/layer/DI/resource checks. Comparable [screenshots](../screenshots/2026-11-18-household-management/README.md)
  were inspected in their rendered format.
- Actual [Compose compiler reports](evidence/2026-11-18-household-management-compose.json) show all
  restartable functions skippable in core, Household, Expenses, Authentication and entrypoint.
  Reports were enabled through a temporary init script, without a compiler behavior change.

## Verification limits and delivery gates

The disposable database uses a documented Vault API double to exercise lifecycle/access/cleanup;
it does not prove Supabase's encryption implementation. Hosted metadata confirms real Vault APIs
exist and neither authenticated nor anonymous clients can SELECT decrypted secrets. Production
verification must remain metadata/advisory reads, not tests against household records.

Desktop screenshots/skippability do not prove native touch, IME, background/resume or frame timing.
All 12 Android emulator tests passed in [PR CI](https://github.com/ruialmeida51/loom/actions/runs/44430999776),
including the new paused-bill scenario covering keyboard clearance, resume, Save and system Back.
The macOS/Xcode gate passed too, and native device captures were reviewed.

Both migrations applied to hosted Supabase on 19 November 2026. The recorded versions are
`20261119013329_recurring_bill_editing_and_deletion` and
`20261119014955_household_lifecycle_and_reusable_invitations`; repository filenames match those
versions with unchanged SQL bodies. Hosted checks inspect metadata and grants only. Public RPCs
remain invokers, anonymous execution is denied, private management receipts deny client access,
and anonymous/authenticated clients cannot read decrypted Vault secrets.

Final history-alignment CI, merge and the normal versioned Firebase upload remain tracked on
[PR #21](https://github.com/ruialmeida51/loom/pull/21). Firebase delivery does not create a Play release
or GitHub tag.

Baseline hosted security advisories included disabled leaked-password protection and the intentionally
policy-free private receipt cleanup queue. These are recorded baseline settings, not regressions
introduced by this source change. The post-migration advisory adds the expected policy-free private
management receipt table, whose client grants are revoked deliberately. The existing
[leaked-password protection setting](https://supabase.com/docs/guides/auth/password-security#password-strength-and-leaked-password-protection)
remains unchanged.
