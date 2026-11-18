package com.eyther.lumbridge.features.settings.data.local

import com.eyther.lumbridge.core.data.local.PlatformStorage

internal class CommonUsageDataConsentStorage : UsageDataConsentStorage {
    override fun isAnswered(): Boolean = PlatformStorage.get("usage-data-consent-v1") == "answered"

    override fun markAnswered() = PlatformStorage.put("usage-data-consent-v1", "answered")
}
