package com.eyther.lumbridge.features.diagnostics.data.platform

internal actual object NativeDiagnosticsPlatform : DiagnosticsPlatform {
    actual override val available: Boolean
        get() = NativeDiagnosticsBridge.reporter != null

    actual override fun setCollectionEnabled(enabled: Boolean) {
        NativeDiagnosticsBridge.reporter?.setCollectionEnabled(enabled = enabled)
    }

    actual override suspend fun deleteUnsentReports() {
        NativeDiagnosticsBridge.reporter?.deleteUnsentReports()
    }
}
