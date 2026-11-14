package com.eyther.lumbridge.features.analytics.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource

class SetAnalyticsConsentUseCase(
    private val analyticsDataSource: AnalyticsDataSource,
) {

    suspend operator fun invoke(enabled: Boolean): AppResult<Unit> = analyticsDataSource.setConsent(enabled = enabled)
}
