# Loom redesign research brief

Captured on 16 November 2026 from the request to reconsider Loom's usability and compare it with the original Lumbridge app. This is the working brief for that research and the subsequent redesign proposal.

## Intended outcome

Roommates and couples should be able to record shared spending, manage groceries and deal with reminders with little effort. Existing functionality broadly covers the need. The priority is making the app feel coherent, responsive and easy to understand during ordinary use.

## Actionable work

1. **Compare all three projects.** Inspect original `lumbridge-android`, `lumbridge-kmp` and current Loom. Inventory household-relevant features, components, layouts and interaction patterns. Classify each as keep, adapt or defer, with reasons and source references.
2. **Diagnose the feeling of slowness.** Trace opening a screen, entering an expense, changing splits, submitting groceries and refreshing. Separate server latency, serial requests, artificial loading delays, discarded content, keyboard/layout movement and navigation animation. Record what is observed in code and what still requires device measurements.
3. **Benchmark real task flows.** Study Tricount's expense entry and unequal splits, Keep's continuous checklist entry, and suitable reminder/quick-capture patterns. Count decisions, taps, modal changes and network gates rather than judging screenshots alone.
4. **Choose one established design system.** Recommend a named system and define its application to Loom: spacing, type hierarchy, density, surfaces, color roles, icons, touch targets, forms, loading and feedback. An existing component dependency alone does not meet this requirement.
5. **Redesign the common expense path first.** Prioritize day-to-day and trip spending: amount and description, sensible payer/participant defaults, a clear split summary and reachable save action. Support equal, shares, exact and percentage splits without exposing every control at once.
6. **Define consistent navigation and editing rules.** Specify which tasks use full screens, sheets, inline editing or confirmations. Define Back, system Back/gesture, swipe dismissal, cancellation, keyboard behavior and return destinations. Preserve drafts and list position across these transitions.
7. **Rebalance Home and repeated lists.** Reduce competing text, oversized wrappers and arbitrary whitespace. Give reminders and shopping useful previews, keep the requested expense/reminder quick actions, and make the next action clear. Make expenses and balances easy to scan.
8. **Make the Feed discoverable while keeping it secondary.** Evaluate a compact horizontal Home preview of about three stories, a clear full-feed route and an editable starter-source selection that makes first use useful. Treat these as ideas to test, not a decision to turn Home into a news page.
9. **Reconsider the visual identity.** A complete palette reset is authorized. Original Lumbridge colors are an optional reference. Compare light/dark and accessible contrast after information hierarchy and core flows are settled.
10. **Produce a concrete implementation proposal.** Deliver cited findings, an explicit design-system recommendation, representative flows, priorities and acceptance checks. Carry the accepted choices into shared components and engineering/design rules during implementation, followed by the requested package/pattern audit.

## Constraints and decisions to retain

- Keep Loom as the working KMP household app with its feature-owned domain/data/presentation packages, typed errors, resources and centralized annotated DI.
- Keep everyday and trip/event expense accounts, household members only, one currency per account, unequal splits and recurring bills.
- Keep Shopping checklist-first with Enter creating the next entry; optional details stay secondary.
- Home, Shopping, Balances and More remain the starting navigation model to evaluate. Global profile stays outside individual households.
- Keep useful phrases where they help; reduce repeated explanations and labels.
- Full offline operation remains deferred. Investigate in-memory reuse, draft continuity, background refresh and explicit pending feedback before proposing an offline synchronization project.
- Existing online financial validation and explicit failure handling remain essential to trustworthy balances.
- Reference-app code may be adapted where appropriate; its architecture and product scope do not automatically become Loom's requirements.

## Research outputs and implementation gates

- [Research and proposed direction](../research/usability-and-design-system.md): comparisons, evidence, recommendation and a staged worklist.
- Prototype the expense path and a representative Home/list view before spreading a new visual language across every feature.
- Validate the chosen path with real keyboard, gestures, loading/failure, large text, narrow screens and light/dark states.
- Record approved changes to loading policy, tokens and components in their existing authoritative guides; avoid accumulating contradictory rules.
- Implement the redesign in reviewable slices and run a final audit after fixes. This research brief does not itself change production UI, backend schemas or release automation.
