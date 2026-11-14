package com.eyther.lumbridge.features.settings.data.notifications

import com.eyther.lumbridge.core.data.local.PlatformStorage
import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.settings.data.network.model.request.PushDeviceRequest
import com.eyther.lumbridge.features.settings.data.network.model.request.RegisterPushDeviceRequest
import com.eyther.lumbridge.features.settings.domain.datasource.PushDeviceDataSource
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CommonPushDeviceDataSource(
    private val access: SupabaseAccess,
) : PushDeviceDataSource {

    private val pushScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        NativePushBridge.onTokenChanged = {
            pushScope.launch { syncRegistration() }
        }
    }

    override suspend fun syncRegistration(): AppResult<Unit> = safeApiCall {
        val userId = access.client?.auth?.currentUserOrNull()?.id ?: return@safeApiCall
        if (PlatformStorage.get("notifications-$userId") != "true") return@safeApiCall
        val (token, platform) = NativePushBridge.currentToken() ?: return@safeApiCall
        val key = "device-$userId"
        val deviceId = PlatformStorage.get(key) ?: newId().also { PlatformStorage.put(key, it) }
        val request = RegisterPushDeviceRequest(
            deviceId = deviceId,
            token = token,
            platform = platform,
        )

        access.requireClient().postgrest.rpc("register_push_device", request)
        Unit
    }

    override suspend fun unregisterCurrentDevice(): AppResult<Unit> = safeApiCall {
        val userId = access.client?.auth?.currentUserOrNull()?.id ?: return@safeApiCall
        val deviceId = PlatformStorage.get("device-$userId") ?: return@safeApiCall
        val request = PushDeviceRequest(deviceId = deviceId)

        access.requireClient().postgrest.rpc("unregister_push_device", request)
        Unit
    }
}
