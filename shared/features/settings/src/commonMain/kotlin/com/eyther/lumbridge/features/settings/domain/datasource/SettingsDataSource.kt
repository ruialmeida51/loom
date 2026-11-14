package com.eyther.lumbridge.features.settings.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import kotlinx.coroutines.flow.StateFlow

interface SettingsDataSource {
    fun stream(): StateFlow<SettingsDomain>

    suspend fun setTheme(value: String): AppResult<Unit>

    suspend fun setNotifications(enabled: Boolean): AppResult<Unit>
}
