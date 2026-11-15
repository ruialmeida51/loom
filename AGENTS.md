# Loom engineering conventions

Loom focuses on household management for roommates and couples. Follow the delivery priorities in `docs/product-roadmap.md`; offline functionality is deferred.

Follow the Plannot-derived architecture described in `docs/architecture.md` and the screen and interaction language in `docs/design-language.md`.

- Keep sheet controllers and registrations in entrypoint Hosts, their typed navigation models in separate navigation/model/sheet files, and pure form content in the owning feature presentation package. Core owns only generic sheet/layout infrastructure. Use grouped rows for related content and dialogs only for brief confirmations.

- One Gradle module per feature; domain/data/presentation are Kotlin packages.
- Keep the Host → Screen → ViewModel contract/implementation → UseCase → DataSource boundary.
- Domain imports neither data nor presentation; serialized transport models belong in data.
- Feature models and policies live in their owning feature domain, even when several features use them. Depend on that feature directly; do not move them into core for convenience or add core aliases.
- Core contains only reusable infrastructure and generic utilities. Keep sample data in its feature's data package, preferences and device registration in settings, exports in insights, and app routes in entrypoint.
- Feature dependencies are allowed and must remain acyclic. Declare direct dependencies when importing another feature's types.
- Keep demo fixtures and port implementations in `entrypoint/app/demo`; production screens, domain models, and Common DataSources must not branch on a demo mode.
- Put UI copy, plurals, accessibility labels, and validation text in owning-module string resources. Keep resource classes out of domain. Presentation maps typed AppError subclasses to TextResource; Compose resolves the resource.
- Failures cross data/domain boundaries as `AppResult.Failure(error: AppError)`. Feature error families belong in their own `domain/errors`; core owns only generic infrastructure errors. Keep SQLSTATE, HTTP, SDK code and raw-message checks inside data adapters. Never inspect exception text or string error codes in domain or presentation.
- Put each request/response model in its own matching file under `data/network/model/request` or `response`, with `@SerialName` on every wire field. Keep pure `ResponseToDomain` and `DomainToRequest` mappers separate from models and Common DataSources.
- Use `stream()` (or `streamExpenses()` when a port has multiple streams) and `Get…StreamUseCase`. Fallible streams return `Flow<AppResult<T>>`; cached session/settings streams return domain state. Keep Plannot's `observeWith` lifecycle helper name.
- Use descriptive action and dependency names, explicit API return types, named arguments for multi-parameter domain/DTO/DI calls, and explicit imports. ViewModel events use `on…` names; state update lambdas name the previous state `oldState`.
- Give observable state, contract APIs, navigation models and reusable components focused files and packages. Keep reducers pure and use `updateState` within `observeWith`.
- Use named Gradle project references such as `projects.shared.features.household`.
- Keep all DI providers and graph assembly in the dedicated `shared/di` Gradle module. Follow Plannot's annotated provider pattern: `@Module`, `@Single` returning domain ports, and `@KoinViewModel(binds = [Contract::class])` returning contracts. Feature classes remain free of DI registration. Use Koin's compiler plugin for Kotlin 2.4; do not add KSP or manual `single`/`viewModel` registrations in features or entrypoint. Preview implementations stay in the dedicated demo package and are supplied through the central port-binding module.
- Run `python3 tools/audit_project.py` before committing changes.
- Do not add Repository classes or inner layer/composables Gradle modules.
- Keep form inputs private and mirror them through `observeWith`; state reducers must be pure.
- Screens collect state; InnerScreens receive individual values and callbacks. Hosts own navigation and shared ViewModels.
- Keep money in integer minor units. Validate all money writes transactionally on the server.
- Scope reads and writes by current household membership. Do not expose server keys or private schemas.
- Preserve coroutine cancellation and let DataSources own I/O dispatch.
- Run relevant domain, database, UI, and platform build checks before committing. An iOS build requires macOS/Xcode.
- Changes to a deployed backend need a new migration. Never rewrite an applied migration’s behaviour.

Useful checks are listed in `README.md`. `tools/test_database.py` uses a disposable local database and never connects to the hosted project.

When preparing releases or changing release automation, use the [Loom release skill](.agents/skills/loom-release/SKILL.md). Keep user-facing changes in CHANGELOG.md; the workflow assigns the version and archives notes after a successful upload.
