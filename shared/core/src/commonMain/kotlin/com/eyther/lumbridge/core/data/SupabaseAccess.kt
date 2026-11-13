package com.eyther.lumbridge.core.data

import com.eyther.lumbridge.core.domain.newId
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SupabaseAccess(sessionStorage: SessionManager) {
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
                install(Auth) { sessionManager = sessionStorage }
                install(Postgrest)
                install(Realtime)
                install(Functions)
            }

    fun requireClient(): SupabaseClient =
        checkNotNull(client) { "core.message_sign_in_is_not_available_in_this_preview" }

    fun userId(): String =
        requireClient().auth.currentUserOrNull()?.id ?: error("core.message_please_sign_in_again")

    /**
     * RLS authorizes rows. Realtime invalidates this query; periodic reload also repairs missed
     * events.
     */
    fun <T> watch(table: String, scopeId: String, load: suspend () -> T): Flow<T> = channelFlow {
        val client = requireClient()
        send(load())
        val channel =
            client.channel(
                "loom-$table-$scopeId-${newId()}",
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
