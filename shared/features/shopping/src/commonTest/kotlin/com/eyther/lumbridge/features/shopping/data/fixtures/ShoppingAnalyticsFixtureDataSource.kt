package com.eyther.lumbridge.features.shopping.data.fixtures

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsPreferencesDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ShoppingAnalyticsFixtureDataSource : AnalyticsDataSource {
    override fun stream(): StateFlow<AnalyticsPreferencesDomain> =
        MutableStateFlow(AnalyticsPreferencesDomain())

    override suspend fun initialize(): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun setConsent(enabled: Boolean): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun track(event: AnalyticsEventDomain): AppResult<Unit> =
        AppResult.Success(Unit)
}
