package com.eyther.lumbridge.features.diagnostics.data.platform

internal interface DiagnosticsPlatform {
    val available: Boolean

    fun setCollectionEnabled(enabled: Boolean)

    suspend fun deleteUnsentReports()
}
