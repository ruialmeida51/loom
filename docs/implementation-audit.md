# Implementation audit — 2026-11-13

The shopping and recurring-expense delivery includes the requested feature/package, demo, localization, and Gradle audit.

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

## Production and preview separation

Production `LoomApp`, authentication, domain models, screens, and Common DataSources contain no demo flags or demo branches. The desktop preview and shared UI test explicitly start `LoomDemoApp`, which injects its own implementations of feature domain ports. Preview changes remain in its in-memory stores. Production sources never select or import that graph.

## Localization and project references

- Eleven module-owned catalogs contain 315 string/plural resources, including labels, validation text, counts and accessibility descriptions.
- Android application labels and notification fallbacks use native resources.
- Presentation maps stable failure codes into resources; domain models never reference resource classes.
- Category and recurrence identifiers are localized for display. User content, currencies, URLs, filenames and protocol identifiers remain data.
- The supplied catalogs are default English. Further locales use Compose resource qualifiers; CSV header translations preserve their column counts.
- Gradle module dependencies use named `projects.…` accessors throughout.

## Validation

- 22 domain tests passed across household, shopping, expenses, reminders and insights.
- The shared Compose UI test passed item editing/undo, batch entry, multiple lists and their lifecycle, household switching, monthly recurring creation and pause/resume/cancel, navigation and themes.
- Android debug build and common Kotlin metadata compilation passed.
- Ten migrations passed on disposable PostgreSQL with authorization, stale-update, transaction, schedule, ledger and statement checks.
- Four RSS tests and strict Edge Function checks passed.
- `python3 tools/audit_project.py` passes and runs in CI.

The database migration is deployed to Loom with RLS enabled on shopping lists. No production test fixtures were created. The existing Auth leaked-password-protection project setting remains documented in [backend setup](backend.md); [Supabase remediation](https://supabase.com/docs/guides/auth/password-security#password-strength-and-leaked-password-protection).

Offline functionality remains deferred. Real device push delivery requires Firebase/APNs credentials.
