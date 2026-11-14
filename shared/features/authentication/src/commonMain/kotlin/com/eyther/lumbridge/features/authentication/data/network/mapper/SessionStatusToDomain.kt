package com.eyther.lumbridge.features.authentication.data.network.mapper

import com.eyther.lumbridge.features.authentication.domain.model.AuthSessionDomain
import io.github.jan.supabase.auth.status.SessionStatus

/** Backgrounding suspends refresh; it must not sign the user out or discard their current flow. */
internal fun SessionStatus.toDomain(previous: AuthSessionDomain?): AuthSessionDomain? =
    when (this) {
        is SessionStatus.Authenticated ->
            session.user?.let { user ->
                AuthSessionDomain(userId = user.id, email = user.email.orEmpty())
            }
        is SessionStatus.NotAuthenticated -> null
        is SessionStatus.Initializing,
        is SessionStatus.RefreshFailure -> previous
    }
