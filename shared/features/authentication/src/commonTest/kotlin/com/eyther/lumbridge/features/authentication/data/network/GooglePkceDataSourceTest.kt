package com.eyther.lumbridge.features.authentication.data.network

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.data.fixtures.sessionResponseFixture
import com.eyther.lumbridge.features.authentication.data.network.config.AuthenticationRedirect
import com.eyther.lumbridge.features.authentication.data.network.config.configureLoomAuthentication
import com.eyther.lumbridge.features.authentication.data.network.datasource.CommonAuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.model.AuthenticationStateDomain
import com.eyther.lumbridge.features.settings.domain.datasource.PushDeviceDataSource
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.UrlLauncher
import io.github.jan.supabase.createSupabaseClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@OptIn(SupabaseExperimental::class, ExperimentalCoroutinesApi::class)
class GooglePkceDataSourceTest {

    @Test
    fun browserSignInAndTheCallbackExchangeUseTheSamePkceVerifierAndPublishTheSession() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val cache = MemoryCodeVerifierCache()
        val callbacks = MutableSharedFlow<AppResult<String>>(replay = 1)
        var launchedUrl: String? = null
        var expectedVerifier: String? = null
        val client =
            createSupabaseClient(supabaseUrl = "https://loom.test", supabaseKey = "test-key") {
                install(Auth) {
                    configureLoomAuthentication(
                        sessionStorage = MemorySessionManager(),
                        verifierCache = cache,
                    )
                    autoLoadFromStorage = false
                    alwaysAutoRefresh = false
                    urlLauncher = UrlLauncher { _, url -> launchedUrl = url }
                }
                httpEngine = MockEngine { request ->
                    assertEquals("/auth/v1/token", request.url.encodedPath)
                    assertEquals("pkce", request.url.parameters["grant_type"])
                    val body =
                        Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                    assertEquals(
                        "authorization-code",
                        body.getValue("auth_code").jsonPrimitive.content,
                    )
                    assertEquals(
                        expectedVerifier,
                        body.getValue("code_verifier").jsonPrimitive.content,
                    )
                    respond(
                        content = sessionResponseFixture(),
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }
        val pushDevices =
            object : PushDeviceDataSource {
                override suspend fun syncRegistration(): AppResult<Unit> = AppResult.Success(Unit)

                override suspend fun unregisterCurrentDevice(): AppResult<Unit> =
                    AppResult.Success(Unit)
            }

        try {
            val source =
                CommonAuthenticationDataSource(
                    access = SupabaseAccess(client = client),
                    pushDevices = pushDevices,
                    callbackEvents = callbacks,
                )
            assertIs<AppResult.Success<Unit>>(source.signInWithGoogle())
            val url = Url(assertNotNull(launchedUrl))
            assertEquals("google", url.parameters["provider"])
            assertEquals(AuthenticationRedirect.URL, url.parameters["redirect_to"])
            assertEquals("s256", url.parameters["code_challenge_method"])
            assertNotNull(url.parameters["code_challenge"])

            expectedVerifier =
                withContext(Dispatchers.Default) {
                    withTimeout(3000) {
                        var saved = cache.loadCodeVerifier()
                        while (saved == null) {
                            yield()
                            saved = cache.loadCodeVerifier()
                        }
                        saved
                    }
                }
            callbacks.emit(AppResult.Success("authorization-code"))
            val session =
                withContext(Dispatchers.Default) {
                    withTimeout(3000) {
                        source
                            .stream()
                            .filterIsInstance<AuthenticationStateDomain.Authenticated>()
                            .first()
                    }
                }
            assertEquals("member@example.com", session.session.email)
            assertEquals("11111111-1111-1111-1111-111111111111", session.session.userId)
        } finally {
            client.close()
            Dispatchers.resetMain()
        }
    }
}
