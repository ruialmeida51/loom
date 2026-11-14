package com.eyther.lumbridge.features.authentication.data.network

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.data.fixtures.sessionResponseFixture
import com.eyther.lumbridge.features.authentication.data.network.config.configureLoomAuthentication
import com.eyther.lumbridge.features.authentication.data.network.datasource.CommonAuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.errors.AuthenticationError
import com.eyther.lumbridge.features.settings.domain.datasource.PushDeviceDataSource
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class EmailVerificationDataSourceTest {

    private val pushDevices = object : PushDeviceDataSource {
        override suspend fun syncRegistration(): AppResult<Unit> = AppResult.Success(Unit)

        override suspend fun unregisterCurrentDevice(): AppResult<Unit> = AppResult.Success(Unit)
    }

    @Test
    fun expiredCodesAndResendsUseTheExpectedAuthEndpointsAndKeepTypedFailures() = runTest {
        val paths = mutableListOf<String>()
        val client = createSupabaseClient(supabaseUrl = "https://loom.test", supabaseKey = "test-key") {
            install(Auth) {
                autoLoadFromStorage = false
                alwaysAutoRefresh = false
            }
            httpEngine = MockEngine { request ->
                paths += request.url.encodedPath
                val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                assertEquals("member@example.com", body.getValue("email").jsonPrimitive.content)
                if (request.url.encodedPath.endsWith("/verify")) {
                    assertEquals("email", body.getValue("type").jsonPrimitive.content)
                    assertEquals("001234", body.getValue("token").jsonPrimitive.content)
                    respond(
                        content = """{"code":403,"error_code":"otp_expired","msg":"opaque server text"}""",
                        status = HttpStatusCode.Forbidden,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                } else if (request.url.encodedPath.endsWith("/resend")) {
                    assertEquals("signup", body.getValue("type").jsonPrimitive.content)
                    respond(content = "{}", headers = headersOf(HttpHeaders.ContentType, "application/json"))
                } else {
                    assertEquals("false", body.getValue("create_user").jsonPrimitive.content)
                    respond(content = "{}", headers = headersOf(HttpHeaders.ContentType, "application/json"))
                }
            }
        }
        try {
            val source = CommonAuthenticationDataSource(access = SupabaseAccess(client = client), pushDevices = pushDevices)
            val result = source.verifyEmail(email = "member@example.com", code = "001234")
            assertIs<AuthenticationError.VerificationCodeExpired>(assertIs<AppResult.Failure>(result).error)
            assertIs<AppResult.Success<Unit>>(source.resendVerificationEmail(email = "member@example.com"))
            assertIs<AppResult.Success<Unit>>(source.sendSignInCode(email = "member@example.com"))
            assertEquals(listOf("/auth/v1/verify", "/auth/v1/resend", "/auth/v1/otp"), paths)
        } finally {
            client.close()
        }
    }
    @Test
    fun emailCodesStillPublishASessionWithTheProductionPkceConfiguration() = runTest {
        val client = createSupabaseClient(supabaseUrl = "https://loom.test", supabaseKey = "test-key") {
            install(Auth) {
                configureLoomAuthentication(sessionStorage = MemorySessionManager(), verifierCache = MemoryCodeVerifierCache())
                autoLoadFromStorage = false
                alwaysAutoRefresh = false
            }
            httpEngine = MockEngine {
                respond(content = sessionResponseFixture(), headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }
        try {
            val source = CommonAuthenticationDataSource(access = SupabaseAccess(client = client), pushDevices = pushDevices)
            assertIs<AppResult.Success<Unit>>(source.verifyEmail(email = "member@example.com", code = "123456"))
            val session = withContext(Dispatchers.Default) {
                withTimeout(3000) { source.stream().filterNotNull().first() }
            }
            assertEquals("member@example.com", session.email)
        } finally {
            client.close()
        }
    }

    @Test
    fun aResponseWithoutASessionCannotClaimVerificationSucceeded() = runTest {
        val client = createSupabaseClient(supabaseUrl = "https://loom.test", supabaseKey = "test-key") {
            install(Auth) {
                autoLoadFromStorage = false
                alwaysAutoRefresh = false
            }
            httpEngine = MockEngine {
                respond(content = "{}", headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }
        try {
            val source = CommonAuthenticationDataSource(access = SupabaseAccess(client = client), pushDevices = pushDevices)
            val result = source.verifyEmail(email = "member@example.com", code = "123456")
            assertIs<AuthenticationError.VerificationFailed>(assertIs<AppResult.Failure>(result).error)
        } finally {
            client.close()
        }
    }

}
