#!/usr/bin/env python3
"""Builds the App Store Connect "Header" and "Search Results" creative assets, one pair per
listing locale.

New Apple feature (shipped 2026-10-05, iOS/iPadOS 27+ only) — optional, separate from screenshots
and app previews, uploaded via App Store Connect's own Asset Library rather than this repo. See
store/appstore/README.md for upload steps.

    python3 tools/store-screenshots/header_and_search_results.py --output build/store-screenshots/listing-ios

Shares its palette, font handling and tagline text with feature_graphic.py (the Play equivalent)
and frame.py, so every store asset reads as one brand rather than three. Needs Pillow
(`pip install Pillow`).
"""

import argparse
import pathlib
import sys

try:
    from PIL import Image, ImageDraw, ImageFilter
except ImportError:
    sys.exit("Pillow is required: pip install Pillow")

import frame
from feature_graphic import ICON_PATH, TAGLINES, WORDMARK, icon

# Apple's primary aspect for each placement (an alternate 16:9 @ 5244x2950 exists for both but
# isn't used here — see the spec: /help/app-store-connect/reference/app-information/creative-assets-specifications).
HEADER_SIZE = (3840, 1646)       # 21:9, shown above the icon on the product page
SEARCH_RESULTS_SIZE = (2880, 1920)  # 3:2, within Apple's 1920x1280-3840x2560 bounds

# Apple crops/scales these for different surfaces, so nothing that matters sits past this margin.
SAFE_MARGIN_RATIO = 0.08

WORDMARK_SIZE = 170
TAGLINE_SIZE = 56
GAP = 70


def fit_tagline(text, max_width, start_size=TAGLINE_SIZE):
    for size in range(start_size, 24, -2):
        font = frame.load_font(size, weight=500)
        if font.getbbox(text)[2] - font.getbbox(text)[0] <= max_width:
            return font
    return frame.load_font(24, weight=500)


def background(size, glow_box):
    canvas = frame.vertical_gradient(size, frame.BRAND_TOP, frame.BRAND_BOTTOM).convert("RGBA")
    glow = Image.new("RGBA", size, (0, 0, 0, 0))
    ImageDraw.Draw(glow).ellipse(glow_box, fill=(*frame.ACCENT_FROM, 60))
    canvas.alpha_composite(glow.filter(ImageFilter.GaussianBlur(size[1] // 6)))
    return canvas


def build_horizontal(size, locale, icon_size):
    """Header layout: icon beside a wordmark+rule+tagline stack, centred as one group — the same
    composition feature_graphic.py uses, scaled up for a much wider canvas."""
    canvas = background(size, glow_box=(
        round(size[0] * 0.55), round(size[1] * 0.1),
        round(size[0] * 0.95), round(size[1] * 0.95),
    ))
    tagline = TAGLINES[locale]
    safe_margin = round(size[0] * SAFE_MARGIN_RATIO)
    max_text_width = size[0] - safe_margin * 3 - icon_size

    wordmark_font = frame.load_font(WORDMARK_SIZE, weight=700)
    tagline_font = fit_tagline(tagline, max_text_width)

    wordmark_box = wordmark_font.getbbox(WORDMARK)
    tagline_box = tagline_font.getbbox(tagline)
    text_width = max(wordmark_box[2] - wordmark_box[0], tagline_box[2] - tagline_box[0])

    group_width = icon_size + GAP + text_width
    left = max(round((size[0] - group_width) / 2), safe_margin)

    badge = frame.rounded(
        Image.open(ICON_PATH).convert("RGBA").resize((icon_size, icon_size), Image.Resampling.LANCZOS),
        round(icon_size * 0.23),
    )
    canvas.alpha_composite(badge, (left, round((size[1] - icon_size) / 2)))

    draw = ImageDraw.Draw(canvas)
    text_left = left + icon_size + GAP

    rule_size = (round(icon_size * 0.57), 12)
    wordmark_height = wordmark_box[3] - wordmark_box[1]
    tagline_height = tagline_box[3] - tagline_box[1]
    stack_height = wordmark_height + 44 + rule_size[1] + 44 + tagline_height
    stack_top = round((size[1] - stack_height) / 2)

    draw.text((text_left - wordmark_box[0], stack_top - wordmark_box[1]), WORDMARK,
              font=wordmark_font, fill=frame.INK_ON_BRAND)

    rule_top = stack_top + wordmark_height + 44
    rule = frame.rounded(
        frame.horizontal_gradient(rule_size, frame.ACCENT_FROM, frame.ACCENT_TO).convert("RGBA"), 6
    )
    canvas.alpha_composite(rule, (text_left, rule_top))

    tagline_top = rule_top + rule_size[1] + 44
    draw.text((text_left - tagline_box[0], tagline_top - tagline_box[1]), tagline,
              font=tagline_font, fill=(226, 232, 240))

    return canvas.convert("RGB")


def build_stacked(size, locale, icon_size):
    """Search Results layout: a squarer canvas shown small in the Search tab, so the group is
    centred and stacked vertically instead of running wide."""
    canvas = background(size, glow_box=(
        round(size[0] * 0.1), round(size[0] * 0.1),
        round(size[0] * 0.9), round(size[0] * 0.75),
    ))
    tagline = TAGLINES[locale]
    safe_margin = round(min(size) * SAFE_MARGIN_RATIO)
    max_text_width = size[0] - safe_margin * 2

    wordmark_font = frame.load_font(round(WORDMARK_SIZE * 0.72), weight=700)
    tagline_font = fit_tagline(tagline, max_text_width, start_size=round(TAGLINE_SIZE * 0.8))

    wordmark_box = wordmark_font.getbbox(WORDMARK)
    tagline_box = tagline_font.getbbox(tagline)

    badge = frame.rounded(
        Image.open(ICON_PATH).convert("RGBA").resize((icon_size, icon_size), Image.Resampling.LANCZOS),
        round(icon_size * 0.23),
    )

    rule_size = (round(icon_size * 0.5), 10)
    wordmark_height = wordmark_box[3] - wordmark_box[1]
    tagline_height = tagline_box[3] - tagline_box[1]
    stack_height = icon_size + 56 + wordmark_height + 36 + rule_size[1] + 36 + tagline_height
    stack_top = round((size[1] - stack_height) / 2)

    canvas.alpha_composite(badge, (round((size[0] - icon_size) / 2), stack_top))

    draw = ImageDraw.Draw(canvas)
    wordmark_top = stack_top + icon_size + 56
    draw.text(((size[0] - (wordmark_box[2] - wordmark_box[0])) / 2 - wordmark_box[0],
               wordmark_top - wordmark_box[1]), WORDMARK, font=wordmark_font, fill=frame.INK_ON_BRAND)

    rule_top = wordmark_top + wordmark_height + 36
    rule = frame.rounded(
        frame.horizontal_gradient(rule_size, frame.ACCENT_FROM, frame.ACCENT_TO).convert("RGBA"), 5
    )
    canvas.alpha_composite(rule, (round((size[0] - rule_size[0]) / 2), rule_top))

    tagline_top = rule_top + rule_size[1] + 36
    draw.text(((size[0] - (tagline_box[2] - tagline_box[0])) / 2 - tagline_box[0],
               tagline_top - tagline_box[1]), tagline, font=tagline_font, fill=(226, 232, 240))

    return canvas.convert("RGB")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", required=True, type=pathlib.Path)
    args = parser.parse_args()

    if not ICON_PATH.exists():
        sys.exit(f"run this from the repo root — {ICON_PATH} not found")

    args.output.mkdir(parents=True, exist_ok=True)
    for locale in TAGLINES:
        header_path = args.output / f"header-{locale}.png"
        build_horizontal(HEADER_SIZE, locale, icon_size=460).save(header_path, "PNG", optimize=True)
        print(f"wrote {header_path}")

        search_path = args.output / f"search-results-{locale}.png"
        build_stacked(SEARCH_RESULTS_SIZE, locale, icon_size=560).save(search_path, "PNG", optimize=True)
        print(f"wrote {search_path}")


if __name__ == "__main__":
    main()
