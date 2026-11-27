# Store listing assets

Adapted from Plannot's `tools/store-screenshots`. Regenerates the phone screenshots for the Play
Console and App Store Connect, the Play feature graphic, and the App Store header and search-results
images. Everything comes from the demo household's sample data, so it needs no account, backend,
emulator or simulator, and every machine produces the same images.

## Run it

From the repo root:

```bash
# 1. Raw 1080x2340 phone captures of the demo app (desktop Compose, light theme).
./gradlew :shared:entrypoint:jvmTest --tests '*StoreScreenshotsCapture*' -PloomStoreCapture=true

# 2. Captioned frames for each store.
python3 tools/store-screenshots/frame.py --input build/store-screenshots/raw \
    --output build/store-screenshots/listing/en-GB --locale en-GB
python3 tools/store-screenshots/frame_ios.py --input build/store-screenshots/raw \
    --output build/store-screenshots/listing-ios/en-GB --locale en-GB

# 3. Brand graphics.
python3 tools/store-screenshots/feature_graphic.py --output build/store-screenshots/listing
python3 tools/store-screenshots/header_and_search_results.py --output build/store-screenshots/listing-ios
```

Framing needs Pillow (`pip install Pillow`).

| Output | Size | For |
|---|---|---|
| `listing/en-GB/NN-<screen>.png` | 1080x1920 | Play phone screenshots (2–8, 9:16) |
| `listing/feature-graphic-en-GB.png` | 1024x500, no alpha | Play feature graphic |
| `listing-ios/en-GB/NN-<screen>.png` | 1179x2556 | App Store iPhone (the required size) |
| `listing-ios/header-en-GB.png` | 3840x1646 | App Store header (optional, iOS 27+) |
| `listing-ios/search-results-en-GB.png` | 2880x1920 | App Store search results (optional) |

## How it differs from Plannot

- **Capture** is a desktop UI test (`shared/entrypoint/src/jvmTest/.../store/StoreScreenshotsCapture.kt`)
  driving `LoomDemoApp(showPreviewLabel = false)`, not an emulator walk. It runs only with
  `-PloomStoreCapture=true`, so CI never writes listing images. Both stores use the same captures,
  because Loom's UI is shared Compose.
- **Brand**: the primary teal (`#1E6B57`) deepening down the canvas, the icon's gold (`#D3AC44`) as
  the accent, Space Grotesk Bold for captions and the wordmark, Inter for the tagline, and
  `branding/android/loom-play-store.png` as the icon.
- **One locale** (en-GB), since that's the only language the app ships.

## Changing the listing

- **Copy**: the captions are in `captions.py`; the tagline is in `TAGLINES` in `feature_graphic.py`
  (it's the sign-in screen's `ui_a_calmer_home_for_the_things_you_share`).
- **Content**: the demo household's data is in `shared/entrypoint/.../app/demo/data`.
- **Which screens, and their order**: the `save(...)` calls in `StoreScreenshotsCapture` and `ORDER` in
  `captions.py` must name the same screens.
- The greeting and task times follow the clock, so capture during the day for "Good morning".
