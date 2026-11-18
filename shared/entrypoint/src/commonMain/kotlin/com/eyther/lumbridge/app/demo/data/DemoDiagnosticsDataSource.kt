package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.diagnostics.domain.datasource.DiagnosticsDataSource
import com.eyther.lumbridge.features.diagnostics.domain.model.DiagnosticsPreferencesDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DemoDiagnosticsDataSource : DiagnosticsDataSource {
    private val preferences = MutableStateFlow(DiagnosticsPreferencesDomain(available = true))

    override fun stream() = preferences.asStateFlow()

    override suspend fun initialize() = AppResult.Success(Unit)

    override suspend fun setConsent(enabled: Boolean): AppResult<Unit> {
        preferences.value = preferences.value.copy(enabled = enabled)
        return AppResult.Success(Unit)
    }
}
