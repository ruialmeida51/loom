package com.eyther.lumbridge.features.diagnostics.data.local

import com.eyther.lumbridge.core.data.local.PlatformStorage

internal class CommonDiagnosticsConsentStorage : DiagnosticsConsentStorage {
    override fun get(): Boolean = PlatformStorage.get("crash-report-consent") == "true"

    override fun set(enabled: Boolean) =
        PlatformStorage.put("crash-report-consent", enabled.toString())
}
