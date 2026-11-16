package com.eyther.lumbridge.core.data.network

import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.newId
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SupabaseAccess(
    val client: SupabaseClient?,
) {

    val configured: Boolean = client != null

    fun requireClient(): SupabaseClient = client ?: throw CommonError.ServiceUnavailable()

    fun userId(): String =
        requireClient().auth.currentUserOrNull()?.id ?: throw CommonError.NotAuthenticated()

    /**
     * RLS authorizes rows. Realtime invalidates this query; periodic reload also repairs missed
     * events.
     */
    fun <T> watch(table: String, scopeId: String, load: suspend () -> T): Flow<T> = channelFlow {
        val client = requireClient()
        send(load())
        val invalidations = Channel<Unit>(capacity = Channel.CONFLATED)
        val channel = client.channel("loom-$table-$scopeId-${newId()}")
        val changes =
            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                this.table = table
            }
        val listener = launch { changes.collect { invalidations.trySend(Unit) } }
        val subscription = launch {
            try {
                channel.subscribe()
                // Repair changes between the initial read and subscription becoming ready.
                invalidations.trySend(Unit)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Polling still refreshes authoritative data if realtime is unavailable.
            }
        }
        val poller = launch {
            while (isActive) {
                delay(30_000)
                invalidations.trySend(Unit)
            }
        }
        try {
            // One reader serializes reloads; a slower older response cannot replace a newer one.
            for (invalidation in invalidations) send(load())
        } finally {
            listener.cancel()
            subscription.cancel()
            poller.cancel()
            invalidations.close()
            channel.unsubscribe()
        }
    }
        .distinctUntilChanged()
}
