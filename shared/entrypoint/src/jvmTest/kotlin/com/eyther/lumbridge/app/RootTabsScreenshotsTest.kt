package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
import java.io.File
import kotlin.test.Test
import org.jetbrains.skia.EncodedImageFormat

/** Comparable five-tab evidence uses one viewport and captures the real shared navigation flow. */
@OptIn(ExperimentalTestApi::class)
class RootTabsScreenshotsTest {
    @Test
    fun captureFiveTabsAtTheSameViewport() =
        runSkikoComposeUiTest(size = Size(440f, 800f)) {
            setContent { LoomDemoApp() }
            awaitLoadedDestination("home_content")
            val targets =
                listOf(
                    "home" to "home_content",
                    "shopping" to "shopping_content",
                    "money" to "expenses_content",
                    "tasks" to "tasks_content",
                    "more" to "more_content",
                )
            val directory =
                File(
                        System.getProperty("loom.screenshots.dir", "build/screenshots"),
                        "household-management",
                    )
                    .apply { mkdirs() }
            targets.forEach { (tab, content) ->
                onNodeWithTag("navigation_$tab").performClick()
                awaitLoadedDestination(content)
                val bytes =
                    onRoot().captureToImage().asSkiaBitmap().let {
                        org.jetbrains.skia.Image.makeFromBitmap(it)
                            .encodeToData(EncodedImageFormat.PNG)!!
                            .bytes
                    }
                File(directory, "root-$tab.png").writeBytes(bytes)
            }
        }
}
