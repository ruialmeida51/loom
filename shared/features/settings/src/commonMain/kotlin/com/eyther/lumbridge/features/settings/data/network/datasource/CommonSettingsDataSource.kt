package com.eyther.lumbridge.features.settings.data.network.datasource

import com.eyther.lumbridge.core.data.local.PlatformStorage
import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.AppLanguage
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.settings.data.local.LanguagePreferences
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import com.eyther.lumbridge.features.settings.data.network.model.request.PushDeviceRequest
import com.eyther.lumbridge.features.settings.data.network.model.request.RegisterPushDeviceRequest
import com.eyther.lumbridge.features.settings.data.notifications.PushPlatform
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.model.NotificationPermissionDomain
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CommonSettingsDataSource(
    private val access: SupabaseAccess,
    private val userId: String,
) : SettingsDataSource {

    private val mutablePreferences =
        MutableStateFlow(
            SettingsDomain(
                theme = ThemePreferences.mode.value,
                language = LanguagePreferences.language.value,
                notifications = PlatformStorage.get("notifications-${userId}") == "true",
                notificationPrimeAnswered =
                    PlatformStorage.get("notification-prime-${userId}") == "true",
            ),
        )

    override fun stream(): StateFlow<SettingsDomain> = mutablePreferences.asStateFlow()

    override suspend fun setTheme(value: String): AppResult<Unit> {
        return safeApiCall {
            ThemePreferences.set(value)
            mutablePreferences.update { oldPreferences ->
                oldPreferences.copy(theme = value)
            }
        }
    }

    override suspend fun setLanguage(language: AppLanguage): AppResult<Unit> {
        return safeApiCall {
            LanguagePreferences.set(language)
            mutablePreferences.update { oldPreferences ->
                oldPreferences.copy(language = language)
            }
        }
    }

    override suspend fun setNotifications(enabled: Boolean): AppResult<Unit> {
        return safeApiCall {
            val deviceId = getDeviceId()

            if (enabled) {
                val (token, platform) = PushPlatform.requestToken()
                val request =
                    RegisterPushDeviceRequest(
                        deviceId = deviceId,
                        token = token,
                        platform = platform,
                    )

                access.requireClient().postgrest.rpc("register_push_device", request)
            } else {
                val request = PushDeviceRequest(deviceId = deviceId)

                access.requireClient().postgrest.rpc("disable_notifications", request)
            }

            PlatformStorage.put("notifications-${userId}", enabled.toString())
            mutablePreferences.update { oldPreferences ->
                oldPreferences.copy(notifications = enabled)
            }
        }
    }

    override suspend fun getNotificationPermission() = safeApiCall {
        val permission = PushPlatform.getPermission()
        if (
            permission == NotificationPermissionDomain.DENIED ||
                permission == NotificationPermissionDomain.UNAVAILABLE
        ) {
            mutablePreferences.update { oldPreferences ->
                oldPreferences.copy(notifications = false)
            }
        }
        permission
    }

    override suspend fun markNotificationPrimeAnswered(): AppResult<Unit> = safeApiCall {
        PlatformStorage.put("notification-prime-${userId}", "true")
        mutablePreferences.update { oldPreferences ->
            oldPreferences.copy(notificationPrimeAnswered = true)
        }
    }

    override suspend fun openNotificationSettings(): AppResult<Unit> = safeApiCall {
        PushPlatform.openSettings()
    }

    private fun getDeviceId(): String {
        val key = "device-${userId}"
        val existing = PlatformStorage.get(key)
        if (existing != null) return existing

        val id = newId()
        PlatformStorage.put(key, id)
        return id
    }
}
