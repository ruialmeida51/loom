package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.Density
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
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
    fun homeLeadsWithTheBalanceAndTheFullTaskListHasItsOwnTab() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            ThemePreferences.set("LIGHT")
            setContent { LoomDemoApp() }
            waitUntil(timeoutMillis = 10000) {
                onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty() &&
                    onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("home_greeting").assertIsDisplayed()
            onNodeWithTag("home_balance_card").assertIsDisplayed()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("home_balance_amount").fetchSemanticsNodes().isNotEmpty()
            }
            capture("home-light")
            onNodeWithTag("household_switch").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("household_choice_The little nest").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("household_choice_The little nest").assertIsSelected()
            capture("household-chooser-light")
            onNodeWithContentDescription("Back").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithText("Feed").assertDoesNotExist()
            onNodeWithTag("home_content").performScrollToNode(hasTestTag("home_add_task"))
            onNodeWithTag("home_add_task").performClick()
            onNodeWithTag("form_sheet").assertIsDisplayed()
            onNodeWithText("Cancel").performClick()
            onNodeWithContentDescription("Back").assertDoesNotExist()
            onNodeWithTag("home_content").performScrollToNode(hasTestTag("home_add_expense"))
            onNodeWithTag("home_add_expense").assertIsDisplayed()
            onNodeWithTag("navigation_more").performClick()
            capture("more-light")
            onNodeWithTag("more_content").assertIsDisplayed()
            onNodeWithContentDescription("Back").assertDoesNotExist()
            onNodeWithTag("navigation_tasks").performClick()
            awaitLoadedDestination(tag = "tasks_content")
            capture("tasks-light")
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

    @Test
    fun secondaryNewsPreviewOpensTheFullFeedAndBackKeepsHomeScroll() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            ThemePreferences.set("LIGHT")
            setContent { LoomDemoApp() }
            waitUntil(timeoutMillis = 10000) {
                onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty() &&
                    onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("home_content").performScrollToNode(hasTestTag("home_feed_preview"))
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("home_feed_preview").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("home_feed_preview").assertExists()
            val carousel = onNodeWithTag("home_feed_preview").fetchSemanticsNode().boundsInRoot
            val page = onNodeWithTag("home_content").fetchSemanticsNode().boundsInRoot
            assertTrue(
                abs(carousel.left - page.left) < 1f && abs(carousel.right - page.right) < 1f,
                "The news viewport reaches both screen edges",
            )
            capture("home-news-light")
            onNodeWithText("All news").performClick()
            onNodeWithText("The reading corner").assertExists()
            onNodeWithContentDescription("Back").performClick()
            onNodeWithText("All news").assertIsDisplayed()
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
