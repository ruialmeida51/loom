package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.Density
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

@OptIn(ExperimentalTestApi::class)
class NestingAndReceiptsUiTest {
    @Test
    fun swipesGroupItemsAndParentDeletionPromotesChildrenWhileUndoRestoresThem() =
        runSkikoComposeUiTest(size = Size(390f, 800f), density = Density(1f, 1.2f)) {
            ThemePreferences.set("LIGHT")
            setContent { LoomDemoApp() }
            awaitLoadedDestination("home_content")
            onNodeWithTag("navigation_shopping").performClick()
            awaitLoadedDestination("shopping_content")
            onNodeWithTag("edit_Cherry tomatoes").performTouchInput { swipeRight() }
            waitUntil(timeoutMillis = 5_000) { indent("Cherry tomatoes", "Avocados") > 20f }
            capture("shopping-nested-light")
            onNodeWithTag("check_Avocados").performClick()
            onNodeWithTag("check_Cherry tomatoes").assertDoesNotExist()
            onNodeWithText("Completed", substring = true).performClick()
            onNodeWithTag("check_Cherry tomatoes").assertExists()
            onNodeWithTag("actions_Avocados").performScrollTo().performClick()
            onNodeWithText("Remove").performClick()
            onNodeWithTag("confirm_delete_grocery").performClick()
            waitUntil(timeoutMillis = 5_000) { abs(indentFromComposer("Cherry tomatoes")) < 2f }
            onNodeWithTag("check_Avocados").assertDoesNotExist()
            onNodeWithText("Undo").performClick()
            waitUntil(timeoutMillis = 5_000) { indent("Cherry tomatoes", "Avocados") > 20f }
            onNodeWithTag("edit_Cherry tomatoes").performTouchInput { swipeLeft() }
            waitUntil(timeoutMillis = 5_000) { abs(indent("Cherry tomatoes", "Avocados")) < 2f }
        }

    @Test
    fun receiptOpensInItsOwnViewerAndDeletionNeedsConfirmation() =
        runSkikoComposeUiTest(size = Size(360f, 780f), density = Density(1f, 1.3f)) {
            ThemePreferences.set("DARK")
            setContent { LoomDemoApp() }
            awaitLoadedDestination("home_content")
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination("expenses_content")
            onNodeWithText("The weekly shop").performClick()
            awaitLoadedDestination("expense_details_content")
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("receipt_thumbnail").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("receipt_thumbnail").performScrollTo().assertIsDisplayed()
            capture("receipt-preview-dark-narrow")
            onNodeWithTag("receipt_thumbnail").performClick()
            onNodeWithTag("receipt_viewer").assertExists()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("receipt_zoom_reset").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithContentDescription("Zoom in").performClick()
            onNodeWithTag("receipt_zoom_reset").performClick()
            capture("receipt-viewer-dark-narrow")
            onNodeWithContentDescription("Back").performClick()
            awaitLoadedDestination("expense_details_content")
            onNodeWithTag("receipt_options").performScrollTo().performClick()
            onNodeWithText("Delete").performClick()
            onNodeWithTag("confirm_delete_receipt").assertIsDisplayed()
            onNodeWithText("Cancel").performClick()
            onNodeWithText("View photo").assertExists()
            onNodeWithTag("receipt_options").performClick()
            onNodeWithText("Delete").performClick()
            onNodeWithTag("confirm_delete_receipt").performClick()
            onNodeWithTag("confirm_delete_receipt").assertDoesNotExist()
            onNodeWithText("Receipt deleted").assertExists()
            onNodeWithText("Add receipt").assertExists()
        }

    @Test
    fun favouritesAreVisibleAndCanRestoreARemovedItemWithoutOpeningAnItemForm() =
        runSkikoComposeUiTest(size = Size(390f, 800f), density = Density(1f, 1.2f)) {
            ThemePreferences.set("LIGHT")
            setContent { LoomDemoApp() }
            awaitLoadedDestination("home_content")
            onNodeWithTag("navigation_shopping").performClick()
            awaitLoadedDestination("shopping_content")
            onNodeWithTag("actions_Avocados").performClick()
            onNodeWithText("Add to favourites").performClick()
            onNodeWithTag("actions_Avocados").performClick()
            onNodeWithText("Remove").performClick()
            onNodeWithTag("confirm_delete_grocery").performClick()
            onNodeWithText("Undo").assertExists()
            onNodeWithTag("shopping_favorites").performScrollTo().performClick()
            onNodeWithTag("favorite_Avocados").performClick()
            onNodeWithTag("favorite_Avocados").assertTextContains("Already on this list")
            capture("shopping-favourites-light")
        }

    private fun SkikoComposeUiTest.indent(child: String, parent: String): Float =
        onNodeWithTag("check_$child").fetchSemanticsNode().boundsInWindow.left -
            onNodeWithTag("check_$parent").fetchSemanticsNode().boundsInWindow.left

    private fun SkikoComposeUiTest.indentFromComposer(child: String): Float =
        onNodeWithTag("check_$child").fetchSemanticsNode().boundsInWindow.left -
            onNodeWithTag("shopping_entry_checkbox").fetchSemanticsNode().boundsInWindow.left

    private fun SkikoComposeUiTest.capture(name: String) {
        waitForIdle()
        val folder =
            File(System.getProperty("loom.screenshots.dir", "build/screenshots")).apply { mkdirs() }
        File(folder, "$name.png")
            .writeBytes(
                requireNotNull(
                        Image.makeFromBitmap(captureToImage().asSkiaBitmap())
                            .encodeToData(EncodedImageFormat.PNG),
                    )
                    .bytes,
            )
    }
}
