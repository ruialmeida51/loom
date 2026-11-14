package com.eyther.lumbridge.features.analytics.data.platform

import com.eyther.lumbridge.features.analytics.data.platform.model.AnalyticsEventRequest

internal actual object NativeAnalyticsPlatform : AnalyticsPlatform {

    actual override val available: Boolean = false

    actual override fun setConsent(enabled: Boolean) = Unit

    actual override fun logEvent(event: AnalyticsEventRequest) = Unit

    actual override fun clearData() = Unit
}
