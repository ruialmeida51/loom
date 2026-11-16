package com.eyther.lumbridge.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.NavigationEventInput
import androidx.navigationevent.OnBackCompletedFallback
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.app.demo.data.DemoHouseholdStore
import com.eyther.lumbridge.app.navigation.push.PendingPush
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class NavigationFlowsUiTest {
    @Test
    fun systemBackPopsChildrenThenReturnsPeerTabsHomeBeforeExiting() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            var exits = 0
            val dispatcher =
                NavigationEventDispatcher(
                    object : OnBackCompletedFallback {
                        override fun onBackCompletedFallback() {
                            exits++
                        }
                    },
                )
            val input = BackInput()
            dispatcher.addInput(input)
            val owner =
                object : NavigationEventDispatcherOwner {
                    override val navigationEventDispatcher = dispatcher
                }
            setContent {
                CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides owner) {
                    LoomDemoApp()
                }
            }
            waitUntil(timeoutMillis = 10000) {
                onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty() &&
                    onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithContentDescription("Back").assertDoesNotExist()
            onNodeWithTag("navigation_shopping").performClick()
            awaitLoadedDestination(tag = "shopping_content")
            onNodeWithTag("shopping_inline_input").performTextInput("Milk draft")
            runOnIdle { input.back() }
            onNodeWithTag("home_content").assertIsDisplayed()
            assertEquals(0, exits)
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithTag("add_expense").performClick()
            onNodeWithTag("expense_title").performTextInput("Draft expense")
            runOnIdle { input.back() }
            onNodeWithTag("expenses_content").assertIsDisplayed()
            onNodeWithContentDescription("Back").assertDoesNotExist()
            runOnIdle { input.back() }
            onNodeWithTag("home_content").assertIsDisplayed()
            assertEquals(0, exits)
            onNodeWithTag("navigation_more").performClick()
            onNodeWithText("Feed").performClick()
            runOnIdle { input.back() }
            onNodeWithTag("more_content").assertIsDisplayed()
            runOnIdle { input.back() }
            onNodeWithTag("home_content").assertIsDisplayed()
            runOnIdle { input.back() }
            assertEquals(1, exits)
            runOnIdle { dispatcher.dispose() }
        }

    @Test
    fun aNotificationCanOpenBalancesWhileGlobalAccountIsVisible() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            PendingPush.clear()
            try {
                setContent { LoomDemoApp() }
                waitUntil(timeoutMillis = 10000) {
                    onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty() &&
                        onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
                }
                onNodeWithTag("open_my_account").performClick()
                onNodeWithText("Profile").assertIsDisplayed()
                runOnIdle {
                    PendingPush.open(
                        householdId = DemoHouseholdStore.households.first().id,
                        route = "money",
                        accountId = DemoHouseholdStore.households.first().id,
                    )
                }
                waitUntil(timeoutMillis = 5000) {
                    onAllNodesWithTag("expenses_content").fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithTag("expenses_content").assertIsDisplayed()
                onNodeWithText("Profile").assertDoesNotExist()
                onNodeWithContentDescription("Back").assertDoesNotExist()
                assertNull(PendingPush.destination.value)
            } finally {
                PendingPush.clear()
            }
        }

    private class BackInput : NavigationEventInput() {
        fun back() {
            dispatchOnBackCompleted()
        }
    }
}
