# Household polish, consent and diagnostics

Baseline: main 97f7d5b / Loom 4.0.9 (59). Offline support remains deferred.

| ID | Accepted work | Status |
| --- | --- | --- |
| NAV-01 | Polish More with icon-led grouped rows (user clarified More tab only) | Implemented; light/dark visual check passed |
| MONEY-01 | Expense card overflow: Edit and confirmed Delete | Implemented; validation passes locally |
| MONEY-02 | Repayment/reimbursement cards with Edit and confirmed Delete; authorized transactional version checks | Implemented; validation passes locally |
| STABILITY-01 | Audit resume crash paths; add opt-in Firebase Crashlytics, platform setup and release mapping | Implemented; validation passes locally |
| CONSENT-01 | Plannot-inspired startup usage-data sheet with independent analytics/crash choices and persistent decisions | Implemented; validation passes locally |
| NOTIFY-01 | Contextual notification priming on task creation/view and Settings Allow; OS prompt only after explicit enable | Implemented; validation passes locally |
| SHOP-01 | Continuous swipe grouping motion without snap/reset flicker; preserve row identity and scrolling | Implemented; validation passes locally |
| HOME-01 | Matching card-shaped skeletons and compact spacing on initial/manual refresh | Implemented; validation passes locally |
| AUDIT-01 | Feature/package ownership, typed errors, resources, central annotated DI, isolated demo ports and final reviewer audit | Implemented; validation passes locally |
| DELIVERY-01 | Appropriate unit/UI/native/SQL checks, deploy additive migration, PR and Firebase release | Migration applied; platform CI and Firebase delivery pending |

References: Plannot UsageDataConsentBottomSheet / ShouldAskUsageDataConsentUseCase and
NotificationsPrimeBottomSheet, inspected from the existing reference checkout. Optional analytics and
crash-report switches are independent; prior analytics consent must not automatically grant diagnostics.
Notification consent is separate and contextual, never an automatic startup permission request.

Next-feature suggestion: a compact household activity log after the existing flows are stable. This is
advice, not additional implementation scope without a user request.

Resume investigation: FileKit Android compression decodes the full image before resizing and retains
several bitmaps. Bounded sampling before decode and explicit recycling replace this path; this is
a plausible source of memory pressure after returning from camera/gallery, not a confirmed crash
stack. Crashlytics will provide evidence for any remaining resume failures.

iOS Firebase client registered through the existing service account; matching client configuration fetched.
Both platforms default analytics/crash collection off; native Kotlin crash frames omit exception messages.

Local validation: 223 JVM tests (51 UI), Android app/test APKs, SQL/Edge Function/tooling and architecture audit pass.
Native Android, iOS/Xcode and Firebase delivery receipts will be attached to the PR.
