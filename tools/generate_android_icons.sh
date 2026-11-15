#!/usr/bin/env bash
# Deterministic Image Asset Studio-compatible resources from Loom's supplied white/gold logo.
set -euo pipefail
cd "$(dirname "$0")/.."
command -v magick >/dev/null
icon_work=$(mktemp -d)
trap 'rm -rf "$icon_work"' EXIT

# Gold is below this blue-channel range; retain the distressed white mark and antialiased edges.
magick branding/loom-icon.png -alpha off -fx 'max(0,min(1,(b-0.55)/0.40))' "$icon_work/alpha.png"
magick branding/loom-icon.png -alpha off -fill white -colorize 100 "$icon_work/alpha.png" -alpha off -compose CopyOpacity -composite -trim +repage "$icon_work/mark.png"
read -r mark_width mark_height < <(magick "$icon_work/mark.png" -format '%w %h\n' info:)
# Fit the complete mark's bounding rectangle inside Android's central 66dp safe circle.
mark_scale=$(awk -v width="$mark_width" -v height="$mark_height" 'BEGIN {printf "%.6f", 100 * 1024 * 66 / (108 * sqrt(width*width + height*height))}')
magick "$icon_work/mark.png" -resize "${mark_scale}%" -gravity center -background none -extent 1024x1024 branding/android/loom-launcher-foreground.png

# A launcher shows the central 72dp of the 108dp adaptive layer.
magick branding/android/loom-launcher-foreground.png -gravity center -crop 682x682+0+0 +repage -resize 512x512 "$icon_work/visible.png"
magick -size 512x512 xc:'#D3AC44' "$icon_work/visible.png" -compose Over -composite branding/android/loom-play-store.png
for spec in 'mdpi 48 108' 'hdpi 72 162' 'xhdpi 96 216' 'xxhdpi 144 324' 'xxxhdpi 192 432'; do
    read -r density legacy_size adaptive_size <<< "$spec"
    icon_destination="androidApp/src/main/res/mipmap-${density}"
    mkdir -p "$icon_destination"
    magick branding/android/loom-launcher-foreground.png -resize "${adaptive_size}x${adaptive_size}" "$icon_destination/ic_launcher_foreground.png"
    magick branding/android/loom-play-store.png -resize "${legacy_size}x${legacy_size}" "$icon_destination/ic_launcher.png"
    magick -size "${legacy_size}x${legacy_size}" xc:none -fill white -draw "circle $((legacy_size/2)),$((legacy_size/2)) $((legacy_size/2)),0" "$icon_work/round.png"
    magick "$icon_destination/ic_launcher.png" "$icon_work/round.png" -alpha off -compose CopyOpacity -composite "$icon_destination/ic_launcher_round.png"
done
for qualifier in anydpi-v26 anydpi-v33; do
    mkdir -p "androidApp/src/main/res/mipmap-${qualifier}"
    for name in ic_launcher ic_launcher_round; do
        {
            printf '%s\n' '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">'
            printf '%s\n' '    <background android:drawable="@color/loom_gold" />' '    <foreground android:drawable="@mipmap/ic_launcher_foreground" />'
            if [[ "$qualifier" == 'anydpi-v33' ]]; then
                printf '%s\n' '    <monochrome android:drawable="@mipmap/ic_launcher_foreground" />'
            fi
            printf '%s\n' '</adaptive-icon>'
        } > "androidApp/src/main/res/mipmap-${qualifier}/${name}.xml"
    done
done

# Review common masks and a system-tinted monochrome rendering at actual launcher proportions.
magick -size 512x512 xc:none -fill white -draw 'circle 256,256 256,0' "$icon_work/circle.png"
magick -size 512x512 xc:none -fill white -draw 'roundrectangle 0,0 511,511 110,110' "$icon_work/rounded.png"
magick branding/android/loom-play-store.png "$icon_work/circle.png" -alpha off -compose CopyOpacity -composite "$icon_work/circle-preview.png"
magick branding/android/loom-play-store.png "$icon_work/rounded.png" -alpha off -compose CopyOpacity -composite "$icon_work/rounded-preview.png"
magick -size 512x512 xc:'#E5E1C3' \( "$icon_work/visible.png" -fill '#494725' -colorize 100 \) -compose Over -composite "$icon_work/circle.png" -alpha off -compose CopyOpacity -composite "$icon_work/themed-preview.png"
magick montage "$icon_work/circle-preview.png" "$icon_work/rounded-preview.png" "$icon_work/themed-preview.png" -tile 3x1 -geometry 200x200+20+20 -background '#ECECEC' branding/android/launcher-masks.png
printf '%s\n' 'Generated legacy, round, adaptive and monochrome launcher resources for all five densities.'
