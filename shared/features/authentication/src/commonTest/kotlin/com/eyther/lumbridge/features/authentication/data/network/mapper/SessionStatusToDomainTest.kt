package com.eyther.lumbridge.features.authentication.data.network.mapper

import com.eyther.lumbridge.features.authentication.data.fixtures.sessionResponseFixture
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

class SessionStatusToDomainTest {

    @Test
    fun backgroundResumeAndRefreshRetryKeepTheIdentityUntilAnActualSignOut() {
        val authenticated =
            SessionStatus.Authenticated(
                session = Json.decodeFromString<UserSession>(sessionResponseFixture()),
            )
        val session = assertNotNull(authenticated.toDomain(previous = null))
        // The Android SDK sends Initializing from ProcessLifecycleOwner.onStop.
        val background = SessionStatus.Initializing.toDomain(previous = session)
        assertEquals(session, background)
        val retry =
            SessionStatus.RefreshFailure(RefreshFailureCause.NetworkError(Exception()))
                .toDomain(previous = background)
        assertEquals(session, retry)
        assertEquals(session, authenticated.toDomain(previous = retry))
        assertNull(SessionStatus.NotAuthenticated(isSignOut = true).toDomain(previous = session))
        assertNull(SessionStatus.NotAuthenticated().toDomain(previous = session))
    }

    @Test
    fun initializationDoesNotInventASessionOnAColdStart() {
        assertNull(SessionStatus.Initializing.toDomain(previous = null))
        assertNull(
            SessionStatus.RefreshFailure(RefreshFailureCause.NetworkError(Exception()))
                .toDomain(previous = null),
        )
    }
}
