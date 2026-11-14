package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsPreferencesDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DemoAnalyticsDataSource : AnalyticsDataSource {

    private val preferences = MutableStateFlow(AnalyticsPreferencesDomain(available = true))

    override fun stream(): StateFlow<AnalyticsPreferencesDomain> = preferences.asStateFlow()

    override suspend fun initialize(): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun setConsent(enabled: Boolean): AppResult<Unit> {
        preferences.value = preferences.value.copy(enabled = enabled)
        return AppResult.Success(Unit)
    }

    override suspend fun track(event: AnalyticsEventDomain): AppResult<Unit> = AppResult.Success(Unit)
}
