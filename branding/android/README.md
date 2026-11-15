# Android launcher assets

The supplied `../loom-icon.png` stays unchanged. `tools/generate_android_icons.sh` deterministically extracts its distressed white mark onto transparency and generates Image Asset Studio-compatible resources. Android Studio's GUI was unavailable in the build workspace; this is the actual generation method.

- Five legacy/round density sets: 48, 72, 96, 144 and 192 px.
- Five adaptive foreground layers: 108, 162, 216, 324 and 432 px, with a solid `loom_gold` background.
- API 26 adaptive and round XML, API 33 monochrome/themed XML, and the manifest's `roundIcon`.
- The complete mark fits within the central 66dp safe circle of the 108dp layer. The tail is preserved.
- `loom-play-store.png` is the opaque 512px store asset; `launcher-masks.png` previews circle, rounded-square and themed rendering.

Regenerate with ImageMagick 7:

```sh
./tools/generate_android_icons.sh
```

To inspect/import in Android Studio, open **New → Image Asset → Launcher Icons (Adaptive and Legacy)**. Use `loom-launcher-foreground.png` as the foreground, disable trimming and retain its prepared padding; use `#D3AC44` as the background. The same alpha mark supplies the monochrome layer. Review all masks before accepting Studio's generated resources.

References: [Image Asset Studio](https://developer.android.com/studio/write/create-app-icons), [adaptive icon geometry](https://developer.android.com/develop/ui/compose/system/icon_design_adaptive).
