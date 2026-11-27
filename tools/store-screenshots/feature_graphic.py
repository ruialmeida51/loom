#!/usr/bin/env python3
"""Builds the 1024x500 Play Store feature graphic, one per listing locale.

    python3 tools/store-screenshots/feature_graphic.py --output build/store-screenshots/listing

Shares its palette and font handling with frame.py so the graphic and the screenshots read as one
set. Needs Pillow (`pip install Pillow`).
"""

import argparse
import pathlib
import sys

try:
    from PIL import Image, ImageDraw, ImageFilter
except ImportError:
    sys.exit("Pillow is required: pip install Pillow")

import frame

SIZE = (1024, 500)

# Play crops the feature graphic on some surfaces, so everything that matters stays inside this
# margin rather than running to the edge.
SAFE_MARGIN = 110

ICON_PATH = pathlib.Path("branding/android/loom-play-store.png")
ICON_SIZE = 168

# The sign-in screen's own line (ui_a_calmer_home_for_the_things_you_share), so the listing and
# the app say the same thing.
TAGLINES = {
    "en-GB": "A calmer home for the things you share.",
    "pt-PT": "Uma casa mais tranquila para tudo o que partilham.",
}

WORDMARK = "Loom"
WORDMARK_SIZE = 96
TAGLINE_SIZE = 34
TAGLINE_MAX_WIDTH = 620
GAP = 44


def fit_tagline(text, start_size=TAGLINE_SIZE):
    """Largest size at which the tagline clears the text column."""
    for size in range(start_size, 18, -1):
        font = frame.load_font(size, weight=500)
        if font.getbbox(text)[2] - font.getbbox(text)[0] <= TAGLINE_MAX_WIDTH:
            return font
    return frame.load_font(18, weight=500)


def background():
    canvas = frame.vertical_gradient(SIZE, frame.BRAND_TOP, frame.BRAND_BOTTOM).convert("RGBA")

    # A soft wash of the icon's gold in the corner, so the panel is not a flat slab.
    glow = Image.new("RGBA", SIZE, (0, 0, 0, 0))
    ImageDraw.Draw(glow).ellipse([(640, 170), (1180, 620)], fill=(*frame.ACCENT_FROM, 60))
    canvas.alpha_composite(glow.filter(ImageFilter.GaussianBlur(120)))
    return canvas


def icon():
    source = Image.open(ICON_PATH).convert("RGBA").resize((ICON_SIZE, ICON_SIZE), Image.Resampling.LANCZOS)
    return frame.rounded(source, round(ICON_SIZE * 0.23))


def build(locale):
    canvas = background()
    tagline = TAGLINES[locale]

    wordmark_font = frame.load_font(WORDMARK_SIZE, weight=700)
    tagline_font = fit_tagline(tagline)

    wordmark_box = wordmark_font.getbbox(WORDMARK)
    tagline_box = tagline_font.getbbox(tagline)
    text_width = max(wordmark_box[2] - wordmark_box[0], tagline_box[2] - tagline_box[0])

    group_width = ICON_SIZE + GAP + text_width
    left = round((SIZE[0] - group_width) / 2)
    if left < SAFE_MARGIN:
        left = SAFE_MARGIN

    badge = icon()
    canvas.alpha_composite(badge, (left, round((SIZE[1] - ICON_SIZE) / 2)))

    draw = ImageDraw.Draw(canvas)
    text_left = left + ICON_SIZE + GAP

    # Lay the three rows out from their real glyph heights and centre the stack on the icon.
    # getbbox is relative to the drawing origin, so each row's own y-offset has to come back out
    # of the position it is drawn at, or the rule lands on top of the wordmark.
    rule_size = (96, 7)
    wordmark_height = wordmark_box[3] - wordmark_box[1]
    tagline_height = tagline_box[3] - tagline_box[1]
    stack_height = wordmark_height + 26 + rule_size[1] + 26 + tagline_height
    stack_top = round((SIZE[1] - stack_height) / 2)

    draw.text((text_left - wordmark_box[0], stack_top - wordmark_box[1]), WORDMARK,
              font=wordmark_font, fill=frame.INK_ON_BRAND)

    rule_top = stack_top + wordmark_height + 26
    rule = frame.rounded(
        frame.horizontal_gradient(rule_size, frame.ACCENT_FROM, frame.ACCENT_TO).convert("RGBA"), 3
    )
    canvas.alpha_composite(rule, (text_left, rule_top))

    tagline_top = rule_top + rule_size[1] + 26
    draw.text((text_left - tagline_box[0], tagline_top - tagline_box[1]), tagline,
              font=tagline_font, fill=(226, 232, 240))

    # Play rejects a feature graphic with an alpha channel.
    return canvas.convert("RGB")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", required=True, type=pathlib.Path)
    args = parser.parse_args()

    if not ICON_PATH.exists():
        sys.exit(f"run this from the repo root — {ICON_PATH} not found")

    args.output.mkdir(parents=True, exist_ok=True)
    for locale in TAGLINES:
        destination = args.output / f"feature-graphic-{locale}.png"
        build(locale).save(destination, "PNG", optimize=True)
        print(f"wrote {destination}")


if __name__ == "__main__":
    main()
