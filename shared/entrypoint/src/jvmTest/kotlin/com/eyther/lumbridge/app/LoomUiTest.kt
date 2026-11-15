package com.eyther.lumbridge.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
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
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import java.io.File
import kotlin.test.Test
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

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
                onAllNodesWithText("Today").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("navigation_more").performClick()
            onNodeWithText("Feed").performClick()
            onNodeWithText("Dismiss").assertDoesNotExist()
            onNodeWithText("+ Follow a feed").performClick()
            onNodeWithText("https://…").performTextInput("https://")
            onNodeWithText("Follow feed").performClick()

            val invalidUrlMessage = "Enter a secure RSS or Atom feed URL beginning with https://."
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText(invalidUrlMessage).fetchSemanticsNodes().isNotEmpty()
            }
            onAllNodesWithText(invalidUrlMessage).assertCountEquals(1)
            onNodeWithText("https://…").performTextReplacement("https://example.com/feed")
            onNodeWithText("Follow feed").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("Follow feed").fetchSemanticsNodes().isEmpty()
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
                onAllNodesWithText("Today").fetchSemanticsNodes().isNotEmpty()
            }
            capture("home-light")
            onNodeWithTag("navigation_shopping").performClick()
            onNodeWithTag("shopping_inline_input")
                .performScrollTo()
                .performTextInput("Barista oat drink")
            onNodeWithTag("shopping_inline_input").performImeAction()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Barista oat drink").fetchSemanticsNodes().isNotEmpty()
            }
            capture("shopping-light")
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
                onAllNodesWithText("Today").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithText("Add expense").performScrollTo().performClick()
            onNodeWithTag("expense_title").performTextInput("Streaming service")
            onNodeWithTag("expense_amount").performTextInput("15.00")
            capture("expense-form-light")
            onNodeWithText("Save expense").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Today").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("navigation_money").performClick()
            onNodeWithTag("expenses_content").performScrollToNode(hasText("Streaming service"))
            onNodeWithText("Streaming service").assertExists()
            capture("money-light")
            onNodeWithTag("balances_view_balances").performClick()
            onNodeWithText("Who pays whom").assertExists()
            capture("balances-light")
            onNodeWithContentDescription("Back").assertDoesNotExist()
            onNodeWithTag("open_my_account").performClick()
            onNodeWithTag("account_content").performScrollToNode(hasTestTag("analytics_consent"))
            onNodeWithTag("analytics_consent").assertIsOff()
            onNodeWithTag("analytics_consent").performClick().assertIsOn()
            onNodeWithTag("analytics_consent").performClick().assertIsOff()
            capture("settings-privacy")
            onNodeWithTag("account_content").performScrollToNode(hasText("Dark"))
            onNodeWithText("Dark", useUnmergedTree = true).performClick()
            onNodeWithContentDescription("Back").performClick()
            capture("money-dark")
            onNodeWithTag("navigation_home").performClick()
            capture("home-dark")
        }

    private fun ComposeUiTest.capture(name: String) {
        waitForIdle()
        val bitmap = onNodeWithTag("loom_app").captureToImage()
        val directory =
            File(System.getProperty("loom.screenshots.dir", "build/screenshots")).apply { mkdirs() }
        File(directory, "$name.png")
            .writeBytes(
                Image.makeFromBitmap(bitmap.asSkiaBitmap())
                    .encodeToData(EncodedImageFormat.PNG)!!
                    .bytes,
            )
    }
}
