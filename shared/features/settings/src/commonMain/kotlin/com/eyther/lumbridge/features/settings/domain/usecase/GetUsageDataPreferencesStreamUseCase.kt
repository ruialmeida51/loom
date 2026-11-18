package com.eyther.lumbridge.features.settings.domain.usecase

import com.eyther.lumbridge.features.settings.domain.datasource.UsageDataConsentDataSource
import com.eyther.lumbridge.features.settings.domain.model.UsageDataPreferencesDomain
import kotlinx.coroutines.flow.Flow

class GetUsageDataPreferencesStreamUseCase(private val dataSource: UsageDataConsentDataSource) {
    operator fun invoke(): Flow<UsageDataPreferencesDomain> = dataSource.stream()
}
