# Architecture and Plannot patterns

Loom retains Plannot’s separation of responsibilities and replaces its many inner Gradle modules with Kotlin packages. Shared Android/iOS logic and UI stay in `commonMain`; native storage, notification permissions/tokens, and CSV sharing stay in platform source sets.

## Feature packages

```text
domain/
  model/          Immutable domain facts and policies
  datasource/     Ports; no transport or UI types
  errors/         Feature-owned AppError families
  usecase/        Business operations and orchestration
data/
  network/
    datasource/   Common implementations and I/O operations
    model/
      request/    One serializable request per file
      response/   One serializable response per file
    mapper/       Pure transport mappings and server-error normalization
presentation/
  mapper/         Typed errors/domain values to UI resources
  viewmodel/
    contracts/    Observable state and event contracts
  screen/         Screen + pure InnerScreen
  components/     Feature-owned rows, forms and bottom-sheet content
```

Core and the composition root stay small shared modules. There are no Repository classes and no separate composables modules. Feature UI does not access Supabase or instantiate DataSources.

## Domain ownership and dependencies

Sharing a model does not change which domain owns it. A feature imports another feature's domain and declares a direct Gradle dependency when it needs that model. Feature dependencies are allowed; circular dependencies are not.

| Owner | Responsibility |
| --- | --- |
| `household/domain/model` | Household, member, selected household context, and supported household currency policy |
| `account/domain/model` | Global profile and nickname, independent of household membership |
| `expenses/domain/model` | Spending accounts, participants, integer money, split methods, repayments, and ledger rules |
| `authentication/data/local` | Secure authentication session serialization over core's native storage |
| `settings` | Local preferences, combined usage-data consent, contextual notification prompts and push-device registration |
| `insights/data/export` | Native monthly-statement CSV sharing |
| `analytics` | Typed consent state and event vocabulary, local consent storage, and native Firebase Analytics adapters |
| `diagnostics` | Optional crash-report consent state, native Firebase Crashlytics adapters and Kotlin/Native fatal frames |
| `legal` | Public document destinations, release policy configuration, and localized legal-link components |
| `entrypoint/app/demo/data` | Preview-only household fixtures and implementations of feature domain ports |
| `entrypoint/navigation/push` | App-specific push destinations and pending navigation |
| `di` | Central annotated providers and application/household graph assembly; no domain models or feature logic |
| `core` | Generic results, clock/ID utilities, Supabase transport, native storage, Compose components, theme rendering, flow helpers, and navigation machinery |

For example, expenses depends on household for its context and currency policy; insights depends on expenses for money and ledger models. Authentication coordinates device registration through settings' `PushDeviceDataSource` domain port. `LoomDemoApp` is a dedicated preview entry point with its own graph. Production authentication, models, screens, and Common DataSources contain no demo mode or fixtures. Core has no dependency on any feature and provides no aliases for feature models.

## State and navigation

- Form values belong to a private `InnerInputState`; they are mirrored into public immutable screen state.
- Plannot’s `observeWith`, `updateState`, launch strategies, and ViewModel launch helper are adapted under Loom’s package name.
- `Screen` collects state with lifecycle awareness. `InnerScreen` receives values and callbacks.
- Hosts own saved back stacks, navigation callbacks, and ViewModel lifetimes. Typed serializable destination models live in `entrypoint/app/navigation/model`.
- Plannot’s Navigator, NavigationState, NavDisplay wrappers, and transitions are adapted in core. A modifier is applied once so nested Hosts do not duplicate outer Scaffold padding.
- A household gets its own isolated Koin graph. Switching households disposes the graph and its navigation entries. Annotated Koin providers in the dedicated `shared/di` module bind ViewModels to their contracts; entrypoint only opens/closes those graphs.

All provider declarations live in `shared/di`, organized by application/account/household-setup/household graph and datasource/usecase/viewmodel responsibility. `@Single` factories return domain ports without redundant `binds`; `@KoinViewModel(binds = [Contract::class])` factories return ViewModel contracts. Feature modules have no DI registration or annotation-processor configuration. Koin 4.2's compiler plugin processes the central providers on Kotlin 2.4 without KSP. Production graphs construct production adapters there; the preview supplies its port implementations to the same central household bindings without a demo dependency in production DI.

`PortalHost` retains the selected household graph while My account or the household chooser is open. Its user graph provides profile, preferences and notification registration even before the first household exists. A Host-owned ViewModel store scopes the account ViewModel to that authenticated user. The nickname editor belongs to account; membership and invitations belong to household.

`HouseholdHost` owns five peer tabs: Home, Shopping, Money, Tasks and More. Each has a feature Host that owns its toolbar and child stack. Tab roots render a compact portal toolbar with household selector and profile avatar; child destinations replace it with Back/title/actions. Home opens task creation in place and its expense shortcut within its own stack. TasksHost owns the full task list; MoreHost is a navigation hub for Feed and household details; it introduces no shared feature models or domain module. Balances owns account management, expense details/editing, recurrence and insights. `ExpenseFormHost` owns split and date sheets. Each split opening gets a scoped ViewModel store; dismissing it destroys only the unconfirmed split draft. Core owns only generic toolbar, sheet and navigation infrastructure.

Android Back pops a child first, then returns Shopping/Money/Tasks/More roots to Home. Only Home root leaves the app. Tab transitions fade; child transitions slide in the appropriate forward/back direction. Navigation decorators retain per-tab stores, and the household graph stays alive through account/chooser visits. Safe-area padding is applied and consumed once by the owning scaffold.

## Data and money

DataSource operations return `AppResult.Failure(error: AppError)` at the I/O boundary and preserve coroutine cancellation. Feature-owned sealed error families live in `domain/errors`; `core/domain/errors` contains generic failures such as connection, session, and access errors. Only data adapters inspect SQLSTATE, Supabase SDK error codes, or deployed server messages. They retain the cause for diagnostics and return a typed error before it leaves data. Domain and presentation never parse failure text.

Network request/response classes each have their own file in `data/network/model/request` or `response`. Every wire property declares `@SerialName`. Pure `DomainToRequest` and `ResponseToDomain` mappings live in `data/network/mapper`, separate from DTOs and Common DataSources. Sources receive Supabase access through the composition root; transport tests inject a client using Ktor's MockEngine.

Read streams use `stream()` or a descriptive variant when a port offers several streams, consumed by `Get…StreamUseCase`. Fallible streams expose `Flow<AppResult<T>>`, so realtime errors follow the same typed boundary as writes. Cached session/settings streams expose their current domain state. `GetExpenseLedgerStreamUseCase` owns ledger aggregation; insights builds monthly statements in its own stream use case.

 Realtime events invalidate household queries; periodic refresh repairs missed events. Household's data package supplies keyset pagination so the Data API's row cap does not truncate balances.

Money is stored in integer minor units. Equal splits distribute remainder cents in a stable member-ID order; percentages use the largest-remainder method. Database transactions validate active members, share totals, authorship, and idempotency IDs. A positive balance means the member is owed money; recording a repayment decreases that credit and the sender’s debt.

Recurring bills retain their day-of-month anchor when February clamps a due date. Statement snapshots are content-addressed and revisioned; a later correction produces a new version instead of changing an issued record. Spending excludes repayments.

## Security

Every exposed app table uses RLS. Access is checked against current household membership, rather than editable metadata or stale JWT role claims. Privileged implementations live in an unexposed schema with fixed search paths and explicit caller checks; the public RPC wrappers run with caller privileges. Financial tables reject direct authenticated writes.

Sessions use Android Keystore encryption and iOS Keychain. Authentication exposes explicit Restoring, Authenticated and SignedOut domain states; a nullable identity never doubles as initialization state. Supabase Kotlin 3.9.0 owns stored-session loading, refresh rotation, retry and mobile lifecycle refresh. Recoverable refresh failures retain an established identity; a cold-start refresh failure stays on the neutral reconnecting surface until the SDK resolves it. Push registration runs independently of the session collector. The desktop preview keeps its session in memory. No server key, signing key, Firebase configuration, or APNs private key belongs in source control.

The private worker tables deny client access. The worker secret is generated into Supabase Vault; only its hash is stored for verification. Push work uses leases, retries, event IDs, current membership checks, and token cleanup. RSS network requests allow only configured trusted publishers, revalidate every redirect, bound response sizes, and reject DTD/entity declarations.

## Demo, resources, and build references

`LoomApp` composes production sources. The desktop preview and UI tests explicitly use `LoomDemoApp` under `entrypoint/app/demo`; feature screens are shared through the same ViewModel contracts and use cases. Demo sources implement domain ports inside the preview package. No demo flag is stored in authentication or household domain models.

Features own their string/plural catalogs and presentation message mappings. Domain failures use typed errors. Non-Composable presentation mappers produce `TextResource`; ViewModels store those resources in error, info, and undo state, and Compose resolves them for the current locale. App routes and generic components own their respective catalogs. Build dependencies use Gradle-generated references such as `projects.shared.features.household`, with `TYPESAFE_PROJECT_ACCESSORS` enabled.

Run `python3 tools/audit_project.py` to check package paths, feature dependencies, domain imports, demo isolation, typed error boundaries, transport-model files/annotations, stream naming, explicit imports, UI resource use, and named project references.

## Screen and sheet ownership

The [screen language](design-language.md) defines hierarchy, grouping, empty states and motion. Entry-point sheet hosts own per-destination controllers and connect existing ViewModel contracts to pure feature form components. Core supplies the generic sheet layout, scrolling/action footer, grouped rows and transitions. Feature forms never create modal windows themselves.

Account owns global profiles and nickname editing. Updating a nickname writes only the caller's `profiles.display_name` through self-update RLS. Household consumes the account domain port to refresh membership display names without resetting drafts. Profile transport and resources belong to account; member/invitation transport and resources belong to household. Settings remains responsible for local preferences and device registration.

Expense accounts, participants and split metadata belong to expenses. Each household's Everyday account uses the household ID so existing clients and records keep their original scope. New trip/event accounts have independent currencies, expenses, recurring schedules, repayments and statement revisions. Server transactions enforce current membership, account participation, permissions, integer allocations, optimistic versions and retry idempotency. Weighted splits use deterministic largest-remainder rounding; the server verifies both the allocations and their declared split method. Soft deletion changes live ledgers without rewriting saved statements.

Selected shopping lists and spending accounts are stored by both user and household. Profile/preferences stay global. Pending financial notifications carry an account ID, and Hosts select that account before opening its destination. Analytics remains opt-in and accepts only typed screen/action names, never household, profile or transaction content.


### Resume and publisher management (17 November 2026)

Data reads wait for SDK initialization/recovery and a token with sufficient lifetime rather than forcing
parallel refreshes from Home sections. PostgREST and Functions require a session. Read streams opt into
transport-only retries; real authorization failures remain terminal and writes never retry automatically.
First/manual load timing belongs in presentation. Refresh covers the existing composition, keeping its
focus, drafts and geometry alive; Home covers each section independently.

Feed owns subscription identity/status, URL requests, data adapters and management UseCases. Article
reading and subscription management have separate Screen/contract/VM/routes; the Host owns sheets and
Back. Subscription rows exist without articles. A transactional address replacement uses a new identity,
checks the expected old address and cascades old articles, preventing an in-flight old worker from
publishing under the new source. All SQL mutation checks remain server-side and household scoped.

Full reading uses feature-owned FeedQueryDomain/FeedCursorDomain/FeedPageDomain and a separate
GetFeedPageUseCase. Source IDs and unread status filter server-side before a 20-row page; the
published-at/ID cursor has matching indexes and avoids OFFSET. The data adapter parses timestamps
to Instant and reads one extra row to determine whether another page exists. Request generations
discard stale filter/page results; append errors retain existing rows and cursor. The reader is a
stable snapshot refreshed explicitly or on return from management, and Home retains a bounded live
three-article stream. Both reads use the existing authenticated session/recovery gate. A
security-invoker subscription view obtains publisher titles under existing household RLS; no
privileged function, new writes or customer data duplication is introduced.

The current account position is the financial overview. Monthly statement navigation is retired; old
saved routes/push links redirect to balances. Historical statement storage/export code remains in its
own insights feature for compatibility, and existing records are not rewritten.

## Tasks and reminder compatibility

`features/tasks` owns typed task/recurrence models, ports, transport mappers and presentation. Home
depends on that feature for its preview. Domain dates are typed `Instant?`; raw ISO and recurrence
strings are parsed only in data. The VM owns confirmed optional schedule inputs; picker drafts stay
in the component. Task creation stays on Home or Tasks and shows mutation feedback in the owning Host.

The canonical `public.tasks` table retains household RLS, scope guards and realtime publication. A
security-invoker `public.reminders` compatibility view exposes only dated notification-enabled tasks
to old clients and scheduled workers. Existing reminders keep their notification intent. New app
writes explicitly choose notification intent. Completion locks the task and checks the expected
due date (including null) to prevent advancing a newer occurrence from a stale request.

## Shopping groups and receipts

Shopping owns parent relationships, family mutation planning and stable tree order in its domain.
The ViewModel freezes affected versions, presents one optimistic family change and adopts the atomic
RPC result for rollback/Undo. Completed-item removal also adopts promoted-child versions. Gesture
feedback is transient presentation state, with equivalent menu and accessibility actions.

Expenses owns receipt metadata, immutable photo bytes, typed media errors and focused receipt
UseCases. Its data adapter re-encodes bounded JPEGs and uses private authenticated Supabase Storage
with unique, non-upsert paths. DTOs and mappers remain separate. Central annotated DI supplies both
production and isolated demo ports. The dedicated viewer owns a route; the image component owns
only transient zoom/pan. See the backend guide for RLS, version checks and Storage cleanup.
