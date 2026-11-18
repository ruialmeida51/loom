package com.eyther.lumbridge.features.settings.data.fixtures

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.model.NotificationPermissionDomain
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

class NotificationFixtureDataSource : SettingsDataSource {
    private val preferences = MutableStateFlow(SettingsDomain())
    var permission = NotificationPermissionDomain.NOT_REQUESTED
    var registrationCalls = 0
    var settingsCalls = 0
    var registration =
        CompletableDeferred<AppResult<Unit>>().apply { complete(AppResult.Success(Unit)) }

    override fun stream() = preferences

    override suspend fun setTheme(value: String): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun getNotificationPermission() = AppResult.Success(permission)

    override suspend fun openNotificationSettings(): AppResult<Unit> {
        settingsCalls++
        return AppResult.Success(Unit)
    }

    override suspend fun markNotificationPrimeAnswered(): AppResult<Unit> {
        preferences.value = preferences.value.copy(notificationPrimeAnswered = true)
        return AppResult.Success(Unit)
    }

    override suspend fun setNotifications(enabled: Boolean): AppResult<Unit> {
        registrationCalls++
        val result = registration.await()
        if (result is AppResult.Success) {
            permission = NotificationPermissionDomain.GRANTED
            preferences.value = preferences.value.copy(notifications = enabled)
        }
        return result
    }
}
