package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
import java.io.File
import kotlin.test.Test
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

@OptIn(ExperimentalTestApi::class)
class GroupManagementUiTest {
    @Test
    fun theFinalShoppingListCanBeDeletedAndReplacedWithoutShowingAnUnusableInput() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            startApp()
            onNodeWithTag("navigation_shopping").performClick()
            awaitLoadedDestination(tag = "shopping_content")
            onNodeWithTag("manage_lists").performClick()
            onNode(hasText("New list") and hasAnyAncestor(hasTestTag("modal_sheet"))).assertIsDisplayed()
            onNodeWithText("Cancel").assertDoesNotExist()
            onNodeWithText("Done").assertDoesNotExist()
            onNodeWithTag("list_Groceries").assertIsSelected()
            waitForIdle()
            val screenshot = Image.makeFromBitmap(captureToImage().asSkiaBitmap())
            File(
                    System.getProperty("loom.screenshots.dir", "build/screenshots"),
                    "shopping-lists-light.png",
                )
                .apply { parentFile.mkdirs() }
                .writeBytes(checkNotNull(screenshot.encodeToData(EncodedImageFormat.PNG)).bytes)
            onNodeWithTag("list_actions_Groceries").performClick()
            onNodeWithText("Delete").performClick()
            onNodeWithTag("confirm_delete_list").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Create list").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("shopping_inline_input").assertDoesNotExist()
            onNodeWithText("Create list").performClick()
            onNodeWithText("List name").performTextInput("Weekend groceries")
            onNodeWithTag("save_shopping_list").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Weekend groceries").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("shopping_inline_input").assertIsDisplayed()
        }

    @Test
    fun aNewShoppingListKeepsTheIconChosenForIt() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            startApp()
            onNodeWithTag("navigation_shopping").performClick()
            awaitLoadedDestination(tag = "shopping_content")
            onNodeWithTag("shopping_new_list").performClick()
            onNodeWithText("List name").performTextInput("Holiday")
            onNodeWithTag("list_icon_travel").performClick().assertIsSelected()
            waitForIdle()
            val screenshot = Image.makeFromBitmap(captureToImage().asSkiaBitmap())
            File(
                    System.getProperty("loom.screenshots.dir", "build/screenshots"),
                    "shopping-list-icon-light.png",
                )
                .apply { parentFile.mkdirs() }
                .writeBytes(checkNotNull(screenshot.encodeToData(EncodedImageFormat.PNG)).bytes)
            onNodeWithTag("save_shopping_list").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("list_chip_Holiday").fetchSemanticsNodes().isNotEmpty() ||
                    onAllNodesWithText("Holiday").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("manage_lists").performClick()
            onNodeWithTag("list_actions_Holiday").performClick()
            onNodeWithText("Rename").performClick()
            onNodeWithTag("list_icon_travel").assertIsSelected()
            onNodeWithTag("list_icon_bag").assertIsNotSelected()
        }

    @Test
    fun archivedTripsHaveTheirOwnSectionAndCanBeDeletedInTheSamePicker() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            startApp()
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithTag("expense_accounts").performClick()
            onNodeWithText("New trip or event").performClick()
            onNodeWithText("Account name").performTextInput("Finished holiday")
            onNodeWithTag("save_expense_account").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Finished holiday").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("expense_accounts").performClick()
            onNodeWithTag("account_actions_Finished holiday").performClick()
            onNodeWithText("Archive account").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Archived").fetchSemanticsNodes().isNotEmpty()
            }
            onNode(hasText("Finished holiday") and hasAnyAncestor(hasTestTag("modal_sheet")))
                .assertExists()
            onNode(hasText("Close") and hasAnyAncestor(hasTestTag("modal_sheet")))
                .assertDoesNotExist()
            onNodeWithTag("account_actions_Finished holiday").performClick()
            onNodeWithText("Delete").performClick()
            onNodeWithTag("confirm_delete_account").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Finished holiday").fetchSemanticsNodes().isEmpty()
            }
            onNode(hasText("Everyday") and hasAnyAncestor(hasTestTag("modal_sheet"))).assertExists()
        }

    private fun ComposeUiTest.startApp() {
        setContent { LoomDemoApp() }
        waitUntil(timeoutMillis = 10_000) {
            onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty() &&
                onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
        }
    }
}
