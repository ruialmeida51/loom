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
class TripCurrencyUiTest {
    @Test
    fun anExpensePaidInPoundsShowsItsEuroValueAndKeepsThePounds() =
        runSkikoComposeUiTest(size = Size(440f, 900f)) {
            startApp()
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithTag("account_chip_Porto weekend").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Guesthouse").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("add_expense").performClick()
            onNodeWithTag("expense_amount").performTextInput("48.00")
            onNodeWithTag("expense_title").performTextInput("Fish market")
            onNodeWithTag("expense_currency").performClick()
            onNodeWithTag("currency_GBP").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("= €55.42 in this account").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithText("1 GBP = 1.154601 EUR · today's rate").assertExists()
            onNodeWithTag("save_expense").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("£48.00", substring = true).fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithText("€55.42").assertExists()
        }

    private fun ComposeUiTest.startApp() {
        setContent { LoomDemoApp() }
        waitUntil(timeoutMillis = 10_000) {
            onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
