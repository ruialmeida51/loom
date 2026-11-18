package com.eyther.lumbridge.features.analytics.data.platform

interface IOSAnalyticsReporter {
    fun setConsent(enabled: Boolean)

    fun logEvent(name: String, parameters: Map<String, String>)

    fun clearData()
}
