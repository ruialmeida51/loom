# UI and stability

Adapted from Plannot `ai/PATTERNS_UI.md` and `PATTERNS_VISUAL.md`.

Use Material theme colours, shapes and typography consistently. Keep compact grouped information,
a clear primary action and relevant amounts ahead of metadata. Feature dimensions stay in features;
shared tokens carry shared semantics. Test light/dark, narrow screens, IME and larger text.

Shimmer is for content loading; CircularProgressIndicator is for requests triggered by actions.
Use Loom's draw-phase shimmer and delayAtLeast/delayFirstAtLeast. A skeleton matches its final grouping.
Pull-to-refresh is real and visible; background updates remain silent. Retain data on refresh failure.

Prefer persistent UI lists/maps/sets and stable keys. Mark sealed UI interfaces and subtypes @Stable;
never annotate mutable types as immutable. Filtering/mapping belongs outside composition. Small
resource resolution stays in composition so language changes update copy immediately.

Checkbox/composer/pending rows reserve identical leading and text geometry. Keep one composer
node across commits; writes are serial in entry order. Insets and focus are behavior, not decoration.

Delete uses a filled/outlined destructive button with accessible text and loading, never TextButton.
The action menu can open a confirmation. Success uses transient nonmodal feedback; errors stay actionable.
