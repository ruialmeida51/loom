package com.eyther.lumbridge.authentication

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.demo.UsabilityTestActivity
import com.eyther.lumbridge.features.authentication.data.network.config.configureLoomAuthentication
import com.eyther.lumbridge.features.authentication.data.network.datasource.CommonAuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.model.AuthenticationStateDomain
import com.eyther.lumbridge.features.settings.domain.datasource.PushDeviceDataSource
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Real ProcessLifecycleOwner callbacks and screen locking, with no hosted credentials or writes.
 */
class SessionLifecycleDeviceTest {
    @Test
    fun homeLockAndActivityRecreationRestoreTheSameIdentityAndRotatedRefreshToken() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val device = UiDevice.getInstance(instrumentation)
        val scenario = ActivityScenario.launch(UsabilityTestActivity::class.java)
        val storage = MemorySessionManager()
        storage.saveSession(
            UserSession(
                accessToken = "test-access-0",
                refreshToken = "test-refresh-0",
                expiresIn = 3600,
                tokenType = "Bearer",
                user =
                    UserInfo(id = "member", aud = "authenticated", email = "member@example.test"),
            ),
        )
        val refreshes = AtomicInteger()
        val client =
            createSupabaseClient("https://loom.test", "test-key") {
                install(Auth) {
                    configureLoomAuthentication(
                        sessionStorage = storage,
                        verifierCache = MemoryCodeVerifierCache(),
                    )
                }
                httpEngine = MockEngine { request ->
                    assertEquals("refresh_token", request.url.parameters["grant_type"])
                    val body =
                        Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                    assertEquals(
                        "test-refresh-${refreshes.get()}",
                        body.getValue("refresh_token").jsonPrimitive.content,
                    )
                    val rotation = refreshes.incrementAndGet()
                    respond(
                        """{"access_token":"test-access-$rotation","refresh_token":"test-refresh-$rotation",
                    "token_type":"Bearer","expires_in":3600,"user":{"id":"member","aud":"authenticated","email":"member@example.test"}}""",
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
                    access = SupabaseAccess(client),
                    pushDevices = pushDevices,
                )
            withTimeout(10_000L) {
                source.stream().filterIsInstance<AuthenticationStateDomain.Authenticated>().first()
            }
            repeat(2) { cycle ->
                if (cycle == 0) device.pressHome() else device.sleep()
                withTimeout(10_000L) {
                    client.auth.sessionStatus.first { it is SessionStatus.Initializing }
                }
                val saved = storage.loadSession()
                assertEquals("test-refresh-$cycle", saved.refreshToken)
                // Model a token that expired during a long sleep without slowing the device suite.
                storage.saveSession(saved.copy(expiresAt = Clock.System.now() - 1.seconds))
                assertTrue(source.stream().value is AuthenticationStateDomain.Authenticated)
                if (cycle == 1) {
                    device.wakeUp()
                    device.executeShellCommand("wm dismiss-keyguard")
                }
                context.startActivity(
                    Intent(context, UsabilityTestActivity::class.java)
                        .addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
                        ),
                )
                val restored =
                    withTimeout(10_000L) {
                        client.auth.sessionStatus
                            .filterIsInstance<SessionStatus.Authenticated>()
                            .first { it.session.refreshToken == "test-refresh-${cycle + 1}" }
                    }
                assertEquals("member", restored.session.user?.id)
                assertEquals("test-refresh-${cycle + 1}", storage.loadSession().refreshToken)
            }
            scenario.recreate()
            withTimeout(10_000L) {
                client.auth.sessionStatus.filterIsInstance<SessionStatus.Authenticated>().first {
                    it.session.user?.id == "member"
                }
            }
            assertNotNull(client.auth.currentSessionOrNull())
            assertEquals("member", client.auth.currentUserOrNull()?.id)
            assertEquals(2, refreshes.get())
        } finally {
            device.wakeUp()
            device.executeShellCommand("wm dismiss-keyguard")
            client.close()
            scenario.close()
        }
    }
}
