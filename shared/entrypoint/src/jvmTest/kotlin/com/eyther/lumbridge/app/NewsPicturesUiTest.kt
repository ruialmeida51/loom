package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

@OptIn(ExperimentalTestApi::class)
class NewsPicturesUiTest {
    @Test
    fun todaysReadsSitUnderTheBalanceWithTheirPictures() =
        runSkikoComposeUiTest(size = Size(440f, 900f)) {
            ThemePreferences.set("LIGHT")
            setContent { LoomDemoApp() }
            waitUntil(timeoutMillis = 10_000) {
                onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty() &&
                    onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
            }
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("article_picture", useUnmergedTree = true)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            val reads = onAllNodesWithText("Today’s reads").fetchSemanticsNodes().single()
            val today = onAllNodesWithText("Today").fetchSemanticsNodes().single()
            assertTrue(
                reads.boundsInRoot.top < today.boundsInRoot.top,
                "The news comes before the day's tasks",
            )
            capture("home-todays-reads")
            onNodeWithText("All news").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("The reading corner").fetchSemanticsNodes().isNotEmpty() &&
                    onAllNodesWithTag("article_picture", useUnmergedTree = true)
                        .fetchSemanticsNodes()
                        .isNotEmpty()
            }
            capture("news-list-pictures")
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
