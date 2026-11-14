package com.eyther.lumbridge.features.analytics.data.platform

import com.eyther.lumbridge.features.analytics.data.platform.model.AnalyticsEventRequest

internal interface AnalyticsPlatform {

    val available: Boolean

    fun setConsent(enabled: Boolean)

    fun logEvent(event: AnalyticsEventRequest)

    fun clearData()
}
