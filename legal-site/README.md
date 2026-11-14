# Loom legal pages

The public policy website is built from `tools/build_legal_site.py` and `config/loom-legal.properties`. It has no JavaScript, cookies, analytics tags or external fonts. Generated output stays in `dist/` and is not committed.

Run `python3 tools/build_legal_site.py --draft` for an unpublished local draft. A publication build requires an app/developer label and working contact email. Enable **Settings → Pages → Source: GitHub Actions** for the repository, then run **Legal pages**. The expected origin is `https://loom.eyther.dev`.

The Play release workflow checks the public privacy, terms and deletion pages against the policy version and publisher in the source before it uploads a bundle.
