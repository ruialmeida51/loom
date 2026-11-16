# Validation and final audit

Adapted from Plannot `ai/PATTERNS_TESTING.md` and reviewer procedure.

Choose behavior tests for concurrency, errors, drafts, date/timezone boundaries, refresh and navigation.
Use disposable PostgreSQL for RLS, atomicity, invitation expiry/revocation/guess limits and upgrades.
Mock HTTP proves wire mapping, not authorization. Use native Android tests for IME, touch geometry,
refresh gestures, background/resume and system Back. JVM Compose tests cover state-driven UI flows.

Run tools/audit_project.py; appropriate Gradle JVM/common/Android builds; database integration checks;
release tools tests when affected. No concurrent Gradle processes. iOS verification requires macOS/Xcode.
Record actual checks and gaps. Screenshots/previews do not by themselves prove device behavior.

Finish with [the reviewer checklist](agents/AGENTS_REVIEWER.agent.md) against origin/main, after all
fixes. Recheck changed code after audit fixes. Findings include path:line, severity, problem and concrete
fix; report pass / pass with remaining findings / needs revision and validation limits.
