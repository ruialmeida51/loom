package com.eyther.lumbridge.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import java.io.File
import kotlin.test.Test
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

@OptIn(ExperimentalTestApi::class)
class FinancialFlowsUiTest {
    @Test
    fun tripSpendingUsesItsOwnCurrencyAndWeightedRepayments() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            startApp()
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithTag("expense_accounts").performClick()
            onNodeWithText("New trip or event").performClick()
            onNodeWithText("Account name").performTextInput("Lisbon weekend")
            onNodeWithText("GBP").performClick()
            onNodeWithTag("save_expense_account").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Lisbon weekend").fetchSemanticsNodes().isNotEmpty()
            }
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithTag("add_expense").performClick()
            onNodeWithTag("expense_title").performTextInput("Beach groceries")
            onNodeWithTag("expense_amount").performTextInput("30.00")
            onNodeWithText("Split").performClick()
            onNodeWithTag("split_method").performClick()
            onNodeWithText("By shares").performClick()
            onNodeWithContentDescription("Share for You").performTextReplacement("2")
            capture("split-shares-light")
            onNodeWithTag("confirm_expense_split").performClick()
            onNodeWithTag("save_expense").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Beach groceries").fetchSemanticsNodes().isNotEmpty()
            }
            selectAccount("Everyday")
            onNodeWithText("Beach groceries").assertDoesNotExist()
            selectAccount("Lisbon weekend")
            onNodeWithText("Beach groceries").assertExists()
            onNodeWithTag("balances_view_balances").performClick()
            capture("trip-balances-light")
            onAllNodesWithTag("record_payment").onFirst().performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("repayment_amount").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("repayment_amount").assertTextContains("7.50")
            capture("repayment-light")
            onNodeWithTag("record_repayment_submit").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("record_repayment_submit").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("expense_accounts").performClick()
            capture("trip-accounts-light")
        }

    @Test
    fun expenseDetailsEditAndDeleteReturnToTheSameAccount() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            startApp()
            onNodeWithText("Add expense").performScrollTo().performClick()
            onNodeWithTag("expense_title").performTextInput("Electrician")
            onNodeWithTag("expense_amount").performTextInput("12.00")
            onNodeWithTag("save_expense").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty() &&
                    onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithTag("expenses_content").performScrollToNode(hasText("Electrician"))
            onNodeWithText("Electrician").performClick()
            awaitLoadedDestination(tag = "expense_details_content")
            onNodeWithTag("expense_actions").performClick()
            onNodeWithText("Edit").performClick()
            onNodeWithTag("expense_title").performTextReplacement("Electrician final")
            onNodeWithTag("expense_amount").performTextReplacement("18.00")
            onNodeWithTag("save_expense").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Expense details").fetchSemanticsNodes().isNotEmpty()
            }
            awaitLoadedDestination(tag = "expense_details_content")
            onNodeWithText("Electrician final").assertExists()
            onNodeWithText("€18.00").assertExists()
            capture("expense-details-light")
            onNodeWithTag("expense_actions").performClick()
            onNodeWithText("Delete").performClick()
            onNodeWithTag("confirm_delete_expense").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Expense details").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("expenses_content").assertIsDisplayed()
            onNodeWithText("Electrician final").assertDoesNotExist()
            onNodeWithContentDescription("Back").assertDoesNotExist()
        }

    @Test
    fun percentagesAndExactAmountsHaveFocusedEditorsAndPreserveTheDraft() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            startApp()
            onNodeWithText("Add expense").performScrollTo().performClick()
            onNodeWithTag("expense_title").performTextInput("Shared dinner")
            onNodeWithTag("expense_amount").performTextInput("12.00")
            onNodeWithText("Split").performClick()
            onNodeWithTag("split_method").performClick()
            onNodeWithText("Percentages").performClick()
            onNodeWithContentDescription("Share for You").performTextReplacement("60")
            onNodeWithContentDescription("Share for Alex").performTextReplacement("40")
            onNodeWithContentDescription("Share for Sam").performTextReplacement("0")
            onNodeWithTag("confirm_expense_split").performClick()
            onNodeWithText("Percentages", substring = true).assertExists()
            onNodeWithText("Split").performClick()
            onNodeWithText("€7.20").assertExists()
            onNodeWithContentDescription("Share for You").assertTextContains("60")
            onNodeWithTag("split_method").performClick()
            onNodeWithText("Exact amounts").performClick()
            onNodeWithContentDescription("Share for You").performTextReplacement("6.00")
            onNodeWithContentDescription("Share for Alex").performTextReplacement("6.00")
            onNodeWithContentDescription("Share for Sam").performTextReplacement("0.00")
            onNodeWithTag("confirm_expense_split").performClick()
            onNodeWithTag("expense_title").assertTextContains("Shared dinner")
            onNodeWithText("Category").assertExists()
            onNodeWithText("Date").performScrollTo().performClick()
            onNodeWithTag("form_sheet").assertIsDisplayed()
            capture("expense-date-light")
            onNodeWithTag("confirm_expense_date").performClick()
            onNodeWithTag("expense_amount").assertTextContains("12.00")
        }

    @Test
    fun splitControlsAndFooterStayReachableOnANarrowScreenWithLargerText() =
        runSkikoComposeUiTest(size = Size(360f, 640f), density = Density(1f, 1.4f)) {
            startApp()
            onNodeWithTag("home_add_expense").performScrollTo().performClick()
            onNodeWithTag("expense_amount").performTextInput("30.00")
            onNodeWithTag("expense_title").performTextInput("Weekend food")
            onNodeWithText("Split").performScrollTo().performClick()
            onNodeWithTag("split_method").performClick()
            onNodeWithText("By shares").performClick()
            onNodeWithContentDescription("Share for You")
                .performScrollTo()
                .performTextReplacement("2")
            onNodeWithTag("confirm_expense_split").assertIsDisplayed()
            capture("split-shares-narrow")
            onNodeWithTag("confirm_expense_split").performClick()
            onNodeWithTag("expense_title").assertTextContains("Weekend food")
            onNodeWithTag("save_expense").assertIsDisplayed()
        }

    @Test
    fun pausedTripBillsCanBeEditedResumedAndDeletedWithoutChangingPastExpenses() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            startApp()
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithTag("expense_accounts").performClick()
            onNodeWithText("New trip or event").performClick()
            onNodeWithText("Account name").performTextInput("Summer rental")
            onNodeWithText("GBP").performClick()
            onNodeWithTag("save_expense_account").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Summer rental").fetchSemanticsNodes().isNotEmpty()
            }
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithTag("add_expense").performClick()
            onNodeWithTag("expense_title").performTextInput("Cottage rent")
            onNodeWithTag("expense_amount").performTextInput("90.00")
            onNodeWithText("Category").assertExists()
            onNodeWithTag("expense_recurring").performScrollTo().performClick()
            onNodeWithTag("save_expense").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Cottage rent").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("recurring_bills_row").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("£90.00", substring = true).fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithText("£90.00", substring = true).assertExists()
            onNodeWithTag("recurring_actions_Cottage rent").performScrollTo().performClick()
            onNodeWithText("Pause").performClick()
            onNodeWithText("Paused").assertExists()
            onNodeWithTag("recurring_actions_Cottage rent").performClick()
            onNodeWithText("Edit").performClick()
            awaitLoadedDestination(tag = "recurring_form_content")
            onNodeWithTag("expense_amount").performTextReplacement("100.00")
            capture("recurring-edit-paused-light")
            onNodeWithTag("save_recurring").performClick()
            awaitLoadedDestination(tag = "recurring_content")
            onNodeWithText("Paused").assertExists()
            onNodeWithText("£100.00", substring = true).assertExists()
            onNodeWithTag("recurring_actions_Cottage rent").performClick()
            onNodeWithText("Resume").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Paused").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithText("Next on", substring = true).assertExists()
            capture("trip-recurring-light")
            onNodeWithTag("recurring_actions_Cottage rent").performClick()
            onNodeWithText("Delete").performClick()
            onNodeWithTag("confirm_delete_recurring").performClick()
            onNodeWithText("Cottage rent").assertDoesNotExist()
            onNodeWithContentDescription("Back").performClick()
            onNodeWithText("Cottage rent").assertExists()
            onNodeWithText("£90.00").assertExists()
            selectAccount("Everyday")
            onNodeWithText("Cottage rent").assertDoesNotExist()
        }

    private fun ComposeUiTest.startApp() {
        ThemePreferences.set("LIGHT")
        setContent { Box(Modifier.fillMaxSize().testTag("loom_app")) { LoomDemoApp() } }
        waitUntil(timeoutMillis = 10000) {
            onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty() &&
                onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
        }
    }

    private fun ComposeUiTest.selectAccount(name: String) {
        onNodeWithTag("expense_accounts").performClick()
        onNode(hasText(name) and hasAnyAncestor(hasTestTag("modal_sheet"))).performClick()
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithText("Expense accounts").fetchSemanticsNodes().isEmpty()
        }
        awaitLoadedDestination(tag = "expenses_content")
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
