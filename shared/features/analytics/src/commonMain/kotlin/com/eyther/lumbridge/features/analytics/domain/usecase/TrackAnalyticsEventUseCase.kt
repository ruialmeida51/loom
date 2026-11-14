package com.eyther.lumbridge.features.analytics.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain

class TrackAnalyticsEventUseCase(
    private val analyticsDataSource: AnalyticsDataSource,
) {

    suspend operator fun invoke(event: AnalyticsEventDomain): AppResult<Unit> = analyticsDataSource.track(event = event)
}
