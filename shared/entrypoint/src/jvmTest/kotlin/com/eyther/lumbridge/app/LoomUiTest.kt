package com.eyther.lumbridge.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.unit.dp
import java.io.File
import kotlin.test.Test
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

@OptIn(ExperimentalTestApi::class)
class LoomUiTest {
    @Test
    fun shoppingAndThemesWorkAcrossNavigation() = runComposeUiTest {
        setContent { Box(Modifier.requiredSize(440.dp, 760.dp).testTag("loom_app")) { LoomApp() } }
        waitForIdle()
        if (onAllNodesWithText("Explore a sample household").fetchSemanticsNodes().isNotEmpty())
            onNodeWithText("Explore a sample household").performClick()
        waitUntil(timeoutMillis = 10_000) {
            onAllNodesWithText("Home, in harmony.").fetchSemanticsNodes().isNotEmpty()
        }
        capture("home-light")
        onNodeWithText("Shopping", useUnmergedTree = true).performClick()
        onNodeWithText("Add an item").performClick()
        onNodeWithText("What do we need?").performTextInput("Lemons")
        onNodeWithText("Quantity or note").performTextInput("4")
        onNodeWithText("Add item").performClick()
        waitUntil(timeoutMillis = 5_000) {
            onAllNodesWithText("Add item").fetchSemanticsNodes().isEmpty()
        }
        onNode(hasScrollAction()).performScrollToNode(hasText("Lemons"))
        onNodeWithText("Lemons").assertExists()
        onNode(hasScrollAction()).performScrollToIndex(0)
        capture("shopping-light")
        onNodeWithContentDescription("Settings").performClick()
        onNodeWithText("Dark", useUnmergedTree = true).performClick()
        waitForIdle()
        onNodeWithText("Home", useUnmergedTree = true).performClick()
        onNodeWithText("Home, in harmony.").assertExists()
        capture("home-dark")
        onNodeWithText("Money", useUnmergedTree = true).performClick()
        onNodeWithText("Money, untangled.").assertExists()
        capture("money-dark")
    }

    private fun ComposeUiTest.capture(name: String) {
        waitForIdle()
        val image = onNodeWithTag("loom_app").captureToImage()
        val directory =
            File(System.getProperty("loom.screenshots.dir", "build/screenshots")).apply { mkdirs() }
        File(directory, "$name.png")
            .writeBytes(
                Image.makeFromBitmap(image.asSkiaBitmap())
                    .encodeToData(EncodedImageFormat.PNG)!!
                    .bytes,
            )
    }
}
