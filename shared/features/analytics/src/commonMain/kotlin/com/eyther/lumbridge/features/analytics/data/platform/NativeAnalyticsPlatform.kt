package com.eyther.lumbridge.features.analytics.data.platform

import com.eyther.lumbridge.features.analytics.data.platform.model.AnalyticsEventRequest

internal expect object NativeAnalyticsPlatform : AnalyticsPlatform {

    override val available: Boolean

    override fun setConsent(enabled: Boolean)

    override fun logEvent(event: AnalyticsEventRequest)

    override fun clearData()
}
