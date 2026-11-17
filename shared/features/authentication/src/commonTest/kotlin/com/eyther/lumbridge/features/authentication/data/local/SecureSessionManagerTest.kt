package com.eyther.lumbridge.features.authentication.data.local

import com.eyther.lumbridge.core.data.local.PlatformStorage
import io.github.jan.supabase.auth.user.UserSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.test.runTest

class SecureSessionManagerTest {
    @Test
    fun aStoredExpiredSessionRemainsExpiredWhenRestored() = runTest {
        val manager = SecureSessionManager()
        val expiry = Clock.System.now() - 2.hours
        try {
            manager.saveSession(
                UserSession(
                    accessToken = "test-access",
                    refreshToken = "test-refresh",
                    expiresIn = 3600,
                    tokenType = "Bearer",
                    expiresAt = expiry,
                ),
            )
            val restored = SecureSessionManager().loadSession()
            assertEquals(expiry, restored.expiresAt)
            assertEquals("test-refresh", restored.refreshToken)
            assertTrue(restored.expiresAt < Clock.System.now())
        } finally {
            manager.deleteSession()
        }
    }

    @Test
    fun aLegacySessionWithoutAbsoluteExpiryRequiresSdkRefresh() = runTest {
        val manager = SecureSessionManager()
        try {
            PlatformStorage.put(
                "session",
                """{"access_token":"test-access","refresh_token":"test-refresh","expires_in":3600,"token_type":"Bearer"}""",
            )
            val restored = assertNotNull(manager.loadSessionOrNull())
            assertEquals("test-refresh", restored.refreshToken)
            assertTrue(
                restored.expiresAt < Clock.System.now(),
                "A missing expiry must not renew the old access token locally",
            )
        } finally {
            manager.deleteSession()
        }
    }

    @Test
    fun malformedStorageIsNotAnAuthenticatedSession() = runTest {
        val manager = SecureSessionManager()
        try {
            PlatformStorage.put("session", "{broken")
            assertNull(manager.loadSessionOrNull())
        } finally {
            manager.deleteSession()
        }
    }
}
