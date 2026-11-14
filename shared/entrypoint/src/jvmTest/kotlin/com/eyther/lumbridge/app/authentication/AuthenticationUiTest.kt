package com.eyther.lumbridge.app.authentication

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.app.authentication.data.AuthenticationFixtureDataSource
import com.eyther.lumbridge.app.navigation.AuthenticationHost
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.di.applicationModules
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.koin.compose.KoinIsolatedContext
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@OptIn(ExperimentalTestApi::class)
class AuthenticationUiTest {

    @Test
    fun registrationVerificationAndPasswordVisibilityWorkAcrossSeparateDestinations() {
        val source = AuthenticationFixtureDataSource()
        val graph = authenticationGraph(source = source)
        try {
            runComposeUiTest {
                setContent { AuthenticationTestContent(graph = graph) }
                waitUntil(timeoutMillis = 10_000) {
                    onAllNodesWithText("Welcome home").fetchSemanticsNodes().isNotEmpty()
                }
                capture(name = "auth-login-light")
                onNodeWithTag("auth_password").performTextInput("private password")
                onNodeWithTag("auth_password").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
                onNodeWithContentDescription("Show password").performClick()
                assertPasswordDisplay(expected = "private password")
                onNodeWithContentDescription("Hide password").performClick()
                assertPasswordDisplay(expected = "•".repeat("private password".length))
                onNodeWithTag("auth_password").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
                onNodeWithText("New here? Create an account").performScrollTo().performClick()
                onNodeWithText("Make yourself at home").assertExists()
                capture(name = "auth-register-light")
                onNodeWithTag("auth_email").performTextInput("member@example.com")
                onNodeWithTag("auth_password").performTextInput("private password")
                onNodeWithTag("accept_terms").performScrollTo().performClick()
                onNodeWithTag("register_submit").performScrollTo().performClick()
                waitUntil(timeoutMillis = 5_000) {
                    onAllNodesWithText("Verification code").fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithTag("verification_submit").assertIsNotEnabled()
                onNodeWithTag("verification_resend").assertIsNotEnabled()
                capture(name = "auth-verification-light")
                onNodeWithTag("verification_code").performTextInput("000000")
                onNodeWithTag("verification_submit").performClick()
                waitUntil(timeoutMillis = 5_000) {
                    onAllNodesWithText("That code is invalid or has expired. Request a new code and try again.")
                        .fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithTag("verification_code").performTextReplacement("123456")
                onNodeWithTag("verification_submit").performClick()
                waitUntil(timeoutMillis = 5_000) { source.stream().value != null }
            }
        } finally {
            graph.close()
        }
    }

    @Test
    fun anExistingAccountCanSignInWithAnEmailCodeWithoutAPassword() {
        val source = AuthenticationFixtureDataSource()
        val graph = authenticationGraph(source = source)
        try {
            runComposeUiTest {
                setContent { AuthenticationTestContent(graph = graph) }
                onNodeWithTag("auth_email").performTextInput("member@example.com")
                onNodeWithTag("login_submit").assertIsNotEnabled()
                onNodeWithTag("login_email_code").performClick()
                waitUntil(timeoutMillis = 5_000) {
                    onAllNodesWithText("Verification code").fetchSemanticsNodes().isNotEmpty()
                }
                assertEquals(1, source.signInCodeCount)
                assertEquals(0, source.resendCount)
                onNodeWithTag("verification_code").performTextInput("123456")
                onNodeWithTag("verification_submit").performClick()
                waitUntil(timeoutMillis = 5_000) { source.stream().value != null }
            }
        } finally {
            graph.close()
        }
    }

    @Test
    fun anExistingUnconfirmedAccountCanRequestAnotherCode() {
        val source = AuthenticationFixtureDataSource()
        val graph = authenticationGraph(source = source)
        try {
            runComposeUiTest {
                setContent { AuthenticationTestContent(graph = graph) }
                onNodeWithTag("auth_email").performTextInput("member@example.com")
                onNodeWithTag("auth_password").performTextInput("private password")
                onNodeWithTag("login_submit").performScrollTo().performClick()
                waitUntil(timeoutMillis = 5_000) {
                    onAllNodesWithText("Verification code").fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithTag("verification_resend").performClick()
                waitUntil(timeoutMillis = 5_000) { source.resendCount == 1 }
                onNodeWithTag("verification_resend").assertIsNotEnabled()
            }
        } finally {
            graph.close()
        }
    }

    private fun authenticationGraph(source: AuthenticationDataSource): KoinApplication = koinApplication {
        modules(applicationModules())
        modules(module { single<AuthenticationDataSource> { source } })
    }

    private fun ComposeUiTest.assertPasswordDisplay(expected: String) {
        onNodeWithTag("auth_password").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { getResults ->
            val results = mutableListOf<TextLayoutResult>()
            assertTrue(getResults(results))
            assertEquals(expected, results.single().layoutInput.text.text)
        }
    }

    @Composable
    private fun AuthenticationTestContent(graph: KoinApplication) {
        KoinIsolatedContext(context = graph) {
            LoomTheme(mode = ThemeMode.LIGHT) {
                Box(Modifier.requiredSize(440.dp, 760.dp).testTag("auth_app")) {
                    AuthenticationHost(onOpenLegalDocument = {})
                }
            }
        }
    }

    private fun ComposeUiTest.capture(name: String) {
        waitForIdle()
        val bitmap = onNodeWithTag("auth_app").captureToImage()
        val directory = File(System.getProperty("loom.screenshots.dir", "build/screenshots")).apply { mkdirs() }
        File(directory, "$name.png").writeBytes(
            Image.makeFromBitmap(bitmap.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)!!.bytes,
        )
    }
}
