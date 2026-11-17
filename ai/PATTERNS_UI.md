# UI and stability

Adapted from Plannot `ai/PATTERNS_UI.md` and `PATTERNS_VISUAL.md`.

Use Material theme colours, shapes and typography consistently. Keep compact grouped information,
a clear primary action and relevant amounts ahead of metadata. Feature dimensions stay in features;
shared tokens carry shared semantics. Test light/dark, narrow screens, IME and larger text.

Shimmer is for content loading; CircularProgressIndicator is for requests triggered by actions.
Use Loom's draw-phase shimmer for initial loading and explicit refresh with one 300 ms minimum
transition. Match final grouping and retain loaded data/drafts underneath. Resume/background updates
remain immediate and silent. Retain data on refresh failure.

Prefer persistent UI lists/maps/sets and stable keys. Mark sealed UI interfaces and subtypes @Stable;
never annotate mutable types as immutable. Filtering/mapping belongs outside composition. Small
resource resolution stays in composition so language changes update copy immediately.

Checkbox/composer/pending rows reserve identical leading and text geometry. Keep one composer
node across commits; writes are serial in entry order. Insets and focus are behavior, not decoration.

App actions use filled, tonal or outlined buttons; navigation uses grouped rows. Avoid TextButton.
Delete uses a filled/outlined destructive button with accessible text and loading.
The action menu can open a confirmation. Success uses transient nonmodal feedback; errors stay actionable.
