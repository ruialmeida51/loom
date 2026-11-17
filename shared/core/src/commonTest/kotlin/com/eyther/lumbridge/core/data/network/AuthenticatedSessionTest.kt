package com.eyther.lumbridge.core.data.network

import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException

@OptIn(SupabaseInternal::class, ExperimentalCoroutinesApi::class)
class AuthenticatedSessionTest {
    @Test
    fun expiredSessionsWaitForTheSdkInsteadOfForcingConcurrentRefreshes() = runTest {
        val client =
            createSupabaseClient("https://loom.test", "test-key") {
                install(Auth) {
                    autoLoadFromStorage = false
                    autoSaveToStorage = false
                }
                httpEngine = MockEngine { error("The SDK owns token refresh, not the data reads") }
            }
        try {
            client.auth.awaitInitialization()
            client.auth.setSessionStatus(
                SessionStatus.Authenticated(
                    UserSession(
                        accessToken = "expired-token",
                        refreshToken = "refresh-token",
                        expiresIn = 3600,
                        tokenType = "Bearer",
                        expiresAt = Clock.System.now() - 1.seconds,
                    ),
                ),
            )
            var reads = 0
            val first = async { client.readAuthenticated { reads++ } }
            val second = async { client.readAuthenticated { reads++ } }
            runCurrent()
            assertEquals(0, reads)
            assertFalse(first.isCompleted)
            assertFalse(second.isCompleted)
            client.auth.setSessionStatus(
                SessionStatus.Authenticated(
                    UserSession(
                        accessToken = "fresh-token",
                        refreshToken = "fresh-refresh-token",
                        expiresIn = 3600,
                        tokenType = "Bearer",
                    ),
                ),
            )
            first.await()
            second.await()
            assertEquals(2, reads)
        } finally {
            client.close()
        }
    }

    @Test
    fun simultaneousReadsWaitForRefreshAndUseTheRecoveredToken() = runTest {
        val authorizations = mutableListOf<String?>()
        val client =
            createSupabaseClient("https://loom.test", "test-publishable-key") {
                install(Auth) {
                    autoLoadFromStorage = false
                    autoSaveToStorage = false
                }
                install(Postgrest)
                httpEngine = MockEngine { request ->
                    authorizations += request.headers[HttpHeaders.Authorization]
                    respond("[]", headers = headersOf(HttpHeaders.ContentType, "application/json"))
                }
            }
        try {
            client.auth.awaitInitialization()
            client.auth.setSessionStatus(
                SessionStatus.RefreshFailure(
                    RefreshFailureCause.NetworkError(IOException("offline")),
                ),
            )
            val tasks = async {
                client.awaitAuthenticatedSession()
                client.from("tasks").select()
            }
            val shopping = async {
                client.awaitAuthenticatedSession()
                client.from("shopping_items").select()
            }
            runCurrent()
            assertFalse(tasks.isCompleted)
            assertFalse(shopping.isCompleted)
            assertTrue(authorizations.isEmpty())

            client.auth.setSessionStatus(
                SessionStatus.Authenticated(
                    UserSession(
                        accessToken = "recovered-test-token",
                        refreshToken = "test-refresh-token",
                        expiresIn = 3600,
                        tokenType = "Bearer",
                    ),
                ),
            )
            tasks.await()
            shopping.await()
            assertEquals(
                listOf<String?>("Bearer recovered-test-token", "Bearer recovered-test-token"),
                authorizations,
            )
        } finally {
            client.close()
        }
    }

    @Test
    fun aRealMembershipDenialIsNotRetriedOrRelabeledAsSessionRecovery() = runTest {
        var requests = 0
        val client =
            createSupabaseClient("https://loom.test", "test-key") {
                install(Auth) {
                    autoLoadFromStorage = false
                    autoSaveToStorage = false
                }
                install(Postgrest)
                httpEngine = MockEngine {
                    requests++
                    respond(
                        """{"code":"42501","message":"Household access denied.","details":null,"hint":null}""",
                        status = HttpStatusCode.Forbidden,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }
        try {
            client.auth.awaitInitialization()
            client.auth.setSessionStatus(
                SessionStatus.Authenticated(
                    UserSession(
                        accessToken = "valid-test-token",
                        refreshToken = "test-refresh-token",
                        expiresIn = 3600,
                        tokenType = "Bearer",
                    ),
                ),
            )
            val result = safeApiCall {
                client.awaitAuthenticatedSession()
                client.from("tasks").select()
            }
            assertIs<CommonError.AccessDenied>(assertIs<AppResult.Failure>(result).error)
            assertEquals(1, requests)
        } finally {
            client.close()
        }
    }

    @Test
    fun aCancelledReadDoesNotWaitForARecoveredSession() = runTest {
        val client =
            createSupabaseClient("https://loom.test", "test-key") {
                install(Auth) {
                    autoLoadFromStorage = false
                    autoSaveToStorage = false
                }
                httpEngine = MockEngine { error("No transport call expected") }
            }
        try {
            client.auth.awaitInitialization()
            client.auth.setSessionStatus(
                SessionStatus.RefreshFailure(
                    RefreshFailureCause.NetworkError(IOException("offline")),
                ),
            )
            var loaded = false
            val read = launch {
                client.awaitAuthenticatedSession()
                loaded = true
            }
            runCurrent()
            read.cancelAndJoin()
            assertFalse(loaded)
        } finally {
            client.close()
        }
    }
}
