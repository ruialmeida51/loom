package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
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
class AccountGuestsUiTest {
    @Test
    fun aTripGuestShowsInBalancesWithTheGuestTag() =
        runSkikoComposeUiTest(size = Size(440f, 900f)) {
            startApp()
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithTag("account_chip_Porto weekend").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Guesthouse").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("balances_view_balances").performClick()
            onNodeWithTag("member_balances_toggle").performScrollTo().performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Marta").fetchSemanticsNodes().isNotEmpty()
            }
            onAllNodesWithText("Guest").fetchSemanticsNodes().isNotEmpty().let(::check)
        }

    @Test
    fun anExpenseSplitWithAGuestSaves() =
        runSkikoComposeUiTest(size = Size(440f, 900f)) {
            startApp()
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithTag("account_chip_Porto weekend").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Guesthouse").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("add_expense").performClick()
            onNodeWithTag("expense_title").performTextInput("Port tasting")
            onNodeWithTag("expense_amount").performTextInput("30.00")
            onNodeWithText("3 people", substring = true).assertExists()
            onNodeWithTag("save_expense").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Port tasting").fetchSemanticsNodes().isNotEmpty() &&
                    onAllNodesWithTag("save_expense").fetchSemanticsNodes().isEmpty()
            }
        }

    @Test
    fun guestsAddedWhileCreatingATripAreSavedWithIt() =
        runSkikoComposeUiTest(size = Size(440f, 900f)) {
            startApp()
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithTag("expense_accounts").performClick()
            onNodeWithText("New trip or event").performClick()
            onNodeWithText("Account name").performTextInput("Lisbon")
            onNodeWithTag("add_account_guest").performScrollTo().performClick()
            onNodeWithTag("guest_name_field").performTextInput("Joana")
            onNodeWithTag("confirm_guest_name").performClick()
            onNodeWithTag("guest_Joana").assertExists()
            onNodeWithTag("save_expense_account").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("save_expense_account").fetchSemanticsNodes().isEmpty()
            }
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithTag("balances_view_balances").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("member_balances_toggle").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("member_balances_toggle").performScrollTo().performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Joana").fetchSemanticsNodes().isNotEmpty()
            }
        }

    private fun ComposeUiTest.startApp() {
        setContent { LoomDemoApp() }
        waitUntil(timeoutMillis = 10_000) {
            onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty() &&
                onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
        }
    }
}
