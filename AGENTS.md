# Loom engineering conventions

Follow the Plannot-derived architecture described in `docs/architecture.md`.

- One Gradle module per feature; domain/data/presentation are Kotlin packages.
- Keep the Host → Screen → ViewModel contract/implementation → UseCase → DataSource boundary.
- Domain imports neither data nor presentation; serialized transport models belong in data.
- Do not add Repository classes or inner layer/composables Gradle modules.
- Keep form inputs private and mirror them through `observeWith`; state reducers must be pure.
- Screens collect state; InnerScreens receive individual values and callbacks. Hosts own navigation and shared ViewModels.
- Keep money in integer minor units. Validate all money writes transactionally on the server.
- Scope reads and writes by current household membership. Do not expose server keys or private schemas.
- Preserve coroutine cancellation and let DataSources own I/O dispatch.
- Run relevant domain, database, UI, and platform build checks before committing. An iOS build requires macOS/Xcode.
- Changes to a deployed backend need a new migration. Never rewrite an applied migration’s behaviour.

Useful checks are listed in `README.md`. `tools/test_database.py` uses a disposable local database and never connects to the hosted project.
