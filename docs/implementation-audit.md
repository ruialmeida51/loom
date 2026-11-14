# Implementation audit — 2026-11-13

The shopping and recurring-expense delivery and subsequent Plannot style refactor include feature/package ownership, typed errors, data-source readability, preview separation, localization, and Gradle checks.

## Ownership and architecture

| Responsibility | Owner |
| --- | --- |
| Shopping lists/items, reuse policies, versioned updates and shopping forms | `features/shopping` |
| Money, monthly recurrence policies, schedule permissions and controls | `features/expenses` |
| Household membership, selection restoration and identity badge | `features/household` |
| Aggregated household overview | `features/home` |
| Preview fixtures, demo port implementations and explicit preview entry point | `entrypoint/app/demo` |
| Generic results, transport/storage, theme/components, flow and navigation helpers | `core` |

Package declarations match their source paths. Domain imports contain no data, presentation, Supabase, or Compose resource types. Feature dependencies are declared directly and remain acyclic. There are no inner domain/data/presentation Gradle modules or Repository classes.

## Typed failures, readable sources, and naming

- `AppResult.Failure` carries `AppError`; feature error families remain in their feature's `domain/errors`. Raw SQLSTATE, SDK code, constraint-name, and legacy server-message handling stays in data adapters, with coroutine cancellation preserved.
- Native statement sharing follows Screen → ViewModel → ExportStatementUseCase → StatementExportDataSource. Its typed export failures become localized UI resources.
- All 27 transport models have matching files under `data/network/model/request` or `response`, with explicit `@SerialName` fields. Common DataSources use named requests and separate pure mappers.
- Ports use `stream()`/descriptive stream names and `Get…StreamUseCase`. Fallible streams return typed results. ViewModel actions and dependencies have descriptive names; reducers use named prior state and remain pure.
- State models are separate from contract APIs. Navigation keys live under app navigation models. Generic components/icons, theme renderers, state collection, storage, transport, and ViewModel infrastructure have focused core packages.
- Imports are explicit. Multi-dependency DI bindings and domain/DTO construction use named arguments.

## Production and preview separation

Production `LoomApp`, authentication, domain models, screens, and Common DataSources contain no demo flags or demo branches. The desktop preview and shared UI test explicitly start `LoomDemoApp`, which injects its own implementations of feature domain ports. Preview changes remain in its in-memory stores. Production sources never select or import that graph.

## Localization and project references

- Eleven module-owned catalogs contain 333 string/plural resources, including labels, validation text, counts and accessibility descriptions.
- Android application labels and notification fallbacks use native resources.
- Presentation maps typed feature and infrastructure errors to `TextResource`; domain models never reference resource classes or server error codes.
- Category and recurrence identifiers are localized for display. User content, currencies, URLs, filenames and protocol identifiers remain data.
- The supplied catalogs are default English. Further locales use Compose resource qualifiers; CSV header translations preserve their column counts.
- Gradle module dependencies use named `projects.…` accessors throughout.

## Validation

- 39 unit and transport tests pass across core, authentication, household, shopping, expenses, settings, reminders, insights and app navigation, including real SDK serialization/error handling through MockEngine, typed validation, cancellation propagation, export errors, and saved route compatibility.
- The shared Compose UI test passed item editing/undo, batch entry, multiple lists and their lifecycle, household switching, monthly recurring creation and pause/resume/cancel, navigation and themes.
- Android debug build and common Kotlin metadata compilation passed.
- Ten migrations passed on disposable PostgreSQL with authorization, stale-update, transaction, schedule, ledger and statement checks.
- Four RSS tests and strict Edge Function checks passed.
- `python3 tools/audit_project.py` passes and runs in CI.

The database migration is deployed to Loom with RLS enabled on shopping lists. No production test fixtures were created. The existing Auth leaked-password-protection project setting remains documented in [backend setup](backend.md); [Supabase remediation](https://supabase.com/docs/guides/auth/password-security#password-strength-and-leaked-password-protection).

Offline functionality remains deferred. Real device push delivery requires Firebase/APNs credentials.

## Release, legal and analytics audit — 2026-11-14

`features/analytics` owns consent state, typed event enums, its error family, use cases, local storage and platform adapters. Its Common DataSource lives in `data/local/datasource`; the Android Firebase SDK stays in `androidMain/data/platform`. Consent is shared across household graphs through the production composition root. A separate preview implementation stays in `entrypoint/app/demo/data`.

`features/legal` owns document destinations, configured public URLs, its data source and localized link components. Authentication owns Terms validation and its typed failure. Hosts open external documents. Core gained no feature models or dependencies.

The package audit passes for 309 Kotlin files, 27 separate network transport models and 13 catalogs containing 341 string/plural resources. Direct dependencies remain acyclic and use named Gradle project references. Production sources contain no preview branches; SDK failures remain typed above data, and analytics side effects occur outside state reducers.

All 50 JVM tests pass, including seven analytics-consent/schema checks, three registration checks and the shared Compose flow with the consent toggle. Six Python release/publication tests and four Node version-planning/authentication tests pass without live credentials. Common Kotlin metadata compilation and the minified Android release bundle pass. The merged Android manifest starts analytics disabled and contains no advertising/AdServices identifiers permissions.

Signed CI credentials, Play API permissions, public DNS/HTTPS, and the internal draft upload require external verification through their release workflows. The repository's iOS CI remains the macOS/Xcode check. Account deletion currently uses the documented support-assisted process, not an automatic endpoint. Firebase Analytics is integrated for Android only.

## Contextual toolbar audit — 2026-11-14

The app-specific toolbar lives under `entrypoint/app/navigation/components`; it receives household context and navigation callbacks from the Host. Only generic toolbar and menu primitives live in core. Menu rendering items are separate from the component and are not stored in ViewModel state. Back/menu labels and screen titles use resources; the back drawable is adapted from Plannot and mirrors for right-to-left layouts.

The single toolbar replaces the old branding and household rows. It shows the current destination and household, preserves quick household switching, and exposes back navigation only when the active stack can pop. Reminders and Settings use the overflow menu without offering a duplicate link to the current screen.

The package audit passes for 313 Kotlin files and 346 string/plural resources. The shared UI flow passes toolbar back navigation, root navigation, the overflow Settings action, switching households, consent changes and themes. Android debug and common Kotlin metadata builds pass. Updated screenshots are stored under `docs/screenshots`.

## Central annotated DI audit — 2026-11-14

All registration and graph assembly now live in the dedicated `shared/di` Gradle module. It contains application/household datasource, use-case and ViewModel provider modules, plus the shared analytics/legal providers. `@Single` factories return domain ports without redundant bindings. All ten `@KoinViewModel` factories bind and return their contracts. Feature classes retain their feature packages and contain no DI annotations or registration; unused Koin dependencies were removed from their build files.

Koin 4.2.2 and compiler plugin 1.2.3 process the central providers on Kotlin 2.4.21. The entrypoint loads the supplied graph definitions. Each household gets a fresh graph; the production authentication/household source and analytics consent adapter are passed through deliberately. Preview implementations remain in `entrypoint/app/demo/data` and enter through a central port-binding module without a production dependency on demo classes.

The audit passes for 322 Kotlin files, 27 separately packaged network models and 346 resources. It now rejects DI provider declarations outside `shared/di` and feature dependencies on the composition module. All 52 JVM tests pass, including annotated production ViewModel resolution with server-free stubs, household source isolation and shared consent, and the complete Compose preview flow. Android debug, common Kotlin metadata and the minified release bundle pass. iOS remains covered by the macOS/Xcode CI workflow.

The release and toolbar PRs passed Android/iOS CI and were merged. Public policy, Terms and deletion pages are live and verified at `https://loom.eyther.dev`. Signing credentials and Play API access are verified: [release run 43556121911](https://github.com/ruialmeida51/loom/actions/runs/43556121911) uploaded version `0.2.0` / code `48` as an internal-testing draft. Play rejected the explicit `changesNotSentForReview` override; the workflow now uses its default review handling. No public production rollout was performed.


## UI state, errors and feed validation audit — 2026-11-14

Screens receive filtered, grouped and ordered collections from feature presentation state. Balance direction and display amounts belong to the expenses presentation models, shared with Home through an explicit feature dependency. Recurring row actions derive from membership and schedule facts. Category progress belongs to insights presentation. Core gained no feature models.

`ErrorNotice` accepts a visible, non-null message. Screens omit the lazy-list item when no error exists, avoiding blank spacing. Load, action and form failures have separate lifetimes: a successful stream refresh clears its load failure without erasing an action or dialog error. Dialogs forward dismissal events; ViewModels protect in-flight writes.

Feed URL parsing uses Ktor in the feed data package before any client access or insert. It rejects incomplete HTTPS authorities, credentials, invalid ports/hosts, whitespace and excessive length, and normalizes the host while retaining paths and queries. The use case delegates without duplicating protocol parsing; failures remain typed `FeedError`. Server publisher/access policy remains authoritative.

Statement export captures the displayed statement in the ViewModel, then generates CSV and invokes the export use case. Screens supply localized labels. Money magnitude formatting avoids negating `Long.MIN_VALUE`. Focused KDocs explain these constraints and the error lifetimes.

All 60 JVM tests pass, including feed rejection before writes, refresh/error isolation, recurring membership changes, overflow-safe balance formatting, selected-statement export, and both shared Compose flows. The feed UI regression verifies a single dialog error and its removal after a successful follow. Android debug and common Kotlin metadata compilation pass. The package audit passes for 331 Kotlin files, 27 transport models and 346 resources, and now rejects collection preparation, CSV generation and domain permission calls in screens. CI and release verification include the feed tests.

## Authentication screens and email-code audit — 2026-11-14

Login, registration and email verification each own a Screen/InnerScreen, ViewModel contract, implementation and state under `features/authentication/presentation`. The old registration toggle is removed. Shared form layout and the Plannot-derived password visibility component stay in that feature. `AuthenticationHost` and serializable routes belong to entrypoint navigation; all providers remain in `shared/di`. Verification uses a typed injected Params object. Navigation never carries passwords or codes.

The hosted investigation found failed password sign-ins with `invalid_credentials` and successful `user_repeated_signup` events for already confirmed accounts. Repeating signup deliberately does not send another confirmation. Login now has an explicit email-code action for existing accounts, with `createUser = false` so registration and its terms acceptance are preserved. A typed challenge purpose controls resend: signup confirmation for registration, login code for sign-in. Both use email OTP verification, and the session stream owns entry to the authenticated app.

Each verification action has its own loading state. The ViewModel prevents concurrent requests, enforces a local resend cooldown and retains typed failures. Supabase validates email syntax and credentials; domain use cases reject missing input and enforce registration terms/password requirements. SDK codes for expiry, invalid email and delivery restrictions are translated only in data. Every label, accessibility description and message uses feature resources. Test fixtures live only in test sources.

All 70 JVM tests pass, including five Compose flows. Auth regressions cover registration redirects without passwords, unconfirmed and invalid sign-ins, resend failure, cooldown, preserving the challenge purpose, actual password rendering, existing-account email-code login, SDK request fields and typed failures. The annotated graph resolves all three auth contracts, including verification parameters. Android debug, common Kotlin metadata and the minified release bundle pass. The package audit passes for 352 Kotlin files, 27 transport models and 366 resources.

Repository Auth configuration enables confirmation and code-bearing signup/Magic Link templates. Hosted templates and SMTP settings still require dashboard configuration; connected tools cannot update those Auth settings. No real confirmation/sign-in email was sent during tests and inbox delivery is not yet verified. Exact template files, dashboard links and delivery checks are in [backend setup](backend.md#authentication-email-delivery).


## Google sign-in and destination separation audit — 2026-11-14

Authentication owns Google sign-in, callback parsing, typed results and the encrypted PKCE verifier. The common native bridge accepts only the configured scheme, host and path, queues cold-start callbacks, and exchanges a single authorization code through the SDK. Provider cancellation is a typed domain error; provider descriptions and access-token fragments never reach the presentation layer. Login owns its provider-loading state, while the session stream controls authenticated entry. Normal email registration, sign-in and verification remain available. A verification response without a usable session is a typed failure.

Android forwards both initial and subsequent intents. iOS registers the same callback scheme and forwards `onOpenURL`. A cross-file test checks both native registrations against the adapter’s callback and local Supabase configuration. The core Supabase factory accepts configuration from the composition root; feature-specific redirect values and encrypted auth stores remain in authentication, with their construction wired centrally in `shared/di`. The native callback allowlist was added to the hosted project; anonymous authorization reaches Google. Completing a real Google account login on a device remains a manual verification step.

The destination review found household selection, creation and joining bundled into one screen and ViewModel. These now have independent screens, contracts, state and implementations. Entry-point navigation owns their routes and the user-scoped shared selector; joining refreshes the selector and creation opens the returned household. Selector state is keyed by authenticated user and reloaded on entry. Creation/joining fields no longer live in selection state.

All 14 navigable feature screens have their own matching ViewModel and contract. No other page-mode switches were found. Existing filters and modal create/edit forms remain local to their owning destinations. The source audit now enforces matching screen/contract/implementation files and rejects known merged-workflow parameters, in addition to package ownership, typed failures, resource copy and DI boundaries.

The audit covers 377 Kotlin files, 27 transport models and 375 resources. Google SDK regressions check the production PKCE configuration, callback filtering, cancellation, the verifier used in token exchange and publication of the authenticated session. Email verification is tested with the same PKCE configuration and rejects success responses without a session. Household UI tests exercise independent create/join routes and the post-join refresh. All fixtures remain in test sources. Privacy copy now discloses Google-provided account/profile information without changing analytics consent.

Validation: all 80 JVM tests pass, including eight Compose flows. Android debug, common Kotlin metadata and the minified release bundle pass. Seven Python checks, four release-planning tests, generated policy-page validation and the complete source/package audit pass. The hosted Google provider and native callback are configured; final Google account consent on a real device and hosted email inbox delivery remain external verification steps.

## Household flows and release automation audit — 2026-11-14

The resume reset came from treating Supabase's background `Initializing` and refresh-retry states as sign-out. The authentication data mapper now retains identity through those transient states. Actual unauthenticated states still clear it. Lifecycle and back-motion UI regressions cover a partially completed creation form.

Toolbars own top/horizontal safe-area padding; scaffold content consumes its padding once. Navigation no longer adds an inset-discarding scaffold. Peer tabs preserve entry decorators and switch without a push animation. Forward/back transitions use opaque surfaces, reversed motion, explicit stacking and RTL/gesture-edge directions.

The screen language now uses concise introductions, grouped settings/navigation, open checklist/content rows, deliberate empty states and host-owned form sheets. Core contains only generic layout/sheet/motion infrastructure. Each sheet navigation type has a separate file under entrypoint navigation models, and each form belongs to its feature presentation package. No production feature selects demo content.

Household owns nickname validation, typed failures, profile transport and the update use case. Settings owns its editor. Existing profile RLS and column grants restrict writes to self and the display-name column; no migration was necessary. Member streams refresh names across settings, expenses and recurrence without resetting form inputs. All DI remains annotated and centralized, with named project references and acyclic feature dependencies.

The package audit covers 420 Kotlin files, 29 separate transport models, 14 destinations with matching ViewModels/contracts and 13 resource catalogs containing 390 string/plural resources. All 94 JVM tests pass, including 15 UI checks, SDK transport, lifecycle preservation, nickname persistence, short/narrow sheets with larger text and error-state isolation. Database authorization checks pass against all ten migrations in disposable PostgreSQL. Seven Python and sixteen Node release checks pass. Common Kotlin metadata, Android debug and minified APK/AAB packaging pass locally. PR CI also builds Android and the iOS framework/native host; device system-bar, IME and native gesture smoke checks remain useful.

Android release automation starts at 4.0.0 and advances a shared patch/build-code sequence across Firebase and Play. Successful deployment metadata preserves that sequence after Firebase build expiry. Main app changes default to Firebase; Play is an explicit internal-draft upload and creates a GitHub prerelease/tag only after upload success. A connection-check run does not upload, tag or consume a version. Tester emails are sent only to explicitly configured Firebase groups. Release tooling never prints private credentials and removes temporary signing files. No production rollout is automatic.
