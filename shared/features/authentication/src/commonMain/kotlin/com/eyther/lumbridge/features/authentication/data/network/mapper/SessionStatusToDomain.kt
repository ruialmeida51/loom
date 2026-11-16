package com.eyther.lumbridge.features.authentication.data.network.mapper

import com.eyther.lumbridge.features.authentication.domain.model.AuthSessionDomain
import com.eyther.lumbridge.features.authentication.domain.model.AuthenticationStateDomain
import io.github.jan.supabase.auth.status.SessionStatus

/** SDK lifecycle pauses and recoverable refresh failures must retain an established identity. */
internal fun SessionStatus.toDomain(
    previous: AuthenticationStateDomain,
): AuthenticationStateDomain =
    when (this) {
        is SessionStatus.Authenticated ->
            session.user?.let { user ->
                AuthenticationStateDomain.Authenticated(
                    session = AuthSessionDomain(userId = user.id, email = user.email.orEmpty()),
                )
            } ?: AuthenticationStateDomain.SignedOut
        is SessionStatus.NotAuthenticated -> AuthenticationStateDomain.SignedOut
        is SessionStatus.Initializing -> previous
        is SessionStatus.RefreshFailure ->
            if (previous is AuthenticationStateDomain.Restoring)
                AuthenticationStateDomain.Restoring(reconnecting = true)
            else previous
    }
