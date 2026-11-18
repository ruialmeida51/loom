package com.eyther.lumbridge.features.settings.data.local.model

/** Readiness and the saved decision change together, so a restored choice cannot flash a prompt. */
internal data class UsageDataConsentDecision(
    val initialized: Boolean = false,
    val answered: Boolean = false,
)
