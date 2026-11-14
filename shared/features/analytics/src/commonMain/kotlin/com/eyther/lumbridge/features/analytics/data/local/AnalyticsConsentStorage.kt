package com.eyther.lumbridge.features.analytics.data.local

internal interface AnalyticsConsentStorage {

    fun get(): Boolean

    fun set(enabled: Boolean)
}
