# Data

Adapted from Plannot `ai/PATTERNS_DATA.md`; see [boundaries](PATTERNS_BOUNDARIES.md).

Domain DataSources are ports. Common…DataSource adapters own transport/I/O; UseCases own policy and
aggregation. Use safeApiCall/asApiResult and preserve cancellation. Typed AppError crosses upward.
Keep DTOs in request/response files, annotate every wire field with @SerialName and map separately.

RLS and RPCs verify current household membership and ownership. Exposed SQL wrappers are invokers;
necessary definers stay in loom_private, with fixed search_path and explicit grants. Never embed server keys.
Concurrent edits use versions. Bulk writes validate and lock every target before changing any target.
Retrying a successful removal is harmless; a changed/restored item is never silently deleted.

Short invitations are hashed, expiring, single-use and revocable. Guess limits must survive failed
redemptions: return a transport result instead of raising an exception that rolls the counter back.
Raw response statuses become typed domain errors in data mappers, never in UI.

Test policy against disposable PostgreSQL, not hosted user records. Add new migrations; never edit an
applied migration. Hosted verification reads schema/permission metadata and migration history.
