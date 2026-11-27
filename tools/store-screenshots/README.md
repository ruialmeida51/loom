# Store listing assets

Adapted from Plannot's `tools/store-screenshots`. Regenerates the phone screenshots for the Play
Console and App Store Connect, the Play feature graphic, and the App Store header and search-results
images. Everything comes from the demo household's sample data, so it needs no account, backend,
emulator or simulator, and every machine produces the same images.

## Run it

From the repo root:

```bash
for locale in en-GB pt-PT; do
    # 1. Raw 1080x2340 phone captures of the demo app (desktop Compose, light theme), with the
    #    UI, the money format and the sample household all in that language.
    ./gradlew :shared:entrypoint:jvmTest --tests '*StoreScreenshotsCapture*' \
        -PloomStoreCapture=true -PloomStoreLocale=$locale --rerun

    # 2. Captioned frames for each store.
    python3 tools/store-screenshots/frame.py --input build/store-screenshots/raw/$locale \
        --output build/store-screenshots/listing/$locale --locale $locale
    python3 tools/store-screenshots/frame_ios.py --input build/store-screenshots/raw/$locale \
        --output build/store-screenshots/listing-ios/$locale --locale $locale
done

# 3. Brand graphics.
python3 tools/store-screenshots/feature_graphic.py --output build/store-screenshots/listing
python3 tools/store-screenshots/header_and_search_results.py --output build/store-screenshots/listing-ios
```

Framing needs Pillow (`pip install Pillow`).

| Output | Size | For |
|---|---|---|
| `listing/<locale>/NN-<screen>.png` | 1080x1920 | Play phone screenshots (2–8, 9:16) |
| `listing/feature-graphic-<locale>.png` | 1024x500, no alpha | Play feature graphic |
| `listing-ios/<locale>/NN-<screen>.png` | 1179x2556 | App Store iPhone (the required size) |
| `listing-ios/header-<locale>.png` | 3840x1646 | App Store header (optional, iOS 27+) |
| `listing-ios/search-results-<locale>.png` | 2880x1920 | App Store search results (optional) |

## How it differs from Plannot

- **Capture** is a desktop UI test (`shared/entrypoint/src/jvmTest/.../store/StoreScreenshotsCapture.kt`)
  driving `LoomDemoApp(showPreviewLabel = false)`, not an emulator walk. It runs only with
  `-PloomStoreCapture=true`, so CI never writes listing images. Both stores use the same captures,
  because Loom's UI is shared Compose.
- **Brand**: the primary teal (`#1E6B57`) deepening down the canvas, the icon's gold (`#D3AC44`) as
  the accent, Space Grotesk Bold for captions and the wordmark, Inter for the tagline, and
  `branding/android/loom-play-store.png` as the icon.
- **Two locales**, en-GB and pt-PT, the languages the app ships. The capture sets the JVM
  locale, so string resources, money ("26,33 €") and the demo household (`demoText`) follow it.

## Changing the listing

- **Copy**: the captions are in `captions.py`; the tagline is in `TAGLINES` in `feature_graphic.py`
  (it's the sign-in screen's `ui_a_calmer_home_for_the_things_you_share`).
- **Content**: the demo household's data is in `shared/entrypoint/.../app/demo/data`.
- **Which screens, and their order**: the `save(...)` calls in `StoreScreenshotsCapture` and `ORDER` in
  `captions.py` must name the same screens.
- The greeting and task times follow the clock, so capture during the day for "Good morning".
