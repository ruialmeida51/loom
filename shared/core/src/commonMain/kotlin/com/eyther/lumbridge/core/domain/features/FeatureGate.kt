package com.eyther.lumbridge.core.domain.features

/** A capability a household can have switched off, keyed as on the server. */
enum class Feature(val key: String) {
    ACCOUNT_GUESTS("account_guests"),
}

/**
 * Whether the current household has a [Feature]. Features are on unless the household has them
 * switched off, so a failed lookup answers `true` and the server stays the final word.
 */
fun interface FeatureGate {
    suspend fun isEnabled(feature: Feature): Boolean

    companion object {
        /** Every feature on: previews, demo and tests. */
        val AllOn: FeatureGate = FeatureGate { true }
    }
}
