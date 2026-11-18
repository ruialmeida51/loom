package com.eyther.lumbridge.features.diagnostics.data.local

internal interface DiagnosticsConsentStorage {
    fun get(): Boolean

    fun set(enabled: Boolean)
}
