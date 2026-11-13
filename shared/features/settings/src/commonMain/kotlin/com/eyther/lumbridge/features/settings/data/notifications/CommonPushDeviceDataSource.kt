package com.eyther.lumbridge.features.settings.data.notifications

import com.eyther.lumbridge.core.data.PlatformStorage
import com.eyther.lumbridge.core.data.SupabaseAccess
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.features.settings.domain.datasource.PushDeviceDataSource
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CommonPushDeviceDataSource(private val access: SupabaseAccess) : PushDeviceDataSource {
    private val pushScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        NativePushBridge.onTokenChanged = { pushScope.launch { syncRegistration() } }
    }

    override suspend fun syncRegistration() {
        val user = access.client?.auth?.currentUserOrNull()?.id ?: return
        if (PlatformStorage.get("notifications-$user") != "true") return
        val (token, platform) = NativePushBridge.currentToken() ?: return
        val key = "device-$user"
        val id = PlatformStorage.get(key) ?: newId().also { PlatformStorage.put(key, it) }
        try {
            access
                .requireClient()
                .postgrest
                .rpc(
                    "register_push_device",
                    buildJsonObject {
                        put("p_device_id", id)
                        put("p_token", token)
                        put("p_platform", platform)
                    },
                )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            /* Retried on the next session refresh or token rotation. */
        }
    }

    override suspend fun unregisterCurrentDevice() {
        val user = access.client?.auth?.currentUserOrNull()?.id ?: return
        val id = PlatformStorage.get("device-$user") ?: return
        access
            .requireClient()
            .postgrest
            .rpc("unregister_push_device", buildJsonObject { put("p_device_id", id) })
    }
}
