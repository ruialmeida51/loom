package com.eyther.lumbridge.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.NavigationEventInput
import androidx.navigationevent.OnBackCompletedFallback
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.eyther.lumbridge.app.demo.LoomDemoApp
import kotlin.test.Test
import kotlin.test.assertEquals

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
                onAllNodesWithText("Today").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithContentDescription("Back").assertDoesNotExist()
            onNodeWithTag("navigation_shopping").performClick()
            onNodeWithTag("shopping_inline_input").performTextInput("Milk draft")
            runOnIdle { input.back() }
            onNodeWithText("Today").assertIsDisplayed()
            assertEquals(0, exits)
            onNodeWithTag("navigation_money").performClick()
            onNodeWithTag("add_expense").performClick()
            onNodeWithTag("expense_title").performTextInput("Draft expense")
            runOnIdle { input.back() }
            onNodeWithTag("expenses_content").assertIsDisplayed()
            onNodeWithContentDescription("Back").assertDoesNotExist()
            runOnIdle { input.back() }
            onNodeWithText("Today").assertIsDisplayed()
            assertEquals(0, exits)
            onNodeWithText("Feed").performScrollTo().performClick()
            runOnIdle { input.back() }
            onNodeWithText("Today").assertIsDisplayed()
            runOnIdle { input.back() }
            assertEquals(1, exits)
            runOnIdle { dispatcher.dispose() }
        }

    private class BackInput : NavigationEventInput() {
        fun back() {
            dispatchOnBackCompleted()
        }
    }
}
