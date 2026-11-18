package com.eyther.lumbridge.features.analytics.data.platform

import com.eyther.lumbridge.features.analytics.data.platform.model.AnalyticsEventRequest

internal actual object NativeAnalyticsPlatform : AnalyticsPlatform {
    actual override val available: Boolean
        get() = NativeAnalyticsBridge.reporter != null

    actual override fun setConsent(enabled: Boolean) {
        NativeAnalyticsBridge.reporter?.setConsent(enabled = enabled)
    }

    actual override fun logEvent(event: AnalyticsEventRequest) {
        NativeAnalyticsBridge.reporter?.logEvent(name = event.name, parameters = event.parameters)
    }

    actual override fun clearData() {
        NativeAnalyticsBridge.reporter?.clearData()
    }
}
