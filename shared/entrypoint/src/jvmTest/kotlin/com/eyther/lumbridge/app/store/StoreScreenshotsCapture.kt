package com.eyther.lumbridge.app.store

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import java.io.File
import kotlin.test.Test
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

/**
 * Raw phone captures for the store listings, from the demo household's sample data, so any
 * machine produces the same images without an emulator, account or backend. Framing and captions
 * are added by tools/store-screenshots/frame.py; see that folder's README.
 *
 * Runs only on request: `./gradlew :shared:entrypoint:jvmTest --tests '*StoreScreenshotsCapture*'
 * -PloomStoreCapture=true`. Otherwise it returns at once, so CI never writes listing images.
 */
@OptIn(ExperimentalTestApi::class)
class StoreScreenshotsCapture {
    private val enabled = System.getProperty("loom.store.capture") == "true"
    private val directory by lazy {
        File(System.getProperty("loom.store.dir", "build/store-screenshots/raw")).apply { mkdirs() }
    }

    @Test
    fun captureListingScreens() {
        if (!enabled) return
        // 1080 x 2340 px at 2.625x: a common Android phone (411 x 891 dp).
        runSkikoComposeUiTest(size = Size(1080f, 2340f), density = Density(2.625f)) {
            ThemePreferences.set("LIGHT")
            setContent { LoomDemoApp(showPreviewLabel = false) }
            waitUntil(timeoutMillis = 10_000) {
                onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty() &&
                    onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
            }
            awaitPictures()
            save("home")

            onNodeWithText("All news").tap()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("feed_content").fetchSemanticsNodes().isNotEmpty()
            }
            awaitPictures()
            save("news")
            onNodeWithContentDescription("Back").tap()

            onNodeWithTag("navigation_shopping").tap()
            awaitLoadedDestination(tag = "shopping_content")
            save("shopping")

            onNodeWithTag("navigation_tasks").tap()
            awaitLoadedDestination(tag = "tasks_content")
            save("tasks")

            onNodeWithTag("navigation_money").tap()
            awaitLoadedDestination(tag = "expenses_content")
            save("money")

            onNodeWithTag("account_chip_Porto weekend").tap()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("Guesthouse").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("add_expense").tap()
            onNodeWithTag("expense_amount").performTextInput("48.00")
            onNodeWithTag("expense_title").performTextInput("Fish market dinner")
            onNodeWithTag("expense_currency").tap()
            onNodeWithTag("currency_GBP").tap()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("in this account", substring = true)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            save("trip")
        }
    }

    /** A touch tap: a mouse click would leave a hover highlight on the captured screen. */
    private fun SemanticsNodeInteraction.tap() {
        performTouchInput { click() }
    }

    private fun SkikoComposeUiTest.awaitPictures() {
        waitUntil(timeoutMillis = 5_000) {
            onAllNodesWithTag("article_picture", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        waitForIdle()
    }

    private fun SkikoComposeUiTest.save(name: String) {
        waitForIdle()
        File(directory, "$name.png")
            .writeBytes(
                Image.makeFromBitmap(captureToImage().asSkiaBitmap())
                    .encodeToData(EncodedImageFormat.PNG)!!
                    .bytes,
            )
    }
}
