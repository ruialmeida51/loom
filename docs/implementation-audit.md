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
