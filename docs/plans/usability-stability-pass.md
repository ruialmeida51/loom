# Loom usability and stability pass

Status: implementation complete; local validation passed. CI device/iOS checks and hosted migration remain before distribution. Reported symptoms are recorded below; underlying causes remain subject to the code and device audit. Current base is Loom 4.0.1. The shared app, 119 JVM tests and native Android test APK compile successfully; final release builds, hosted migration and CI device checks are still pending. The existing release-version/changelog automation remains in use.

## Complete working checklist

- [x] AUTH-01: Remove the signed-out startup flash. Represent session restoration explicitly and show a neutral startup/loading state until authentication is resolved.
- [x] AUTH-02: Audit encrypted session persistence, access-token expiry, refresh-token rotation, SDK automatic refresh, foreground/resume behavior, network failures and actual sign-out. Verify behavior rather than assuming the SDK configuration is correct.
- [x] NAV-01: Make system Back, predictive Back/gestures and toolbar Back return to the same destination with the same motion direction. Check nested flows and peer-tab roots.
- [x] EXPENSE-01: Replace the separate split destination with an editor inside the expense flow. Preserve the expense draft, and distinguish confirming split edits from cancelling them.
- [x] SHOP-01: Add shopping-list deletion with explicit handling of contained items, shared permissions and the last remaining list.
- [x] MONEY-01: Add balance-account deletion with defined ownership and treatment of expenses, repayments, recurring rules and saved history.
- [x] MONEY-02: Move archived accounts out of normal account selection into an intentional management surface with restore/delete actions.
- [x] TOOLBAR-01: Use one selector component with its arrow immediately next to the visible name across household, shopping-list and balance-account selectors. Handle long labels consistently.
- [x] TOOLBAR-02: Give household selection and global profile equal-width clickable header areas while retaining clear, accessible targets and appropriate content alignment.
- [x] SHOP-02: Stop groceries jumping/reordering after additions or realtime refreshes. Define insertion order, stable identities/keys and preservation of scroll, checked state and current input.
- [x] STATE-01: Audit immutable/stable UI collections and state, derived-list recomputation, stream emissions and ViewModel lifetimes. Remove visible flashes and unnecessary content replacement.
- [x] IME-01: Keep shopping item entry and its action reachable on a long list while the keyboard opens, stays open and closes. Use continuous Keep-style in-list entry.
- [ ] INSETS-01: Audit safe areas, status/navigation bars, IME padding, scrolling and inset consumption across screens and sheets; avoid missing or double-applied padding.
- [x] LOAD-01: Add consistent, layout-matched skeleton loading and correct initial/loading/content/empty/error states. Retain loaded content during refreshes where appropriate.
- [x] LOAD-02: Adapt Plannot's delayAtLeast helper and its usage for deliberate minimum loading durations, preserving cancellation and avoiding repeated flashes.
- [x] ACTION-01: Establish primary, secondary and destructive action hierarchy using buttons/icon buttons, with less reliance on text buttons.
- [x] COMPONENT-01: Reuse focused toolbar, selector, button, row, form/sheet and loading components. Preserve feature/domain ownership, typed errors, resources and dedicated demo code.
- [x] DESIGN-01: Research Plannot, Google Keep, Tricount and suitable reminder/list references. Document the chosen interaction patterns and apply them consistently within Loom's brand.
- [x] AUDIT-01: Record and address other confirmed usability/stability defects found during this pass. Keep added findings visible in this checklist.
- [ ] RELEASE-01: Automatically distribute Firebase releases to the explicitly configured internal-testers group. The default aliases file is edited locally; this change is not released yet.
- [x] ICON-01: Replace the single Android launcher PNG setup with proper adaptive, legacy density, round and themed-icon assets. Preserve the supplied logo and verify launcher masks/safe zones. Check Image Asset Studio availability and document the actual generation method.
- [ ] VERIFY-01: Add meaningful regression coverage for the reported flows, including mobile keyboard and Back behavior where the environment supports it. Record verification limits; run the final ownership/package audit and relevant platform/release checks before distributing the update.

## Confirmed decisions

1. Split editing uses a bottom sheet over the retained expense draft.
2. The balance-account creator or household owner may permanently delete the account and its records for everyone, with an explicit confirmation.
3. Any household member may delete a shopping list and its items. Deleting the final list leaves a Create list state.
4. Keep shopping items in their existing/insertion order and append new entries at the bottom.
5. Use Google Keep-style entry: Enter commits the current item, creates the next blank row and keeps focus/keyboard ready for continuous typing. The current entry must remain visible on long lists through correct IME insets and scrolling. This supersedes the pinned-composer proposal.

6. Keep Archive as an optional action under Manage accounts, with Reopen and Delete. Archived accounts stay out of the normal selector.

## Audit findings

- Startup conflated a still-restoring session with a confirmed sign-out. The SDK already persists/refreshes sessions; explicit domain restoration state is being added.
- Feed could show empty before the first read and had no read-retry action; recurring bills had no first-read skeleton. Both are corrected.
- Shopping used one global write flag, so checking a second item during another save silently dropped the tap. Updates are now scoped to each item, with optimistic feedback and independent rollback.
- Predictive Back selected its motion direction from the gesture edge, unlike toolbar Back. Both paths should use the same layout direction.
- Shopping refreshes use ID keyset pagination, while acknowledged additions append locally. A persisted insertion position is needed to prevent visible reordering.

No actual user records are to be deleted as part of implementation/testing; deletion capability is exercised against disposable/test data.

## Verification and deployment status

- 119 JVM tests pass, including continuous entry, idempotent retry, stale-snapshot protection, independent item rollback, final-list deletion and authentication state transitions.
- The native Android test APK compiles. New CI instrumentation checks exercise the actual IME, long-list Enter entry, system Back and background/resume. Device execution is pending; local emulator provisioning exceeded the workspace disk limit, and the failed installation caches were removed.
- Common Kotlin metadata, debug APK, minified release APK and release AAB build successfully. The final visual text adjustment is additionally covered by focused UI/native compilation checks.
- All 13 migrations and authorization/ledger tests pass in a disposable PostgreSQL container, including a pre-existing archived-list upgrade fixture. No production user records were queried for validation.
- 28 Node tests, 7 Python tests, workflow lint and the feature/package/resource audit pass. The audit covers 18 destination screens and their contracts and 34 separate transport models.
- Hosted schema metadata matches the migration's prerequisites. Supabase's migration confirmation expired twice; a follow-up metadata check confirms the new position column and deletion RPCs have **not** been applied. Do not distribute this client until that connector confirmation succeeds and the hosted schema/grants are verified.
- Supabase's preflight security advisor reported no table/view/RPC finding. Its existing [leaked-password-protection setting](https://supabase.com/docs/guides/auth/password-security#password-strength-and-leaked-password-protection) is disabled.
- Firebase's internal-testers default is configured in source. Upload/distribution and version allocation are pending the hosted migration and CI checks. No Play upload or GitHub release/tag is part of this Firebase update.

Android launcher source, generation method and review images are documented in [branding/android](../../branding/android/README.md). Plannot adaptations and external UX references are recorded in [the design language](../design-language.md).
