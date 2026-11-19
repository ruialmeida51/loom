---
name: loom-release
description: Prepare a Loom release, maintain its changelog, and use the existing Firebase/Play version and distribution workflow.
---

# Loom releases

Use the existing `.github/workflows/play-release.yml` workflow. Main app changes default to Firebase; an explicit Play release uploads an internal draft and creates a GitHub release/tag after upload. Connection checks do not upload, archive notes, tag, or consume a version.

1. Add concise, user-facing bullets under `[Unreleased]` in `CHANGELOG.md`. Keep each bullet on one line and the combined release notes within Play's 500-character limit. Describe the final behavior, not development history. Keep older version entries unchanged.
2. Let `tools/plan_release.mjs` allocate the version name and build code from Play, Firebase and successful GitHub deployment history. `loomVersionName` in `gradle.properties` is the minimum bootstrap version, not the version to edit for every build. Never hardcode or reuse a release code.
3. Run `node --test tools/tests/*.mjs`, the relevant Python checks and `python3 tools/audit_project.py`. App changes also require relevant shared/database/platform checks. Deploy any required backend migrations before distributing the app.
4. The workflow freezes notes from its source commit, passes them to Firebase/Play, records the successful release and then archives that version in `CHANGELOG.md`. The archive preserves new notes added while the build ran. An empty Unreleased section reuses the latest published notes for a promotion/rebuild. The changelog-only bot commit is excluded from the release trigger.
5. Confirm the uploaded version and destination from the workflow result. Do not claim a release succeeded until upload finishes. Firebase builds do not create GitHub tags/releases. Tester groups are used only when explicitly configured.
6. Minified Android builds must complete `uploadCrashlyticsMappingFileRelease` and `verify_android_symbols.py` before distribution. Retain the exact APK/AAB, final R8 mapping, Crashlytics ID and version/source metadata for Firebase and Play. Retrace only with the `pg_map_id` reported by the crash; never substitute another build's mapping. iOS device releases use the documented Crashlytics run phase and retain UUID-matched archive dSYMs. Simulator/compilation checks alone do not prove crash delivery or symbolication.

Do not add a second version allocator or a separate manual release-notes file. The prepared notes and changelog snapshot are saved as release artifacts. Existing user authorization applies; this skill adds no approval or confirmation requirement.
