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
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

@OptIn(ExperimentalTestApi::class)
class SafetyAndDetailsUiTest {
    @Test
    fun signOutOnlyHappensAfterConfirmationAndTheThemeMenuAnchorsAtItsCaret() =
        runSkikoComposeUiTest(size = Size(440f, 800f)) {
            startApp()
            onNodeWithTag("open_my_account").performClick()
            awaitLoadedDestination("account_content")
            onNodeWithTag("appearance_preference").performScrollTo().performClick()
            val anchor =
                onNodeWithTag("appearance_menu_anchor", useUnmergedTree = true)
                    .fetchSemanticsNode()
                    .boundsInWindow
            val menu = onNodeWithTag("appearance_menu").fetchSemanticsNode().boundsInWindow
            assertTrue(menu.right >= anchor.left, "The menu should reach the caret it belongs to")
            assertTrue(
                menu.left > 100f,
                "The right-side selector should not open at the screen's left gutter",
            )
            capture("theme-menu-anchored-light")
            onNodeWithText("Dark", useUnmergedTree = true).performClick()
            onNodeWithTag("account_content").performScrollToNode(hasTestTag("sign_out"))
            onNodeWithTag("sign_out").performClick()
            onNodeWithTag("confirm_sign_out").assertIsDisplayed()
            capture("sign-out-confirmation-dark")
            onNodeWithText("Cancel").performClick()
            onNodeWithTag("account_content").assertIsDisplayed()
            onNodeWithTag("confirm_sign_out").assertDoesNotExist()
            onNodeWithTag("sign_out").performClick()
            onNodeWithTag("confirm_sign_out").performClick()
            onNodeWithTag("account_content").assertDoesNotExist()
            onNodeWithTag("home_content").assertDoesNotExist()
        }

    @Test
    fun deletingARecurringTaskIsExplicitAndCancelKeepsTheTask() =
        runSkikoComposeUiTest(size = Size(360f, 740f), density = Density(1f, 1.3f)) {
            startApp(theme = "DARK")
            onNodeWithTag("navigation_tasks").performClick()
            awaitLoadedDestination("tasks_content")
            onNodeWithContentDescription("Delete task: Water the plants")
                .performScrollTo()
                .performClick()
            onNodeWithText("Delete task?").assertIsDisplayed()
            onNodeWithText("Future repetitions will also stop.", substring = true).assertExists()
            capture("task-delete-confirmation-dark-narrow")
            onNodeWithText("Cancel").performClick()
            onNodeWithContentDescription("Water the plants").assertExists()
            onNodeWithContentDescription("Delete task: Water the plants")
                .performScrollTo()
                .performClick()
            onNodeWithTag("confirm_delete_task").performClick()
            onNodeWithTag("confirm_delete_task").assertDoesNotExist()
            onNodeWithContentDescription("Water the plants").assertDoesNotExist()
            onNodeWithText("Task deleted").assertExists()
        }

    @Test
    fun householdDetailsAndInvitationRevocationKeepTheCodeUntilConfirmed() =
        runSkikoComposeUiTest(size = Size(440f, 800f)) {
            startApp()
            onNodeWithTag("navigation_more").performClick()
            onNodeWithText("Household details").performClick()
            awaitLoadedDestination("household_details_content")
            onNodeWithText("Currency").assertIsDisplayed()
            onNodeWithText("Time zone").assertIsDisplayed()
            onNodeWithText("Owner").assertExists()
            capture("household-details-light")
            onNodeWithTag("create_invitation").performScrollTo().performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("modal_sheet").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithText("ABC234").assertExists()
            capture("household-invitation-light")
            onNodeWithText("Revoke code").performScrollTo().performClick()
            onNodeWithTag("confirm_revoke_invitation").assertIsDisplayed()
            onNodeWithText("Cancel").performClick()
            onNodeWithText("ABC234").assertExists()
            onNodeWithText("Revoke code").performClick()
            onNodeWithTag("confirm_revoke_invitation").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("modal_sheet").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("household_details_content").assertIsDisplayed()
        }

    @Test
    fun splitUsesEqualWidthActionsAndAMenuBesideTheMethod() =
        runSkikoComposeUiTest(size = Size(360f, 740f), density = Density(1f, 1.3f)) {
            startApp()
            onNodeWithTag("home_add_expense").performClick()
            onNodeWithTag("expense_title").performTextInput("Dinner together")
            onNodeWithTag("expense_amount").performTextInput("30.00")
            onNodeWithText("Split").performClick()
            val save =
                onNodeWithTag("confirm_expense_split")
                    .assertIsDisplayed()
                    .fetchSemanticsNode()
                    .boundsInWindow
            val cancel =
                onNode(hasText("Cancel") and hasAnyAncestor(hasTestTag("form_sheet")))
                    .assertIsDisplayed()
                    .fetchSemanticsNode()
                    .boundsInWindow
            assertTrue(abs(save.top - cancel.top) < 2f, "The two actions should share a row")
            assertTrue(
                abs(save.width - cancel.width) < 2f,
                "The two actions should fill equal footer space",
            )
            onNodeWithTag("split_method").performClick()
            val menu = onNodeWithTag("split_method_menu").fetchSemanticsNode().boundsInWindow
            assertTrue(
                menu.left > 32f,
                "The method menu should follow the caret rather than the sheet gutter",
            )
            capture("split-method-menu-anchored-narrow")
            onNodeWithText("By shares").performClick()
            capture("split-actions-modern-narrow")
            onNodeWithTag("confirm_expense_split").performClick()
            onNodeWithTag("expense_title").assertIsDisplayed()
        }

    private fun SkikoComposeUiTest.startApp(theme: String = "LIGHT") {
        ThemePreferences.set(theme)
        setContent { LoomDemoApp() }
        awaitLoadedDestination("home_content")
    }

    private fun SkikoComposeUiTest.capture(name: String) {
        waitForIdle()
        val folder =
            File(System.getProperty("loom.screenshots.dir", "build/screenshots")).apply { mkdirs() }
        File(folder, "$name.png")
            .writeBytes(
                requireNotNull(
                        Image.makeFromBitmap(captureToImage().asSkiaBitmap())
                            .encodeToData(EncodedImageFormat.PNG),
                    )
                    .bytes,
            )
    }
}
