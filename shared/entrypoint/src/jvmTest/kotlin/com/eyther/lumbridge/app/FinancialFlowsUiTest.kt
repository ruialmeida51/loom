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
import com.eyther.lumbridge.app.demo.LoomDemoApp
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
            onNodeWithTag("expense_accounts").performClick()
            onNodeWithText("New trip or event").performClick()
            onNodeWithText("Account name").performTextInput("Lisbon weekend")
            onNodeWithText("GBP").performClick()
            onNodeWithText("Save account").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Lisbon weekend").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("add_expense").performClick()
            onNodeWithTag("expense_title").performTextInput("Beach groceries")
            onNodeWithTag("expense_amount").performTextInput("30.00")
            onNodeWithText("Split").performClick()
            onNodeWithText("By shares").performClick()
            onNodeWithContentDescription("Share for You").performTextReplacement("2")
            capture("split-shares-light")
            onNodeWithText("Done").performClick()
            onNodeWithText("Save expense").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Beach groceries").fetchSemanticsNodes().isNotEmpty()
            }
            selectAccount("Everyday")
            onNodeWithText("Beach groceries").assertDoesNotExist()
            selectAccount("Lisbon weekend")
            onNodeWithText("Beach groceries").assertExists()
            onNodeWithTag("balances_view_balances").performClick()
            capture("trip-balances-light")
            onAllNodesWithText("Record repayment").onFirst().performClick()
            onNodeWithTag("repayment_amount").assertTextContains("7.50")
            capture("repayment-light")
            onNodeWithTag("record_repayment_submit").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Record a repayment").fetchSemanticsNodes().isEmpty()
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
            onNodeWithText("Save expense").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Today").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("navigation_money").performClick()
            onNodeWithTag("expenses_content").performScrollToNode(hasText("Electrician"))
            onNodeWithText("Electrician").performClick()
            onNodeWithText("Edit expense").performClick()
            onNodeWithTag("expense_title").performTextReplacement("Electrician final")
            onNodeWithTag("expense_amount").performTextReplacement("18.00")
            onNodeWithText("Save expense").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Expense details").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithText("Electrician final").assertExists()
            onNodeWithText("€18.00").assertExists()
            capture("expense-details-light")
            onNodeWithText("Delete expense").performClick()
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
            onNodeWithText("Percentages").performClick()
            onNodeWithContentDescription("Share for You").performTextReplacement("60")
            onNodeWithContentDescription("Share for Alex").performTextReplacement("40")
            onNodeWithContentDescription("Share for Sam").performTextReplacement("0")
            onNodeWithText("Done").performClick()
            onNodeWithText("€7.20").assertExists()
            onNodeWithText("Split").performClick()
            onNodeWithText("Exact amounts").performClick()
            onNodeWithContentDescription("Share for You").performTextReplacement("6.00")
            onNodeWithContentDescription("Share for Alex").performTextReplacement("6.00")
            onNodeWithContentDescription("Share for Sam").performTextReplacement("0.00")
            onNodeWithText("Done").performClick()
            onNodeWithTag("expense_title").assertTextContains("Shared dinner")
            onNodeWithText("More details").performClick()
            onNodeWithText("Date").performScrollTo().performClick()
            onNodeWithTag("form_sheet").assertIsDisplayed()
            capture("expense-date-light")
            onNodeWithText("Done").performClick()
            onNodeWithTag("expense_amount").assertTextContains("12.00")
        }

    @Test
    fun tripRecurringBillsCanBePausedResumedAndCancelled() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            startApp()
            onNodeWithTag("navigation_money").performClick()
            onNodeWithTag("expense_accounts").performClick()
            onNodeWithText("New trip or event").performClick()
            onNodeWithText("Account name").performTextInput("Summer rental")
            onNodeWithText("GBP").performClick()
            onNodeWithText("Save account").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Summer rental").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("add_expense").performClick()
            onNodeWithTag("expense_title").performTextInput("Cottage rent")
            onNodeWithTag("expense_amount").performTextInput("90.00")
            onNodeWithText("More details").performClick()
            onNodeWithTag("expense_recurring").performScrollTo().performClick()
            onNodeWithText("Save expense").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Cottage rent").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("balances_actions").performClick()
            onNodeWithText("Recurring expenses").performClick()
            onNodeWithText("£90.00", substring = true).assertExists()
            onNodeWithTag("toggle_recurring_Cottage rent").performScrollTo().performClick()
            onNodeWithText("Paused").assertExists()
            onNodeWithTag("toggle_recurring_Cottage rent").performClick()
            onNodeWithText("Active").assertExists()
            capture("trip-recurring-light")
            onNodeWithTag("cancel_recurring_Cottage rent").performClick()
            onNodeWithTag("confirm_cancel_recurring").performClick()
            onNodeWithText("Cancelled").assertExists()
            onNodeWithContentDescription("Back").performClick()
            selectAccount("Everyday")
            onNodeWithText("Cottage rent").assertDoesNotExist()
        }

    private fun ComposeUiTest.startApp() {
        ThemePreferences.set("LIGHT")
        setContent { Box(Modifier.fillMaxSize().testTag("loom_app")) { LoomDemoApp() } }
        waitUntil(timeoutMillis = 10000) {
            onAllNodesWithText("Today").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun ComposeUiTest.selectAccount(name: String) {
        onNodeWithTag("expense_accounts").performClick()
        onNode(hasText(name) and hasAnyAncestor(hasTestTag("modal_sheet"))).performClick()
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithText("Expense accounts").fetchSemanticsNodes().isEmpty()
        }
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
