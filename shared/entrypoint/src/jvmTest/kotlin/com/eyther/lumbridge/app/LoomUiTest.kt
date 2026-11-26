package com.eyther.lumbridge.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
import com.eyther.lumbridge.app.testutil.capture
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LoomUiTest {

    @Test
    fun invalidFeedErrorsStayInsideTheSheetAndClearAfterAFollowSucceeds() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            ThemePreferences.set("LIGHT")
            setContent {
                Box(Modifier.requiredSize(440.dp, 760.dp).testTag("loom_app")) { LoomDemoApp() }
            }
            waitUntil(timeoutMillis = 10_000) {
                onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty() &&
                    onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("navigation_more").performClick()
            onNodeWithText("Feed").performClick()
            onNodeWithText("Dismiss").assertDoesNotExist()
            awaitLoadedDestination(tag = "manage_feeds")
            onNodeWithTag("manage_feeds").performClick()
            awaitLoadedDestination(tag = "feed_subscriptions")
            onNodeWithText("Add a feed").performClick()
            onNodeWithText("https://…").performTextInput("https://")
            onNodeWithTag("save_feed_subscription").performClick()

            val invalidUrlMessage = "Enter a secure RSS or Atom feed URL beginning with https://."
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText(invalidUrlMessage).fetchSemanticsNodes().isNotEmpty()
            }
            onAllNodesWithText(invalidUrlMessage).assertCountEquals(1)
            onNodeWithText("https://…").performTextReplacement("https://example.com/feed")
            onNodeWithTag("save_feed_subscription").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("save_feed_subscription").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithText(invalidUrlMessage).assertDoesNotExist()
            onNodeWithText("Dismiss").assertDoesNotExist()
        }

    @Test
    fun inlineShoppingKeepsItsDraftWhenVisitingMyAccount() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            ThemePreferences.set("LIGHT")
            setContent {
                Box(Modifier.requiredSize(440.dp, 760.dp).testTag("loom_app")) { LoomDemoApp() }
            }
            waitUntil(timeoutMillis = 10000) {
                onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty() &&
                    onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
            }
            capture("home-light", tag = "loom_app")
            onNodeWithTag("navigation_shopping").performClick()
            awaitLoadedDestination(tag = "shopping_content")
            onNodeWithTag("shopping_inline_input")
                .performScrollTo()
                .performTextInput("Barista oat drink")
            onNodeWithTag("shopping_inline_input").performImeAction()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Barista oat drink").fetchSemanticsNodes().isNotEmpty()
            }
            capture("shopping-light", tag = "loom_app")
            onNodeWithTag("shopping_inline_input").performTextInput("Keep this draft")
            onNodeWithTag("open_my_account").performClick()
            onNodeWithText("My account").assertExists()
            onNodeWithContentDescription("Back").performClick()
            onNodeWithTag("shopping_inline_input").assertTextContains("Keep this draft")
            onNodeWithContentDescription("Back").assertDoesNotExist()
        }

    @Test
    fun expenseEntryFromHomeReturnsHomeAndAppearsInTheEverydayHistory() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            ThemePreferences.set("LIGHT")
            setContent {
                Box(Modifier.requiredSize(440.dp, 760.dp).testTag("loom_app")) { LoomDemoApp() }
            }
            waitUntil(timeoutMillis = 10000) {
                onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty() &&
                    onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithText("Add expense").performScrollTo().performClick()
            onNodeWithTag("expense_title").performTextInput("Streaming service")
            onNodeWithTag("expense_amount").performTextInput("15.00")
            capture("expense-form-light", tag = "loom_app")
            onNodeWithTag("save_expense").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty() &&
                    onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithTag("expenses_content").performScrollToNode(hasText("Streaming service"))
            onNodeWithText("Streaming service").assertExists()
            capture("money-light", tag = "loom_app")
            onNodeWithTag("balances_view_balances").performClick()
            onNodeWithText("Who owes whom").assertExists()
            capture("balances-light", tag = "loom_app")
            onNodeWithContentDescription("Back").assertDoesNotExist()
            onNodeWithTag("open_my_account").performClick()
            onNodeWithTag("account_content").performScrollToNode(hasTestTag("usage_data_settings"))
            onNodeWithTag("usage_data_settings").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("consent_analytics").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("consent_analytics").assertIsOff()
            onNodeWithTag("consent_analytics").performClick().assertIsOn()
            onNodeWithTag("consent_analytics").performClick().assertIsOff()
            onNodeWithTag("consent_save").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("usage_data_consent").fetchSemanticsNodes().isEmpty()
            }
            capture("settings-privacy", tag = "loom_app")
            onNodeWithTag("account_content").performScrollToNode(hasTestTag("delete_account"))
            onNodeWithTag("sign_out").assertExists()
            onNodeWithTag("delete_account").assertExists()
            capture("account-actions-light", tag = "loom_app")
            onNodeWithTag("appearance_preference").performScrollTo().performClick()
            onNodeWithText("Dark", useUnmergedTree = true).performClick()
            onNodeWithContentDescription("Back").performClick()
            onNodeWithTag("balances_view_expenses").performClick()
            capture("money-dark", tag = "loom_app")
            onNodeWithTag("navigation_home").performClick()
            capture("home-dark", tag = "loom_app")
        }

}
