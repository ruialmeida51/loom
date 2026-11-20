package com.eyther.lumbridge.core.data.network

import com.eyther.lumbridge.core.domain.errors.CommonError
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class SupabaseAccess(
    val client: SupabaseClient?,
) {

    val configured: Boolean = client != null

    fun requireClient(): SupabaseClient = client ?: throw CommonError.ServiceUnavailable()

    /** Identity lookups wait for the same lifecycle/token recovery as authenticated reads. */
    suspend fun userId(): String = read { client ->
        client.auth.currentUserOrNull()?.id ?: throw CommonError.NotAuthenticated()
    }

    /**
     * A scoped read waits for SDK recovery; only the existing narrow read-recovery race may retry.
     */
    suspend fun <T> read(action: suspend (SupabaseClient) -> T): T {
        val client = requireClient()
        return client.readAuthenticated { action(client) }
    }

    /**
     * Writes made through this app. Each one reloads the streams that are currently open once,
     * so a list reflects changes saved on another screen without live server subscriptions.
     */
    private val localWrites =
        MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /** Runs a server write; on success every open [watch] reloads once. */
    suspend fun <T> write(action: suspend (SupabaseClient) -> T): T =
        action(requireClient()).also { localWrites.tryEmit(Unit) }

    /**
     * RLS authorizes rows. Loads when collection starts (a screen opening or a pull-to-refresh
     * re-collecting) and after this app's own writes. There are no live server subscriptions or
     * polling: other members' changes appear on the next screen load or refresh.
     */
    fun <T> watch(load: suspend () -> T): Flow<T> = channelFlow {
        val client = requireClient()
        val reloads = Channel<Unit>(capacity = Channel.CONFLATED)
        val listener = launch { localWrites.collect { reloads.trySend(Unit) } }
        try {
            send(client.readAuthenticated(action = load))
            // One reader serializes reloads; a slower older response cannot replace a newer one.
            for (reload in reloads) send(client.readAuthenticated(action = load))
        } finally {
            listener.cancel()
        }
    }
        .distinctUntilChanged()
}
