package com.eyther.lumbridge.features.settings.domain.datasource

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import kotlinx.coroutines.flow.StateFlow

interface SettingsDataSource {
    val preferences: StateFlow<SettingsDomain>

    suspend fun theme(value: String): AppResult<Unit>

    suspend fun notifications(enabled: Boolean): AppResult<Unit>
}
