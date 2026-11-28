package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
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
import com.eyther.lumbridge.app.demo.data.DemoHouseholdStore
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
import com.eyther.lumbridge.app.testutil.capture
import com.eyther.lumbridge.core.domain.AppLanguage
import com.eyther.lumbridge.features.settings.data.local.LanguagePreferences
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

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
            onNodeWithContentDescription("Task options: Water the plants")
                .performScrollTo()
                .performClick()
            onNodeWithText("Delete").performClick()
            onNodeWithText("Delete task?").assertIsDisplayed()
            onNodeWithText("Future repetitions will also stop.", substring = true).assertExists()
            capture("task-delete-confirmation-dark-narrow")
            onNodeWithText("Cancel").performClick()
            onNodeWithContentDescription("Water the plants").assertExists()
            onNodeWithContentDescription("Task options: Water the plants")
                .performScrollTo()
                .performClick()
            onNodeWithText("Delete").performClick()
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
                onAllNodesWithTag("active_invitation_code").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithText("ABC234").assertExists()
            capture("household-invitation-light")
            onNodeWithTag("invitation_actions").performScrollTo().performClick()
            onNodeWithText("Revoke").performClick()
            onNodeWithTag("confirm_household_action").assertIsDisplayed()
            onNodeWithText("Cancel").performClick()
            onNodeWithText("ABC234").assertExists()
            onNodeWithTag("invitation_actions").performClick()
            onNodeWithText("Revoke").performClick()
            onNodeWithTag("confirm_household_action").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("modal_sheet").fetchSemanticsNodes().isEmpty() &&
                    onAllNodesWithTag("active_invitation_code").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("household_details_content").assertIsDisplayed()
        }

    @Test
    fun tappingAHousemateOpensTheirProfileWithPaymentDetailsBalanceAndTasks() =
        runSkikoComposeUiTest(size = Size(440f, 900f)) {
            startApp()
            onNodeWithTag("navigation_more").performClick()
            onNodeWithText("Household details").performClick()
            awaitLoadedDestination("household_details_content")
            onNodeWithTag("member_${DemoHouseholdStore.ALEX}").performScrollTo().performClick()
            awaitLoadedDestination("member_profile")
            onNodeWithTag("member_profile_picture").assertIsDisplayed()
            onNodeWithTag("member_balance").assertExists()
            onNodeWithTag("member_profile")
                .performScrollToNode(hasTestTag("member_payment"))
            onNodeWithTag("pay_mbway").assertExists()
            onNodeWithTag("pay_paypal").assertExists()
            onNodeWithText("Tasks").assertExists()
            capture("member-profile-light")
        }

    @Test
    fun aHousematesProfileReadsInTheDarkTheme() =
        runSkikoComposeUiTest(size = Size(440f, 900f)) {
            startApp(theme = "DARK")
            onNodeWithTag("navigation_more").performClick()
            onNodeWithText("Household details").performClick()
            awaitLoadedDestination("household_details_content")
            onNodeWithTag("member_${DemoHouseholdStore.ALEX}").performScrollTo().performClick()
            awaitLoadedDestination("member_profile")
            capture("member-profile-dark")
        }

    @Test
    fun splitUsesStackedFullWidthActionsAndAMenuBesideTheMethod() =
        runSkikoComposeUiTest(size = Size(360f, 740f), density = Density(1f, 1.3f)) {
            startApp()
            onNodeWithTag("home_add_expense").performClick()
            onNodeWithTag("expense_title").performTextInput("Dinner together")
            onNodeWithTag("expense_amount").performTextInput("30.00")
            onNodeWithTag("split_unevenly").performScrollTo().performClick()
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
            assertTrue(save.bottom < cancel.top, "Save should be above Cancel with space between")
            assertTrue(
                abs(save.width - cancel.width) < 2f,
                "Both stacked actions should use the same full width",
            )
            assertTrue(save.width > 300f, "Stacked actions should fill the narrow sheet")
            // The four methods fit side by side on a narrow phone.
            onNodeWithTag("split_method_percentages").assertIsDisplayed()
            capture("split-method-tabs-narrow")
            onNodeWithTag("split_method_shares").performClick()
            capture("split-actions-modern-narrow")
            onNodeWithTag("confirm_expense_split").performClick()
            onNodeWithTag("expense_title").assertIsDisplayed()
        }

    @Test
    fun languageSwitchesInPlaceAndBackToTheDevice() =
        runSkikoComposeUiTest(size = Size(440f, 800f)) {
            try {
                startApp()
                onNodeWithTag("open_my_account").performClick()
                awaitLoadedDestination("account_content")
                onNodeWithText("Preferences").assertExists()
                onNodeWithTag("language_preference").performScrollTo().performClick()
                onNodeWithText("Português", useUnmergedTree = true).performClick()
                // The same screen, now in Portuguese: the switch keeps navigation where it was.
                waitUntil(timeoutMillis = 5_000) {
                    onAllNodesWithText("Preferências").fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithTag("account_content").assertIsDisplayed()
                onNodeWithText("Preferences").assertDoesNotExist()
                capture("language-portuguese")
                onNodeWithTag("language_preference").performScrollTo().performClick()
                onNodeWithText("Sistema", useUnmergedTree = true).performClick()
                waitUntil(timeoutMillis = 5_000) {
                    onAllNodesWithText("Preferences").fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithTag("account_content").assertIsDisplayed()
            } finally {
                runBlocking { LanguagePreferences.set(AppLanguage.SYSTEM) }
            }
        }

    private fun SkikoComposeUiTest.startApp(theme: String = "LIGHT") {
        ThemePreferences.set(theme)
        setContent { LoomDemoApp() }
        awaitLoadedDestination("home_content")
    }

}
