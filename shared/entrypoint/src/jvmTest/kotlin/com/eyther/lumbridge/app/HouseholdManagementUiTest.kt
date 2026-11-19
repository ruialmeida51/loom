package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
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
class HouseholdManagementUiTest {
    @Test
    fun promotionKeepsBothOwnersAndRemovalShowsBalancesBeforeConfirmation() =
        runSkikoComposeUiTest(size = Size(440f, 800f)) {
            openHousehold()
            onNodeWithTag("member_actions_Alex").performScrollTo().performClick()
            onNodeWithText("Make owner").performClick()
            onNodeWithTag("confirm_household_action").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("modal_sheet").fetchSemanticsNodes().isEmpty() &&
                    onAllNodesWithText("Owner").fetchSemanticsNodes().size == 2
            }
            onNodeWithTag("member_actions_Alex").performClick()
            onNodeWithText("Remove").performClick()
            awaitLoadedDestination("departure_balances")
            onNodeWithText("Current balances by account").assertExists()
            onNodeWithText("Everyday").assertExists()
            onNodeWithTag("confirm_household_action").assertIsEnabled().performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("modal_sheet").fetchSemanticsNodes().isEmpty() &&
                    onAllNodesWithTag("member_actions_Alex").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithText("Owner").assertExists()
        }

    @Test
    fun householdDeletionRequiresItsExactNameAndReturnsToRemainingHouseholds() =
        runSkikoComposeUiTest(size = Size(440f, 800f)) {
            openHousehold()
            onNodeWithTag("delete_household").performScrollTo().performClick()
            onNodeWithTag("confirm_household_action").assertIsNotEnabled()
            onNodeWithTag("household_confirmation_name").performTextInput("The little nest")
            onNodeWithTag("confirm_household_action").assertIsEnabled().performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("The city flat").fetchSemanticsNodes().isNotEmpty() &&
                    onAllNodesWithTag("modal_sheet").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithText("The little nest").assertDoesNotExist()
            onNodeWithText("The city flat").performClick()
            awaitLoadedDestination("home_content")
        }

    private fun ComposeUiTest.openHousehold() {
        setContent { LoomDemoApp() }
        awaitLoadedDestination("home_content")
        onNodeWithTag("navigation_more").performClick()
        onNodeWithText("Household details").performClick()
        awaitLoadedDestination("household_details_content")
    }
}
