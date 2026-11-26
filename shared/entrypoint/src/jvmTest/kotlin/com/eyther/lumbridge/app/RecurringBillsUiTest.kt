package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOn
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
class RecurringBillsUiTest {
    @Test
    fun anExpenseStartedFromHomeNamesItsAccountAndCanMoveToATrip() =
        runSkikoComposeUiTest(size = Size(440f, 900f)) {
            startApp()
            onNodeWithTag("home_add_expense").performScrollTo().performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("expense_account_picker").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithText("Adding to").assertExists()
            onNodeWithTag("expense_account_picker").performClick()
            onNodeWithTag("expense_account_Porto weekend").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Porto weekend").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("expense_title").performTextInput("Port tasting")
            onNodeWithTag("expense_amount").performTextInput("30.00")
            onNodeWithTag("save_expense").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("save_expense").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithTag("account_chip_Porto weekend").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Port tasting").fetchSemanticsNodes().isNotEmpty()
            }
        }

    @Test
    fun recurringBillsAreOneTapFromTheLedgerAndNewOnesStartRepeating() =
        runSkikoComposeUiTest(size = Size(440f, 900f)) {
            startApp()
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithText("next: Internet", substring = true).assertExists()
            onNodeWithTag("recurring_bills_row").performClick()
            awaitLoadedDestination(tag = "recurring_content")
            onNodeWithTag("new_recurring_bill").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("expense_recurring").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("expense_recurring").performScrollTo().assertIsOn()
            onNodeWithText("of every month", substring = true).assertExists()
        }

    private fun ComposeUiTest.startApp() {
        setContent { LoomDemoApp() }
        waitUntil(timeoutMillis = 10_000) {
            onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty() &&
                onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
        }
    }
}
