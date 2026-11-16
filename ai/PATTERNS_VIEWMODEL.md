# ViewModels

Adapted from Plannot `ai/PATTERNS_VIEWMODEL.md`; see [boundaries](PATTERNS_BOUNDARIES.md).
Reference style: authentication/presentation/viewmodel/LoginScreenViewModel.kt and its contract.

- Form: private InnerInputState + inputState; observeWith/updateState expose rendering values.
- Read destination: launchWith for initial one-shot reads; observeWith for subscriptions. No UI startup effects.
- Host shared state: owned by the Host's store, not a second instance created by a child Screen.
- Use on… events, explicit types, named arguments and multiline calls grouped by intent.
- MutableStateFlow.update lambdas name oldState; fold external results into oldState, never stale captures.
- A failed action maps its AppError through a feature mapper. Never replace all failures with a generic flag.
- Keep initial loading, explicit refreshing and per-action progress separate. Retain loaded data on failure.
- delayAtLeast applies to reads; delayFirstAtLeast delays only the first stream result, not realtime events.
- Refresh restarts the actual read, preserves drafts/selections and settles on success or typed failure.
- Async success feedback is TextResource? plus a VM-owned cancellable timer (DEFAULT_ALERT_DURATION).
- Use immutable presentation collections; stable keys reflect record identity, never row indexes.
- Streams must not replace pending writes with stale results. Financial/bulk writes retain server concurrency checks.
