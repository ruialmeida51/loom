# Household polish, optional diagnostics and notification consent

Reviewed against main `97f7d5b` with the final-diff procedure in
[the reviewer checklist](../../ai/agents/AGENTS_REVIEWER.agent.md). Scope is tracked in
[the accepted-work plan](../plans/2026-11-18-household-polish-and-consent.md).

More now uses compact icon-led navigation rows. Expenses and repayments share compact card geometry
and anchored Edit/Delete menus. Payment editing preserves its identity, date, creator and version;
confirmed deletion freezes the original record. Domain validation and typed server errors retain
failed forms/confirmation targets. Local success feedback is followed by authoritative ledger reads.

The additive migration `20261118112001_repayment_editing_and_deletion` is applied to Supabase.
Mutations require current membership and the original sender/recipient/creator or household owner,
lock the account/record, validate participants and use version checks. Tombstones prevent resurrection;
read policies and both settlement branches of the account ledger exclude them. Disposable database
checks cover authorization, stale edits/deletes, lost-response retries and balanced ledger updates.
Hosted verification reads metadata only. Security advisories are unchanged: the private receipt-cleanup
queue intentionally has no client policies; leaked-password protection remains a separate existing
Auth setting ([Supabase guidance](https://supabase.com/docs/guides/auth/password-security#password-strength-and-leaked-password-protection)).

Diagnostics owns its domain port and native Crashlytics adapters. Settings owns the combined startup
consent policy, local decision, private drafts, Screens and ViewModels. Analytics keeps its own event
vocabulary. All providers remain in annotated central DI; demo graphs supply separate diagnostics
ports. Hosts own typed sheet registrations and distinct ViewModel stores. No raw provider failures
or transport models cross into presentation. Strings remain in owning resource catalogs.

Plannot's independent optional switches and equal-emphasis Allow all/Save choices are reused.
Collection defaults off in Android/iOS metadata. Dismissal preserves prior analytics consent and
never grants new crash consent. Readiness/decision restore atomically; saved answers do not prompt
again on resume. Settings opens the same choices. Withdrawal disables collection and deletes unsent
reports; opting in discards reports cached while reporting was off. Loom sets no Crashlytics user ID
or household/custom-content keys. Apple reports include Kotlin frames without exception messages.
Privacy pages and data-safety notes describe SDK diagnostics and independent choices.

Notification explanations are contextual to dated notification tasks or My account's enable control.
Only explicit Allow calls the native requester. Not now persists, denied permission links to system
settings, and only a return from that explicit settings action can register automatically. Android's
request callback is owned by its activity, so an old activity cannot detach its replacement's bridge.

Shopping keeps one draw offset through drag, acknowledgement and rollback; committed indentation
changes once. Cancellation/short swipes preserve the relationship. RTL uses logical start/end.
Home uses contained three-item skeletons, retaining section headers and refresh bounds. Existing
initial/manual minimum delays remain; background reads stay silent and writes use action feedback.

Receipt decoding now samples before allocating Android pixels and recycles decoded/oriented bitmaps.
This reduces a plausible source of memory pressure after camera/gallery return. No production crash
stack was available, so the intermittent resume crash's full cause is not claimed as resolved.
Crashlytics reports need a consenting installation and an actual crash before delivery/symbolication
can be confirmed. CI compiles iOS; physical iOS permissions and lifecycle still need Apple hardware.

Validation before platform CI: 223 JVM checks, including 51 shared UI flows; Android debug and test
APKs; all 20 migrations and database integration tests; 9 Edge Function tests; 7 Python and 31 Node
checks; resource/package/DI audit and whitespace checks. Screenshots verify light/dark, compact cards,
large-text consent and vertically stacked actions. Native Android and Xcode results and Firebase
version/distribution receipts are attached to the PR after CI completes.

Final reviewer result at source submission: **pass**, subject to the explicit platform verification
above. No unresolved findings in the implemented scope; no new product features were added beyond
this request. A household activity log remains a suggestion in the plan, not implementation scope.
