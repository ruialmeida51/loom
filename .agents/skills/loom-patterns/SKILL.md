---
name: loom-patterns
description: Loom architecture, UI, state, data and final audit patterns adapted from Plannot.
---

# Loom patterns

The user authorized copying Plannot patterns; this skill routes to the adapted authority in `ai/`.
Read [boundaries](../../../ai/PATTERNS_BOUNDARIES.md) and the relevant PATTERNS_SCREEN,
PATTERNS_VIEWMODEL, PATTERNS_DATA, PATTERNS_DI, PATTERNS_NAVIGATION, PATTERNS_UI and
PATTERNS_COMPONENTS guides. Paths resolve from the repository root when shown without links.

Use the handcrafted authentication screen/ViewModel style. Loom keeps one Gradle module per
feature, domain/data/presentation packages, typed errors, feature-owned models, central annotated DI,
stream() nomenclature and resource-based copy. The user's instructions and AGENTS.md take precedence.

Every implementation ends with [the reviewer checklist](../../../ai/agents/AGENTS_REVIEWER.agent.md)
after validation and all fixes. Record final findings and verification limits. This skill adds no approval
or delegation requirement; complete authorized work and review it directly.
