package com.eyther.lumbridge.features.authentication.data.network

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.data.fixtures.sessionResponseFixture
import com.eyther.lumbridge.features.authentication.data.network.config.configureLoomAuthentication
import com.eyther.lumbridge.features.authentication.data.network.datasource.CommonAuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.model.AuthenticationStateDomain
import com.eyther.lumbridge.features.settings.domain.datasource.PushDeviceDataSource
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@OptIn(SupabaseInternal::class)
class SessionRecoveryDataSourceTest {
    private val pushDevices =
        object : PushDeviceDataSource {
            override suspend fun syncRegistration(): AppResult<Unit> = AppResult.Success(Unit)

            override suspend fun unregisterCurrentDevice(): AppResult<Unit> =
                AppResult.Success(Unit)
        }

    @Test
    fun simultaneousRecoveryRequestsShareTheSdkRefreshAndPersistItsRotatedToken() = runTest {
        val storage = MemorySessionManager()
        storage.saveSession(expiredSession())
        var refreshes = 0
        val releaseRefresh = CompletableDeferred<Unit>()
        val client =
            createSupabaseClient("https://loom.test", "test-key") {
                install(Auth) {
                    configureLoomAuthentication(storage, MemoryCodeVerifierCache())
                    autoLoadFromStorage = false
                    autoSetupPlatform = false
                }
                httpEngine = MockEngine { request ->
                    refreshes++
                    assertEquals("refresh_token", request.url.parameters["grant_type"])
                    val body =
                        Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                    assertEquals(
                        "stored-refresh",
                        body.getValue("refresh_token").jsonPrimitive.content,
                    )
                    releaseRefresh.await()
                    respond(
                        sessionResponseFixture(),
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }
        try {
            val source =
                CommonAuthenticationDataSource(
                    access = SupabaseAccess(client),
                    pushDevices = pushDevices,
                )
            withContext(Dispatchers.Default) {
                val first = async { source.recoverSession() }
                val second = async { source.recoverSession() }
                releaseRefresh.complete(Unit)
                withTimeout(3000) {
                    assertIs<AppResult.Success<Unit>>(first.await())
                    assertIs<AppResult.Success<Unit>>(second.await())
                    source
                        .stream()
                        .filterIsInstance<AuthenticationStateDomain.Authenticated>()
                        .first()
                }
            }
            assertEquals(1, refreshes)
            assertEquals(
                "test-refresh-token",
                assertNotNull(storage.loadSessionOrNull()).refreshToken,
            )
        } finally {
            client.close()
        }
    }

    @Test
    fun recoveryAfterABackgroundCancellationKeepsStoredCredentialsThroughAnOfflineFailure() =
        runTest {
            val storage = MemorySessionManager()
            storage.saveSession(expiredSession())
            val online = CompletableDeferred<Unit>()
            val failedRefresh = CompletableDeferred<Unit>()
            var attempts = 0
            val client =
                createSupabaseClient("https://loom.test", "test-key") {
                    install(Auth) {
                        configureLoomAuthentication(storage, MemoryCodeVerifierCache())
                        autoLoadFromStorage = false
                        autoSetupPlatform = false
                    }
                    httpEngine = MockEngine {
                        attempts++
                        if (!online.isCompleted) {
                            failedRefresh.complete(Unit)
                            throw IOException("Offline while resuming")
                        }
                        respond(
                            sessionResponseFixture(),
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }
                }
            try {
                val source =
                    CommonAuthenticationDataSource(
                        access = SupabaseAccess(client),
                        pushDevices = pushDevices,
                    )
                withContext(Dispatchers.Default) {
                    client.auth.loadFromStorage()
                    withTimeout(3000) { failedRefresh.await() }
                    // These are the exact SDK status/job changes made by Android's onStop callback.
                    client.auth.stopAutoRefreshForCurrentSession()
                    client.auth.setSessionStatus(SessionStatus.Initializing)
                    assertEquals(
                        "stored-refresh",
                        assertNotNull(storage.loadSessionOrNull()).refreshToken,
                    )
                    online.complete(Unit)
                    withTimeout(3000) {
                        assertIs<AppResult.Success<Unit>>(source.recoverSession())
                        source
                            .stream()
                            .filterIsInstance<AuthenticationStateDomain.Authenticated>()
                            .first()
                    }
                }
                assertEquals(2, attempts)
                assertEquals(
                    "test-refresh-token",
                    assertNotNull(storage.loadSessionOrNull()).refreshToken,
                )
            } finally {
                client.close()
            }
        }

    @Test
    fun aRevokedRefreshTokenSignsOutAndCannotBeRestoredByRetry() = runTest {
        val storage = MemorySessionManager()
        storage.saveSession(expiredSession())
        var requests = 0
        val client =
            createSupabaseClient("https://loom.test", "test-key") {
                install(Auth) {
                    configureLoomAuthentication(storage, MemoryCodeVerifierCache())
                    autoLoadFromStorage = false
                    autoSetupPlatform = false
                }
                httpEngine = MockEngine {
                    requests++
                    respond(
                        """{"error_code":"refresh_token_not_found","msg":"Session revoked"}""",
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }
        try {
            val source =
                CommonAuthenticationDataSource(
                    access = SupabaseAccess(client),
                    pushDevices = pushDevices,
                )
            withContext(Dispatchers.Default) {
                withTimeout(3000) {
                    val result = source.recoverSession()
                    assertIs<CommonError.NotAuthenticated>(
                        assertIs<AppResult.Failure>(result).error,
                    )
                    source.stream().first { it is AuthenticationStateDomain.SignedOut }
                }
                assertIs<CommonError.NotAuthenticated>(
                    assertIs<AppResult.Failure>(source.recoverSession()).error,
                )
            }
            assertEquals(1, requests)
            assertNull(storage.loadSessionOrNull())
        } finally {
            client.close()
        }
    }

    private fun expiredSession(): UserSession =
        Json.decodeFromString<UserSession>(sessionResponseFixture())
            .copy(
                accessToken = "expired-access",
                refreshToken = "stored-refresh",
                expiresAt = Clock.System.now() - 1.seconds,
            )
}
