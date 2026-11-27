"""Marketing captions shared by frame.py (Play) and frame_ios.py (App Store).

One source of truth: the same six screens go to both stores, so the wording lives here once.
Benefit first, feature second: a listing thumbnail is read, not studied. Loom ships English
only; add a locale here only when the app itself ships it.
"""

CAPTIONS = {
    "en-GB": {
        "home": "Your home,\nat a glance",
        "shopping": "One shared list,\nalways up to date",
        "money": "Split costs\nand settle up fairly",
        "trip": "Away? Pay in\nany currency",
        "tasks": "Chores that\ntake turns",
        "news": "A little reading,\nwhen you fancy it",
    },
}

# Listing order. Matches the names StoreScreenshotsCapture saves.
ORDER = ["home", "shopping", "money", "trip", "tasks", "news"]
