package com.eyther.lumbridge.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import java.io.File
import kotlin.test.Test
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

@OptIn(ExperimentalTestApi::class)
class FeedManagementUiTest {
    @Test
    fun subscriptionsCanBeAddedEditedAndRemovedFromTheirOwnDestination() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            ThemePreferences.set("LIGHT")
            setContent { Box(Modifier.fillMaxSize().testTag("feed_app")) { LoomDemoApp() } }
            awaitLoadedDestination(tag = "home_content")
            onNodeWithTag("navigation_more").performClick()
            onNodeWithText("Feed").performClick()
            awaitLoadedDestination(tag = "manage_feeds")
            onNodeWithTag("manage_feeds").performClick()
            awaitLoadedDestination(tag = "feed_subscriptions")
            onNodeWithText("Add a feed").performClick()
            onNodeWithText("https://…")
                .performTextInput("https://rss.impresa.pt/feed/latest/expresso.rss")
            onNodeWithTag("save_feed_subscription").performClick()
            onNodeWithTag("feed_subscriptions").performScrollToNode(hasText("rss.impresa.pt"))
            onNodeWithText("rss.impresa.pt").assertExists()
            val originalCard =
                hasTestTag("feed_subscription_card") and hasAnyDescendant(hasText("rss.impresa.pt"))
            onNode(hasText("rss.impresa.pt") and hasAnyAncestor(originalCard)).performClick()
            onNodeWithText("https://…").performTextReplacement("https://www.nasa.gov/feed/")
            onNodeWithTag("save_feed_subscription").performClick()
            onNodeWithTag("feed_subscriptions").performScrollToNode(hasText("www.nasa.gov"))
            onNodeWithText("www.nasa.gov").assertExists()
            val editedCard =
                hasTestTag("feed_subscription_card") and hasAnyDescendant(hasText("www.nasa.gov"))
            onNode(hasContentDescription("More actions") and hasAnyAncestor(editedCard))
                .performClick()
            onNodeWithText("Remove").performClick()
            onNodeWithTag("confirm_remove_feed").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("www.nasa.gov").fetchSemanticsNodes().isEmpty()
            }
            waitForIdle()
            val output =
                File(
                    System.getProperty("loom.screenshots.dir", "build/screenshots"),
                    "feed-management-light.png",
                )
            output.parentFile.mkdirs()
            output.writeBytes(
                requireNotNull(
                        Image.makeFromBitmap(
                                onNodeWithTag("feed_app").captureToImage().asSkiaBitmap(),
                            )
                            .encodeToData(EncodedImageFormat.PNG),
                    )
                    .bytes,
            )
            onNodeWithContentDescription("Back").performClick()
            onNodeWithText("The reading corner").assertExists()
            for ((mode, name) in listOf("LIGHT" to "feed-light", "DARK" to "feed-dark")) {
                ThemePreferences.set(mode)
                waitForIdle()
                val screen =
                    File(
                        System.getProperty("loom.screenshots.dir", "build/screenshots"),
                        "$name.png",
                    )
                screen.writeBytes(
                    requireNotNull(
                            Image.makeFromBitmap(
                                    onNodeWithTag("feed_app").captureToImage().asSkiaBitmap(),
                                )
                                .encodeToData(EncodedImageFormat.PNG),
                        )
                        .bytes,
                )
            }
            onNodeWithContentDescription("Back").performClick()
            onNodeWithTag("more_content").assertExists()
        }
}
