package com.eyther.lumbridge.features.settings.data.local

internal interface UsageDataConsentStorage {
    fun isAnswered(): Boolean

    fun markAnswered()
}
