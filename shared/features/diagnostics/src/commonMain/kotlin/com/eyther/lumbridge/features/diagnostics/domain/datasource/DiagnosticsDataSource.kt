package com.eyther.lumbridge.features.diagnostics.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.diagnostics.domain.model.DiagnosticsPreferencesDomain
import kotlinx.coroutines.flow.StateFlow

interface DiagnosticsDataSource {
    fun stream(): StateFlow<DiagnosticsPreferencesDomain>

    /** Restore saved device consent once; reopening preferences must preserve the SDK state. */
    suspend fun initialize(): AppResult<Unit>

    suspend fun setConsent(enabled: Boolean): AppResult<Unit>
}
