package com.eyther.lumbridge.features.analytics.data.local

import com.eyther.lumbridge.core.data.local.PlatformStorage

internal class CommonAnalyticsConsentStorage : AnalyticsConsentStorage {

    override fun get(): Boolean = PlatformStorage.get("analytics-consent") == "true"

    override fun set(enabled: Boolean) {
        PlatformStorage.put("analytics-consent", enabled.toString())
    }
}
