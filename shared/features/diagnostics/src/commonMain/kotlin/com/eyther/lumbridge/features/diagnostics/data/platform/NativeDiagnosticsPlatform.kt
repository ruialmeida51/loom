package com.eyther.lumbridge.features.diagnostics.data.platform

internal expect object NativeDiagnosticsPlatform : DiagnosticsPlatform {
    override val available: Boolean

    override fun setCollectionEnabled(enabled: Boolean)

    override suspend fun deleteUnsentReports()
}
