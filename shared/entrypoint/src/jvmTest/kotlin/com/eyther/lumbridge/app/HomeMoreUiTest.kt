package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

@OptIn(ExperimentalTestApi::class)
class HomeMoreUiTest {
    @Test
    fun squareActionsOpenTheirTaskAndMoreOwnsTheFullReminderList() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            ThemePreferences.set("LIGHT")
            setContent { LoomDemoApp() }
            waitUntil(timeoutMillis = 10000) {
                onAllNodesWithText("Today").fetchSemanticsNodes().isNotEmpty()
            }
            for (tag in listOf("home_add_expense", "home_add_reminder")) {
                val bounds = onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
                assertTrue(abs(bounds.width - bounds.height) < 1f, "Quick actions stay square")
            }
            onNodeWithText("Feed").assertDoesNotExist()
            onNodeWithTag("home_add_reminder").performClick()
            onNodeWithTag("form_sheet").assertIsDisplayed()
            onNodeWithText("Cancel").performClick()
            onNodeWithContentDescription("Back").performClick()
            onNodeWithTag("home_add_expense").assertIsDisplayed()
            onNodeWithTag("navigation_more").performClick()
            capture("more-light")
            onNodeWithText("Reminders").performClick()
            onNodeWithTag("reminders_content").assertIsDisplayed()
            capture("reminders-light")
            onNodeWithContentDescription("Back").performClick()
            onNodeWithTag("more_content").assertIsDisplayed()
            onNodeWithContentDescription("Back").assertDoesNotExist()
        }

    @Test
    fun moreRemainsScrollableOnAShortScreenWithLargerText() =
        runSkikoComposeUiTest(
            size = Size(360f, 360f),
            density = Density(density = 1f, fontScale = 1.3f),
        ) {
            ThemePreferences.set("LIGHT")
            setContent { LoomDemoApp() }
            waitUntil(timeoutMillis = 10000) {
                onAllNodesWithTag("home_add_expense").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("navigation_more").performClick()
            onNodeWithText("Household details").performScrollTo().assertIsDisplayed()
            capture("more-short-large-text")
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
