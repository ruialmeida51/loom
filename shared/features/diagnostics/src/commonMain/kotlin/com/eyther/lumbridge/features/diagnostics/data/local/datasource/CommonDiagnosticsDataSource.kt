package com.eyther.lumbridge.features.diagnostics.data.local.datasource

import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.diagnostics.data.local.CommonDiagnosticsConsentStorage
import com.eyther.lumbridge.features.diagnostics.data.local.DiagnosticsConsentStorage
import com.eyther.lumbridge.features.diagnostics.data.platform.DiagnosticsPlatform
import com.eyther.lumbridge.features.diagnostics.data.platform.NativeDiagnosticsPlatform
import com.eyther.lumbridge.features.diagnostics.domain.datasource.DiagnosticsDataSource
import com.eyther.lumbridge.features.diagnostics.domain.errors.DiagnosticsError
import com.eyther.lumbridge.features.diagnostics.domain.model.DiagnosticsPreferencesDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Native defaults are off. Enabling never uploads reports cached during an opted-out period. */
class CommonDiagnosticsDataSource
internal constructor(
    private val platform: DiagnosticsPlatform,
    private val storage: DiagnosticsConsentStorage,
) : DiagnosticsDataSource {
    constructor() : this(NativeDiagnosticsPlatform, CommonDiagnosticsConsentStorage())

    private val mutex = Mutex()
    private val preferences =
        MutableStateFlow(DiagnosticsPreferencesDomain(available = platform.available))

    override fun stream() = preferences.asStateFlow()

    override suspend fun initialize(): AppResult<Unit> = safeApiCall {
        mutex.withLock {
            platform.setCollectionEnabled(enabled = false)
            val enabled = platform.available && storage.get()
            if (!enabled) platform.deleteUnsentReports()
            platform.setCollectionEnabled(enabled = enabled)
            preferences.value =
                DiagnosticsPreferencesDomain(available = platform.available, enabled = enabled)
        }
    }

    override suspend fun setConsent(enabled: Boolean): AppResult<Unit> = safeApiCall {
        mutex.withLock {
            requireDomain(!enabled || platform.available) { DiagnosticsError.Unavailable() }
            if (!enabled) {
                platform.setCollectionEnabled(enabled = false)
                preferences.value = DiagnosticsPreferencesDomain(available = platform.available)
            }
            if (!preferences.value.enabled || !enabled) platform.deleteUnsentReports()
            storage.set(enabled = enabled)
            platform.setCollectionEnabled(enabled = enabled)
            preferences.value =
                DiagnosticsPreferencesDomain(available = platform.available, enabled = enabled)
        }
    }
}
