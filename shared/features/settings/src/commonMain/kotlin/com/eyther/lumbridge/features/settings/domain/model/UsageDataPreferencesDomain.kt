package com.eyther.lumbridge.features.settings.domain.model

data class UsageDataPreferencesDomain(
    val initialized: Boolean = false,
    val answered: Boolean = false,
    val analyticsAvailable: Boolean = false,
    val analyticsEnabled: Boolean = false,
    val crashReportsAvailable: Boolean = false,
    val crashReportsEnabled: Boolean = false,
) {
    val shouldAsk: Boolean
        get() = initialized && !answered && (analyticsAvailable || crashReportsAvailable)
}
