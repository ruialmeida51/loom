# Loom boundaries

Authority: [engineering conventions](../AGENTS.md). Adapted from Plannot's
`ai/PATTERNS_BOUNDARIES.md`; Loom keeps one Gradle module per feature and all DI in `shared/di`.

- Host → Screen → ViewModel contract/implementation → UseCase → DataSource. No Repository layer.
- Domain imports neither data nor presentation. Business rules belong on owning domain types or UseCases.
- Feature models stay in their feature; dependencies between features are explicit and acyclic.
- Core owns generic infrastructure/components, never household, shopping or financial concepts.
- Screens collect state and own effects. InnerScreens take individual values/callbacks, never VMs/state bags.
- Hosts own routes, shared VM stores and sheet controllers. Each navigable workflow has its own screen/VM.
- Private inputState is authoritative; observeWith mirrors it into public state.
- Reducers build from named oldState and are pure. No effects, clock reads or stale captured copies.
- Data maps transport failures into typed AppError families. Only data checks HTTP/SQL codes or raw messages.
- Request/response DTOs get separate files and @SerialName fields; mappers are separate from models/adapters.
- Streams use stream(), Get…StreamUseCase and Plannot's observeWith helper.
- Central annotated Koin providers; no feature registrations, KSP or manual single/viewModel declarations.
- Named Gradle project references. No nested domain/data/presentation modules.
- Money uses integer minor units, with transactional server validation and household authorization.
- New backend changes are new migrations. Preserve coroutine cancellation and data-owned I/O.
- Demo ports/fixtures live in entrypoint/app/demo, never production branches.
- All UI copy, accessibility, dates and errors use owning feature resources.
- Destructive actions use filled/outlined buttons, never text buttons; confirmations have request progress.
- Data screens use matching skeletons for first load and explicit refresh with one 300 ms minimum transition. Retain data, drafts, scroll and selection underneath; resume/background updates remain immediate and silent.
- Mutation loading stays in action controls. A recurring completion confirms the occurrence with a snackbar.
- Apply insets once; preserve form drafts, composer identity, scroll order and focus through writes/lifecycle.
- Final review happens after validation and fixes, against the intended base. Never commit directly to main.
