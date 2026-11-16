package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class GroupManagementUiTest {
    @Test
    fun theFinalShoppingListCanBeDeletedAndReplacedWithoutShowingAnUnusableInput() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            startApp()
            onNodeWithTag("navigation_shopping").performClick()
            awaitLoadedDestination(tag = "shopping_content")
            onNodeWithTag("manage_lists").performClick()
            onNodeWithTag("list_actions_Groceries").performClick()
            onNodeWithText("Delete list").performClick()
            onNodeWithTag("confirm_delete_list").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Create list").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("shopping_inline_input").assertDoesNotExist()
            onNodeWithText("Create list").performClick()
            onNodeWithText("List name").performTextInput("Weekend groceries")
            onNodeWithText("Save list").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Weekend groceries").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("shopping_inline_input").assertIsDisplayed()
        }

    @Test
    fun archivedTripsLeaveTheNormalPickerAndCanBePermanentlyDeletedFromManagement() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            startApp()
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithTag("expense_accounts").performClick()
            onNodeWithText("New trip or event").performClick()
            onNodeWithText("Account name").performTextInput("Finished holiday")
            onNodeWithText("Save account").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Finished holiday").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("expense_accounts").performClick()
            onNodeWithText("Manage accounts").performScrollTo().performClick()
            onNodeWithTag("account_actions_Finished holiday").performClick()
            onNodeWithText("Archive account").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Archived").fetchSemanticsNodes().isNotEmpty()
            }
            onNode(hasText("Done") and hasAnyAncestor(hasTestTag("modal_sheet"))).performClick()
            onNodeWithTag("expense_accounts").performClick()
            onNode(hasText("Finished holiday") and hasAnyAncestor(hasTestTag("modal_sheet")))
                .assertDoesNotExist()
            onNodeWithText("Manage accounts").performScrollTo().performClick()
            onNodeWithTag("account_actions_Finished holiday").performClick()
            onNodeWithText("Delete account").performClick()
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
