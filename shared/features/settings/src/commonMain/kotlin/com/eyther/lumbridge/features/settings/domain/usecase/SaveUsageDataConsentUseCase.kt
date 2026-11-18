package com.eyther.lumbridge.features.settings.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.settings.domain.datasource.UsageDataConsentDataSource

class SaveUsageDataConsentUseCase(private val dataSource: UsageDataConsentDataSource) {
    suspend operator fun invoke(analytics: Boolean, crashReports: Boolean): AppResult<Unit> =
        dataSource.save(analytics = analytics, crashReports = crashReports)
}
