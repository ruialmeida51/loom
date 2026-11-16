package com.eyther.lumbridge.features.authentication.data.network.mapper

import com.eyther.lumbridge.features.authentication.data.fixtures.sessionResponseFixture
import com.eyther.lumbridge.features.authentication.domain.model.AuthenticationStateDomain
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.serialization.json.Json

class SessionStatusToDomainTest {
    @Test
    fun coldStartDoesNotExposeLoginBeforeStorageOrExpiredTokenRefreshResolves() {
        val initial = AuthenticationStateDomain.Restoring()
        val loading = SessionStatus.Initializing.toDomain(previous = initial)
        assertIs<AuthenticationStateDomain.Restoring>(loading)
        val retrying =
            SessionStatus.RefreshFailure(RefreshFailureCause.NetworkError(Exception()))
                .toDomain(previous = loading)
        assertEquals(AuthenticationStateDomain.Restoring(reconnecting = true), retrying)
        val restored =
            SessionStatus.Authenticated(
                    session = Json.decodeFromString<UserSession>(sessionResponseFixture()),
                )
                .toDomain(previous = retrying)
        assertIs<AuthenticationStateDomain.Authenticated>(restored)
        assertEquals(
            Json.decodeFromString<UserSession>(sessionResponseFixture()).user?.id,
            restored.session.userId,
        )
    }

    @Test
    fun backgroundAndRecoverableRefreshFailureKeepIdentityUntilConfirmedSignOut() {
        val authenticated =
            SessionStatus.Authenticated(
                    session = Json.decodeFromString<UserSession>(sessionResponseFixture()),
                )
                .toDomain(previous = AuthenticationStateDomain.Restoring())
        val background = SessionStatus.Initializing.toDomain(previous = authenticated)
        assertEquals(authenticated, background)
        val retrying =
            SessionStatus.RefreshFailure(RefreshFailureCause.NetworkError(Exception()))
                .toDomain(previous = background)
        assertEquals(authenticated, retrying)
        assertEquals(
            AuthenticationStateDomain.SignedOut,
            SessionStatus.NotAuthenticated(isSignOut = true).toDomain(previous = retrying),
        )
        assertEquals(
            AuthenticationStateDomain.SignedOut,
            SessionStatus.NotAuthenticated().toDomain(previous = authenticated),
        )
    }

    @Test
    fun noStoredSessionAndActualSignOutBothResolveTheStartupState() {
        assertEquals(
            AuthenticationStateDomain.SignedOut,
            SessionStatus.NotAuthenticated()
                .toDomain(previous = AuthenticationStateDomain.Restoring()),
        )
        assertEquals(
            AuthenticationStateDomain.SignedOut,
            SessionStatus.Initializing.toDomain(previous = AuthenticationStateDomain.SignedOut),
        )
    }
}
