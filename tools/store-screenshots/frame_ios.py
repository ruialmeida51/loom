#!/usr/bin/env python3
"""Turns raw iOS Simulator captures into App Store listing frames.

Each output is 1179x2556 — Apple's one *required* iPhone size as of this writing ("iPhone with
Dynamic Island, medium display", the iPhone 17/15/16 non-Pro/non-Plus class); App Store Connect
scales everything else down from it. Same visual language as frame.py's Play Store frames (brand
gradient, captioned, device inset), scaled for the taller 1179x2556 canvas rather than reusing its
1080x1920 constants directly.

    python3 tools/store-screenshots/frame_ios.py --input build/store-screenshots/raw-ios \
                                                  --output build/store-screenshots/listing-ios \
                                                  --locale en-GB

Needs Pillow (`pip install Pillow`). Run tools/store-screenshots/capture-ios.sh first.
"""

import argparse
import pathlib
import sys

try:
    from PIL import Image, ImageDraw, ImageFilter, ImageFont
except ImportError:
    sys.exit("Pillow is required: pip install Pillow")

from captions import CAPTIONS, ORDER

CANVAS = (1179, 2556)

# Loom's palette: the primary teal (LoomColorScheme.primary, #1E6B57) deepening down the canvas,
# with the launcher icon's gold (#D3AC44) as the accent.
BRAND_TOP = (30, 107, 87)
BRAND_BOTTOM = (12, 46, 38)
INK_ON_BRAND = (255, 255, 255)
ACCENT_FROM = (211, 172, 68)   # icon gold
ACCENT_TO = (240, 214, 140)    # lighter gold

FONTS = pathlib.Path("shared/core/src/commonMain/composeResources/font")
# Loom's headline face for captions and the wordmark; its text face for taglines.
FONT_PATH = FONTS / "SpaceGrotesk_700.ttf"
TEXT_FONT_PATHS = {400: FONTS / "Inter_400.ttf", 500: FONTS / "Inter_500.ttf", 600: FONTS / "Inter_600.ttf"}

# frame.py's layout, scaled by this canvas's width ratio (1179/1080) and height ratio (2556/1920)
# against its own — the extra height over a straight width-scale goes to a taller caption column
# and more breathing room above/below the device rather than a bigger device image.
DEVICE_WIDTH = 677
DEVICE_TOP = 572
DEVICE_RADIUS = 50
BEZEL = 10
CAPTION_TOP = 200
CAPTION_MAX_WIDTH = 982
CAPTION_LINE_SPACING = 1.22


def load_font(size, weight=700):
    """Space Grotesk Bold for headlines (weight 700 and up), Inter for lighter text."""
    path = FONT_PATH if weight >= 700 else TEXT_FONT_PATHS.get(weight, TEXT_FONT_PATHS[500])
    return ImageFont.truetype(str(path), size)


def fit_font(lines, start_size=72):
    """Largest size at which every line clears the caption column."""
    for size in range(start_size, 30, -2):
        font = load_font(size)
        widest = max(font.getbbox(line)[2] - font.getbbox(line)[0] for line in lines)
        if widest <= CAPTION_MAX_WIDTH:
            return font
    return load_font(32)


def vertical_gradient(size, top, bottom):
    width, height = size
    gradient = Image.new("RGB", (1, height))
    for y in range(height):
        ratio = y / max(height - 1, 1)
        gradient.putpixel(
            (0, y),
            tuple(round(top[i] + (bottom[i] - top[i]) * ratio) for i in range(3)),
        )
    return gradient.resize(size, Image.Resampling.BILINEAR)


def horizontal_gradient(size, left, right):
    width, height = size
    gradient = Image.new("RGB", (width, 1))
    for x in range(width):
        ratio = x / max(width - 1, 1)
        gradient.putpixel(
            (x, 0),
            tuple(round(left[i] + (right[i] - left[i]) * ratio) for i in range(3)),
        )
    return gradient.resize(size, Image.Resampling.BILINEAR)


def rounded(image, radius):
    mask = Image.new("L", image.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([(0, 0), (image.size[0] - 1, image.size[1] - 1)],
                                           radius=radius, fill=255)
    image.putalpha(mask)
    return image


def draw_caption(canvas, text):
    lines = text.split("\n")
    font = fit_font(lines)
    draw = ImageDraw.Draw(canvas)
    ascent, descent = font.getmetrics()
    line_height = round((ascent + descent) * CAPTION_LINE_SPACING)

    y = CAPTION_TOP
    for line in lines:
        bbox = font.getbbox(line)
        draw.text(((CANVAS[0] - (bbox[2] - bbox[0])) / 2 - bbox[0], y), line,
                  font=font, fill=INK_ON_BRAND)
        y += line_height

    # A short accent rule picks up the launcher icon's gold.
    rule_width, rule_height = 120, 9
    rule = rounded(horizontal_gradient((rule_width, rule_height), ACCENT_FROM, ACCENT_TO).convert("RGBA"),
                   rule_height // 2)
    canvas.paste(rule, (round((CANVAS[0] - rule_width) / 2), y + 26), rule)


def frame_one(capture_path, caption):
    canvas = vertical_gradient(CANVAS, BRAND_TOP, BRAND_BOTTOM).convert("RGBA")
    draw_caption(canvas, caption)

    capture = Image.open(capture_path).convert("RGB")
    device_height = round(capture.height * DEVICE_WIDTH / capture.width)
    screen = rounded(capture.resize((DEVICE_WIDTH, device_height), Image.Resampling.LANCZOS).convert("RGBA"),
                     DEVICE_RADIUS)

    bezel_box = (DEVICE_WIDTH + BEZEL * 2, device_height + BEZEL * 2)
    bezel_x = round((CANVAS[0] - bezel_box[0]) / 2)

    # Soft drop shadow so the device sits on the background rather than in it.
    shadow = Image.new("RGBA", CANVAS, (0, 0, 0, 0))
    ImageDraw.Draw(shadow).rounded_rectangle(
        [(bezel_x, DEVICE_TOP + 22), (bezel_x + bezel_box[0], DEVICE_TOP + 22 + bezel_box[1])],
        radius=DEVICE_RADIUS + BEZEL, fill=(0, 0, 0, 120),
    )
    canvas.alpha_composite(shadow.filter(ImageFilter.GaussianBlur(28)))

    bezel = Image.new("RGBA", bezel_box, (0, 0, 0, 0))
    ImageDraw.Draw(bezel).rounded_rectangle([(0, 0), (bezel_box[0] - 1, bezel_box[1] - 1)],
                                            radius=DEVICE_RADIUS + BEZEL, fill=(255, 255, 255, 38))
    canvas.alpha_composite(bezel, (bezel_x, DEVICE_TOP))
    canvas.alpha_composite(screen, (bezel_x + BEZEL, DEVICE_TOP + BEZEL))

    return canvas.convert("RGB")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", required=True, type=pathlib.Path)
    parser.add_argument("--output", required=True, type=pathlib.Path)
    parser.add_argument("--locale", default="en-GB", choices=sorted(CAPTIONS))
    args = parser.parse_args()

    if not FONT_PATH.exists():
        sys.exit(f"run this from the repo root — {FONT_PATH} not found")

    captions = CAPTIONS[args.locale]
    args.output.mkdir(parents=True, exist_ok=True)
    written = 0
    for index, screen in enumerate(ORDER, start=1):
        capture = args.input / f"{screen}.png"
        if not capture.exists():
            print(f"skipping {screen}: {capture} not found")
            continue
        destination = args.output / f"{index:02d}-{screen}.png"
        frame_one(capture, captions[screen]).save(destination, "PNG", optimize=True)
        print(f"wrote {destination}")
        written += 1

    if written < 1:
        sys.exit("App Store needs at least 1 phone screenshot — captured none")


if __name__ == "__main__":
    main()
