package com.eyther.lumbridge.features.settings.domain.datasource

import com.eyther.lumbridge.core.domain.AppLanguage
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.settings.domain.model.NotificationPermissionDomain
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import kotlinx.coroutines.flow.StateFlow

interface SettingsDataSource {
    fun stream(): StateFlow<SettingsDomain>

    /** The app's language, which the phone's own settings can also change. */
    fun streamLanguage(): StateFlow<AppLanguage>

    suspend fun setTheme(value: String): AppResult<Unit>

    suspend fun setLanguage(language: AppLanguage): AppResult<Unit>

    suspend fun setNotifications(enabled: Boolean): AppResult<Unit>

    suspend fun getNotificationPermission(): AppResult<NotificationPermissionDomain>

    suspend fun markNotificationPrimeAnswered(): AppResult<Unit>

    suspend fun openNotificationSettings(): AppResult<Unit>
}
