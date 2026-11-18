package com.eyther.lumbridge.features.diagnostics.data.platform

internal actual object NativeDiagnosticsPlatform : DiagnosticsPlatform {
    actual override val available: Boolean = false

    actual override fun setCollectionEnabled(enabled: Boolean) = Unit

    actual override suspend fun deleteUnsentReports() = Unit
}
