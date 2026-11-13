# Architecture and Plannot patterns

Loom retains Plannot’s separation of responsibilities and replaces its many inner Gradle modules with Kotlin packages. Shared Android/iOS logic and UI stay in `commonMain`; native storage, notification permissions/tokens, and CSV sharing stay in platform source sets.

## Feature packages

```text
domain/
  model/          Immutable domain facts and policies
  datasource/     Ports; no transport or UI types
  usecase/        Business operations and orchestration
data/
  network/
    datasource/   Common implementations, private serialized DTOs and mapping
presentation/
  viewmodel/
    contracts/    Observable state and event contracts
  screen/         Screen + pure InnerScreen
```

Core and the composition root stay small shared modules. There are no Repository classes and no separate composables modules. Feature UI does not access Supabase or instantiate DataSources.

## State and navigation

- Form values belong to a private `InnerInputState`; they are mirrored into public immutable screen state.
- Plannot’s `observeWith`, `updateState`, launch strategies, and ViewModel launch helper are adapted under Loom’s package name.
- `Screen` collects state with lifecycle awareness. `InnerScreen` receives values and callbacks.
- Hosts own typed serializable destinations, saved back stacks, navigation callbacks, and ViewModel lifetimes.
- Plannot’s Navigator, NavigationState, NavDisplay wrappers, and transitions are adapted in core. A modifier is applied once so nested Hosts do not duplicate outer Scaffold padding.
- A household gets its own isolated Koin graph. Switching households disposes the graph and its navigation entries. Koin factories bind ViewModels to their contracts.

## Data and money

DataSource operations map failures to `AppResult` at the I/O boundary and preserve coroutine cancellation. SQL/HTTP models remain in data. Realtime events invalidate household queries; periodic refresh repairs missed events. Queries use keyset pagination so the Data API’s row cap does not truncate balances.

Money is stored in integer minor units. Equal splits distribute remainder cents in a stable member-ID order; percentages use the largest-remainder method. Database transactions validate active members, share totals, authorship, and idempotency IDs. A positive balance means the member is owed money; recording a repayment decreases that credit and the sender’s debt.

Recurring bills retain their day-of-month anchor when February clamps a due date. Statement snapshots are content-addressed and revisioned; a later correction produces a new version instead of changing an issued record. Spending excludes repayments.

## Security

Every exposed app table uses RLS. Access is checked against current household membership, rather than editable metadata or stale JWT role claims. Privileged implementations live in an unexposed schema with fixed search paths and explicit caller checks; the public RPC wrappers run with caller privileges. Financial tables reject direct authenticated writes.

Sessions use Android Keystore encryption and iOS Keychain. The desktop preview keeps its session in memory. No server key, signing key, Firebase configuration, or APNs private key belongs in source control.

The private worker tables deny client access. The worker secret is generated into Supabase Vault; only its hash is stored for verification. Push work uses leases, retries, event IDs, current membership checks, and token cleanup. RSS network requests allow only configured trusted publishers, revalidate every redirect, bound response sizes, and reject DTD/entity declarations.
