package com.eyther.lumbridge.core.data

import com.eyther.lumbridge.core.domain.newId
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.NoSessionFoundException
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseAccess {
    private val pushScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val configured =
        LoomBuildConfig.supabaseUrl.isNotBlank() &&
            LoomBuildConfig.supabasePublishableKey.isNotBlank()
    val client: SupabaseClient? =
        if (!configured) null
        else
            createSupabaseClient(
                supabaseUrl = LoomBuildConfig.supabaseUrl,
                supabaseKey = LoomBuildConfig.supabasePublishableKey,
            ) {
                install(Auth) { sessionManager = SecureSessionManager() }
                install(Postgrest)
                install(Realtime)
                install(Functions)
            }

    fun requireClient(): SupabaseClient =
        checkNotNull(client) { "Sign-in is not available in this preview." }

    fun userId(): String =
        requireClient().auth.currentUserOrNull()?.id ?: error("Please sign in again.")

    init {
        NativePushBridge.onTokenChanged = { pushScope.launch { syncPushRegistration() } }
    }

    suspend fun syncPushRegistration() {
        val user = client?.auth?.currentUserOrNull()?.id ?: return
        if (PlatformStorage.get("notifications-$user") != "true") return
        val (token, platform) = NativePushBridge.currentToken() ?: return
        val key = "device-$user"
        val id = PlatformStorage.get(key) ?: newId().also { PlatformStorage.put(key, it) }
        try {
            requireClient()
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

    suspend fun unregisterCurrentPushDevice() {
        val user = client?.auth?.currentUserOrNull()?.id ?: return
        val id = PlatformStorage.get("device-$user") ?: return
        requireClient()
            .postgrest
            .rpc("unregister_push_device", buildJsonObject { put("p_device_id", id) })
    }

    /**
     * RLS authorizes rows. Realtime invalidates this query; periodic reload also repairs missed
     * events.
     */
    fun <T> watch(table: String, householdId: String, load: suspend () -> T): Flow<T> =
        channelFlow {
            val client = requireClient()
            send(load())
            val channel =
                client.channel(
                    "loom-$table-$householdId-${com.eyther.lumbridge.core.domain.newId()}",
                )
            val changes =
                channel.postgresChangeFlow<PostgresAction>(schema = "public") { this.table = table }
            val listener = launch { changes.collect { send(load()) } }
            try {
                try {
                    channel.subscribe()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    /* Polling still refreshes authoritative data. */
                }
                while (isActive) {
                    delay(30_000)
                    send(load())
                }
            } finally {
                listener.cancel()
                channel.unsubscribe()
            }
        }
}

private class SecureSessionManager : SessionManager {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun saveSession(session: UserSession) =
        PlatformStorage.put("session", json.encodeToString(session))

    override suspend fun loadSession(): UserSession =
        loadSessionOrNull() ?: throw NoSessionFoundException()

    override suspend fun loadSessionOrNull(): UserSession? =
        PlatformStorage.get("session")?.let { value ->
            runCatching { json.decodeFromString<UserSession>(value) }.getOrNull()
        }

    override suspend fun deleteSession() = PlatformStorage.put("session", null)
}
