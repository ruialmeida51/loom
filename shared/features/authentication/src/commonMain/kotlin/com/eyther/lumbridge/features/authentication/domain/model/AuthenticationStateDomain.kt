package com.eyther.lumbridge.features.authentication.domain.model

/** Session restoration is distinct from a confirmed sign-out, including while refresh retries. */
sealed interface AuthenticationStateDomain {
    data class Restoring(val reconnecting: Boolean = false) : AuthenticationStateDomain

    data class Authenticated(val session: AuthSessionDomain) : AuthenticationStateDomain

    data object SignedOut : AuthenticationStateDomain
}
