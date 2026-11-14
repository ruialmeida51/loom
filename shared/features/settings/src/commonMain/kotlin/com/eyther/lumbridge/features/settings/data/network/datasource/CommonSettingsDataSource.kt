package com.eyther.lumbridge.features.settings.data.network.datasource

import com.eyther.lumbridge.core.data.local.PlatformStorage
import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import com.eyther.lumbridge.features.settings.data.network.model.request.PushDeviceRequest
import com.eyther.lumbridge.features.settings.data.network.model.request.RegisterPushDeviceRequest
import com.eyther.lumbridge.features.settings.data.notifications.PushPlatform
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CommonSettingsDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : SettingsDataSource {

    private val mutablePreferences = MutableStateFlow(
        SettingsDomain(
            theme = ThemePreferences.mode.value,
            notifications = PlatformStorage.get("notifications-${context.userId}") == "true",
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

    override suspend fun setNotifications(enabled: Boolean): AppResult<Unit> {
        return safeApiCall {
            val deviceId = getDeviceId()

            if (enabled) {
                val (token, platform) = PushPlatform.requestToken()
                val request = RegisterPushDeviceRequest(
                    deviceId = deviceId,
                    token = token,
                    platform = platform,
                )

                access.requireClient().postgrest.rpc("register_push_device", request)
            } else {
                val request = PushDeviceRequest(deviceId = deviceId)

                access.requireClient().postgrest.rpc("disable_notifications", request)
            }

            PlatformStorage.put("notifications-${context.userId}", enabled.toString())
            mutablePreferences.update { oldPreferences ->
                oldPreferences.copy(notifications = enabled)
            }
        }
    }

    private fun getDeviceId(): String {
        val key = "device-${context.userId}"
        val existing = PlatformStorage.get(key)
        if (existing != null) return existing

        val id = newId()
        PlatformStorage.put(key, id)
        return id
    }
}
