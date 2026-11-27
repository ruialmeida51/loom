"""Marketing captions shared by frame.py (Play) and frame_ios.py (App Store).

One source of truth: the same six screens go to both stores, so the wording lives here once.
Benefit first, feature second: a listing thumbnail is read, not studied. Add a locale here only
when the app itself ships it.
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
    # Written, not translated: the app's own informal "tu", the same as its pt strings.
    "pt-PT": {
        "home": "A tua casa,\nnum relance",
        "shopping": "Uma lista partilhada,\nsempre em dia",
        "money": "Divide despesas\ne acerta contas",
        "trip": "Fora de casa? Paga\nem qualquer moeda",
        "tasks": "Tarefas que\nse fazem à vez",
        "news": "Um pouco de leitura,\nquando te apetecer",
    },
}

# Listing order. Matches the names StoreScreenshotsCapture saves.
ORDER = ["home", "shopping", "money", "trip", "tasks", "news"]
