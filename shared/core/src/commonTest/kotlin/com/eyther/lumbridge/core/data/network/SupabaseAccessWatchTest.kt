package com.eyther.lumbridge.core.data.network

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.ktor.client.engine.mock.MockEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(SupabaseInternal::class, ExperimentalCoroutinesApi::class)
class SupabaseAccessWatchTest {
    @Test
    fun streamsLoadOnceAndReloadOnlyAfterThisAppsSuccessfulWrites() = runTest {
        val client =
            createSupabaseClient("https://loom.test", "test-key") {
                install(Auth) {
                    autoLoadFromStorage = false
                    autoSaveToStorage = false
                }
                httpEngine = MockEngine { error("No network traffic is expected") }
            }
        try {
            client.auth.awaitInitialization()
            client.auth.setSessionStatus(
                SessionStatus.Authenticated(
                    UserSession(
                        accessToken = "access",
                        refreshToken = "refresh",
                        expiresIn = 3600,
                        tokenType = "Bearer",
                        expiresAt = Clock.System.now() + 1.hours,
                    ),
                ),
            )
            val access = SupabaseAccess(client)
            var loads = 0
            val collection = backgroundScope.launch { access.watch { ++loads }.collect() }
            runCurrent()
            assertEquals(1, loads, "A screen opening loads once")
            advanceTimeBy(10.minutes)
            runCurrent()
            assertEquals(1, loads, "Nothing polls or subscribes while the screen stays open")
            access.write { "saved" }
            runCurrent()
            assertEquals(2, loads, "The app's own write reloads open streams once")
            assertFailsWith<IllegalStateException> { access.write { error("Rejected") } }
            runCurrent()
            assertEquals(2, loads, "A failed write changes nothing, so nothing reloads")
            collection.cancel()
        } finally {
            client.close()
        }
    }
}
