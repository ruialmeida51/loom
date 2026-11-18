package com.eyther.lumbridge.features.analytics.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsPreferencesDomain
import kotlinx.coroutines.flow.StateFlow

interface AnalyticsDataSource {

    fun stream(): StateFlow<AnalyticsPreferencesDomain>

    /** Restore saved device consent once; reopening preferences must preserve the SDK state. */
    suspend fun initialize(): AppResult<Unit>

    suspend fun setConsent(enabled: Boolean): AppResult<Unit>

    suspend fun track(event: AnalyticsEventDomain): AppResult<Unit>
}
