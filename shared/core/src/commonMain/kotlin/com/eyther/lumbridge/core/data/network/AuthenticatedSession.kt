package com.eyther.lumbridge.core.data.network

import com.eyther.lumbridge.core.data.network.mapper.toAppError
import com.eyther.lumbridge.core.domain.errors.CommonError
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Waits through SDK initialization and recoverable refresh failures before a scoped read. The SDK
 * owns refresh rotation and retry; a confirmed sign-out still fails immediately.
 */
internal suspend fun SupabaseClient.awaitAuthenticatedSession() {
    val status =
        withTimeoutOrNull(15_000L) {
            auth.awaitInitialization()
            auth.sessionStatus.first { status ->
                status is SessionStatus.NotAuthenticated ||
                    (status is SessionStatus.Authenticated &&
                        status.session.expiresAt > Clock.System.now() + 5.seconds)
            }
        } ?: throw CommonError.NoConnection()
    if (status is SessionStatus.NotAuthenticated) throw CommonError.NotAuthenticated()
}

/** Repairs the narrow race where SDK refresh fails between the readiness check and transport. */
internal suspend fun <T> SupabaseClient.readAuthenticated(action: suspend () -> T): T {
    awaitAuthenticatedSession()
    val userId = auth.currentUserOrNull()?.id
    return try {
        action()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (cause: Exception) {
        val status = auth.sessionStatus.value
        val error = cause.toAppError()
        val recovering =
            status is SessionStatus.RefreshFailure || status is SessionStatus.Initializing
        if (
            !recovering ||
                (error !is CommonError.NotAuthenticated && error !is CommonError.AccessDenied)
        )
            throw cause
        awaitAuthenticatedSession()
        if (auth.currentUserOrNull()?.id != userId)
            throw CommonError.NotAuthenticated(cause = cause)
        action()
    }
}
