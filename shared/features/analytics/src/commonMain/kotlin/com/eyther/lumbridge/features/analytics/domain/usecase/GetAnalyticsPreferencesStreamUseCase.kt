package com.eyther.lumbridge.features.analytics.domain.usecase

import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsPreferencesDomain
import kotlinx.coroutines.flow.StateFlow

class GetAnalyticsPreferencesStreamUseCase(
    private val analyticsDataSource: AnalyticsDataSource,
) {

    operator fun invoke(): StateFlow<AnalyticsPreferencesDomain> = analyticsDataSource.stream()
}
