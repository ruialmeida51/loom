package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
class HouseholdRefinementUiTest {
    @Test
    fun completedGroceriesHaveIndividualAndSelectAllDeletionWithoutChangingCompletion() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            startApp()
            onNodeWithTag("navigation_shopping").performClick()
            awaitLoadedDestination("shopping_content")
            onNodeWithTag("check_Avocados").performClick()
            onNodeWithTag("shopping_completed").performScrollTo().performClick()
            onNodeWithTag("select_completed").performScrollTo().performClick()
            onNodeWithTag("check_Avocados").performScrollTo().performClick()
            onNodeWithText("Delete (1)").assertExists()
            onNodeWithTag("select_all_completed").performScrollTo().performClick()
            onNodeWithText("Delete (2)").assertExists()
            capture("shopping-completed-selection")
            onNodeWithTag("delete_completed").performScrollTo().performClick()
            onNodeWithTag("confirm_delete_completed").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("Completed groceries deleted: 2")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            onNodeWithTag("check_Avocados").assertDoesNotExist()
            onNodeWithTag("check_Coffee beans").assertDoesNotExist()
            onNodeWithTag("edit_Cherry tomatoes").assertExists()
        }

    @Test
    fun shoppingPreviewOpensTheExistingRootAndProfileGroupsHaveSeparateLayouts() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            startApp()
            onNodeWithTag("home_shopping_preview").performScrollTo().assertIsDisplayed()
            onNodeWithText("Coffee beans").assertDoesNotExist()
            capture("home-shopping-preview")
            onNode(hasText("View all") and hasAnyAncestor(hasTestTag("home_shopping_preview")))
                .performScrollTo()
                .performClick()
            awaitLoadedDestination("shopping_content")
            onNodeWithContentDescription("Back").assertDoesNotExist()
            onNodeWithTag("open_my_account").performClick()
            awaitLoadedDestination("account_content")
            val profileBottom =
                onNodeWithTag("edit_nickname").fetchSemanticsNode().boundsInRoot.bottom
            val preferencesTop = onNodeWithText("Preferences").fetchSemanticsNode().boundsInRoot.top
            kotlin.test.assertTrue(preferencesTop > profileBottom)
            capture("account-group-spacing")
        }

    @Test
    fun completingARecurringReminderAcknowledgesTheOccurrenceOnANarrowScreen() =
        runSkikoComposeUiTest(size = Size(360f, 740f), density = Density(1f, fontScale = 1.2f)) {
            startApp()
            onNodeWithText("All reminders").performScrollTo().performClick()
            awaitLoadedDestination("reminders_content")
            onAllNodes(isToggleable())[0].performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("Water the plants completed").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithText("Water the plants", substring = false).assertExists()
            onNodeWithTag("success_snackbar").assertIsDisplayed()
            capture("reminder-completion-narrow")
        }

    private fun SkikoComposeUiTest.startApp() {
        ThemePreferences.set("LIGHT")
        setContent { LoomDemoApp() }
        awaitLoadedDestination("home_content")
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
