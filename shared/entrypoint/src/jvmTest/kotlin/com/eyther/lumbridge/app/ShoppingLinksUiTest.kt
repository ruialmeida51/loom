package com.eyther.lumbridge.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

@OptIn(ExperimentalTestApi::class)
class ShoppingLinksUiTest {
    @Test
    fun aPastedLinkShowsAsItsSiteOpensOnTapAndEditsInFull() =
        runSkikoComposeUiTest(size = Size(440f, 900f)) {
            ThemePreferences.set("LIGHT")
            val opened = mutableListOf<String>()
            val uriHandler =
                object : UriHandler {
                    override fun openUri(uri: String) {
                        opened += uri
                    }
                }
            setContent {
                CompositionLocalProvider(LocalUriHandler provides uriHandler) { LoomDemoApp() }
            }
            waitUntil(timeoutMillis = 10_000) {
                onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("navigation_shopping").performClick()
            awaitLoadedDestination(tag = "shopping_content")
            val name = "Avocados https://www.example.test/shop/hass-avocados?size=large"
            onNodeWithTag("edit_Avocados").performClick()
            onNodeWithTag("edit_input_Avocados").performTextReplacement(name)
            onNodeWithTag("edit_input_Avocados").performImeAction()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("edit_$name").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithText("Avocados example.test").assertExists()
            capture("shopping-item-link")
            onNodeWithText("example.test", substring = true, useUnmergedTree = true)
                .performClick()
            // The link opened; the row did not switch to editing.
            waitForIdle()
            assertEquals(listOf("https://www.example.test/shop/hass-avocados?size=large"), opened)
            onNodeWithTag("edit_input_$name").assertDoesNotExist()
        }

    private fun SkikoComposeUiTest.capture(name: String) {
        waitForIdle()
        val directory =
            File(System.getProperty("loom.screenshots.dir", "build/screenshots")).apply { mkdirs() }
        File(directory, "$name.png")
            .writeBytes(
                Image.makeFromBitmap(captureToImage().asSkiaBitmap())
                    .encodeToData(EncodedImageFormat.PNG)!!
                    .bytes,
            )
    }
}
