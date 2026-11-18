package com.eyther.lumbridge.features.settings.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.settings.domain.datasource.UsageDataConsentDataSource

class InitializeUsageDataConsentUseCase(private val dataSource: UsageDataConsentDataSource) {
    suspend operator fun invoke(): AppResult<Unit> = dataSource.initialize()
}
