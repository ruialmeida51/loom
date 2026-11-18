package com.eyther.lumbridge.features.settings.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.settings.domain.model.UsageDataPreferencesDomain
import kotlinx.coroutines.flow.Flow

interface UsageDataConsentDataSource {
    fun stream(): Flow<UsageDataPreferencesDomain>

    suspend fun initialize(): AppResult<Unit>

    suspend fun save(analytics: Boolean, crashReports: Boolean): AppResult<Unit>
}
