package com.eyther.lumbridge.features.settings.data.network.datasource

import com.eyther.lumbridge.core.data.*
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import com.eyther.lumbridge.features.settings.data.notifications.PushPlatform
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*

class CommonSettingsDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : SettingsDataSource {
    private val mutable =
        MutableStateFlow(
            SettingsDomain(
                PlatformStorage.get("theme")?.takeIf { it in listOf("SYSTEM", "LIGHT", "DARK") }
                    ?: "SYSTEM",
                PlatformStorage.get("notifications-${context.userId}") == "true",
            ),
        )
    override val preferences = mutable.asStateFlow()

    override suspend fun theme(value: String) = safeApiCall {
        ThemePreferences.set(value)
        mutable.update { it.copy(theme = value) }
    }

    override suspend fun notifications(enabled: Boolean) = safeApiCall {
        val key = "device-${context.userId}"
        val deviceId = PlatformStorage.get(key) ?: newId().also { PlatformStorage.put(key, it) }
        if (enabled) {
            val (token, platform) = PushPlatform.requestToken()
            access
                .requireClient()
                .postgrest
                .rpc(
                    "register_push_device",
                    buildJsonObject {
                        put("p_device_id", deviceId)
                        put("p_token", token)
                        put("p_platform", platform)
                    },
                )
        } else
            access
                .requireClient()
                .postgrest
                .rpc("disable_notifications", buildJsonObject { put("p_device_id", deviceId) })
        PlatformStorage.put("notifications-${context.userId}", enabled.toString())
        mutable.update { it.copy(notifications = enabled) }
    }
}
