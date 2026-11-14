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
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
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
                onAllNodesWithText("Home, in harmony.").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("navigation_feed").performClick()
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
    fun householdShoppingRecurringAndThemesWork() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            ThemePreferences.set("LIGHT")
            setContent {
                Box(Modifier.requiredSize(440.dp, 760.dp).testTag("loom_app")) { LoomDemoApp() }
            }
            waitUntil(timeoutMillis = 10_000) {
                onAllNodesWithText("Home, in harmony.").fetchSemanticsNodes().isNotEmpty()
            }
            capture("home-light")
            onNodeWithTag("navigation_shopping").performClick()
            onNodeWithText("Add an item").performClick()
            onNodeWithText("What do we need?").performTextInput("Lemons")
            onNodeWithText("Quantity or note").performTextInput("4")
            onNodeWithText("Add item").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("Add item").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("shopping_content").performScrollToNode(hasText("Lemons"))
            onNodeWithTag("edit_Lemons").performClick()
            onNodeWithText("What do we need?").performTextReplacement("Organic lemons")
            onNodeWithText("Quantity or note").performTextReplacement("6")
            onNodeWithText("Save item").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("Save item").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("shopping_content").performScrollToNode(hasText("Organic lemons"))
            onNodeWithTag("actions_Organic lemons").performClick()
            onNodeWithText("Remove").performClick()
            onNodeWithTag("shopping_content").performScrollToIndex(0)
            onNodeWithText("Undo").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("Undo").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("shopping_content").performScrollToNode(hasText("Organic lemons"))
            onNodeWithText("Organic lemons").assertExists()
            onNodeWithTag("shopping_content").performScrollToIndex(0)
            capture("shopping-light")
            onNodeWithTag("manage_lists").performClick()
            onNodeWithText("New list").performClick()
            onNodeWithText("List name").performTextInput("Groceries")
            onNodeWithText("Save list").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("A list with this name already exists.")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            onNodeWithText("List name").performTextReplacement("Pharmacy")
            onNodeWithText("Save list").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("Save list").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithText("Add an item").performClick()
            onNodeWithText("What do we need?").performTextInput("Bandages\nAntiseptic")
            onNodeWithTag("category_Household").performClick()
            onNodeWithText("Add item").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("Add item").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("shopping_content").performScrollToNode(hasText("Bandages"))
            onNodeWithText("Bandages").assertExists()
            onNodeWithText("Organic lemons").assertDoesNotExist()
            onNodeWithTag("shopping_content").performScrollToIndex(0)
            capture("shopping-lists-light")
            onNodeWithTag("manage_lists").performClick()
            onNodeWithTag("rename_Pharmacy").performClick()
            onNodeWithText("List name").performTextReplacement("First aid")
            onNodeWithText("Save list").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("Save list").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("manage_lists").performClick()
            onNodeWithTag("archive_First aid").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("Restore").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("archive_First aid").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("Restore").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithText("Done").performClick()
            onNodeWithTag("navigation_money").performClick()
            onNodeWithText("Add expense").performClick()
            onNodeWithText("What was it for?").performTextInput("Streaming service")
            onNodeWithText("Amount · EUR").performTextInput("15.00")
            onNodeWithText("Date paid · YYYY-MM-DD").performTextReplacement("2026-11-01")
            onNodeWithTag("expense_recurring").performScrollTo().performClick()
            onNodeWithText("Save expense").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("Save expense").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithText("Recurring expenses").performClick()
            onNodeWithText("Streaming service").assertExists()
            onNodeWithTag("recurring_content")
                .performScrollToNode(hasTestTag("toggle_recurring_Internet"))
            onNodeWithTag("toggle_recurring_Internet").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("Paused").fetchSemanticsNodes().isNotEmpty()
            }
            capture("recurring-light")
            onNodeWithTag("recurring_content")
                .performScrollToNode(hasTestTag("toggle_recurring_Internet"))
            onNodeWithTag("toggle_recurring_Internet").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("Active").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("cancel_recurring_Internet").performClick()
            onNodeWithTag("confirm_cancel_recurring").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("Cancelled").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithContentDescription("Back").performClick()
            onNodeWithText("Money, untangled.").assertExists()
            onNodeWithContentDescription("Back").assertDoesNotExist()
            capture("money-toolbar")
            onNodeWithTag("household_switch").performClick()
            onNodeWithText("The city flat").performClick()
            onNodeWithTag("navigation_shopping").performClick()
            onNodeWithTag("list_First aid").assertDoesNotExist()
            onNodeWithTag("household_switch").performClick()
            onNodeWithText("The little nest").performClick()
            onNodeWithTag("navigation_shopping").performClick()
            onNodeWithTag("list_First aid").assertExists()
            onNodeWithTag("toolbar_actions").performClick()
            onNodeWithText("Settings").performClick()
            onNodeWithTag("settings_content").performScrollToNode(hasTestTag("analytics_consent"))
            onNodeWithTag("analytics_consent").assertIsOff()
            onNodeWithTag("analytics_consent").performClick().assertIsOn()
            onNodeWithTag("analytics_consent").performClick().assertIsOff()
            capture("settings-privacy")
            onNodeWithTag("settings_content").performScrollToNode(hasText("Dark"))
            onNodeWithText("Dark", useUnmergedTree = true).performClick()
            onNodeWithTag("navigation_home").performClick()
            onNodeWithText("Home, in harmony.").assertExists()
            capture("home-dark")
            onNodeWithTag("navigation_money").performClick()
            onNodeWithText("Money, untangled.").assertExists()
            capture("money-dark")
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
