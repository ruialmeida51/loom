package com.eyther.lumbridge.demo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** These checks exercise Android's actual window/IME and Back dispatch, not desktop substitutes. */
class UsabilityDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<UsabilityTestActivity>()
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun awaitHome() {
        compose.waitUntil(timeoutMillis = 15_000) {
            compose.onAllNodesWithText("Today").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun continuousEntryStaysFocusedAndAboveTheRealKeyboardOnALongList() {
        compose.onNodeWithTag("navigation_shopping").performClick()
        repeat(30) { index ->
            val input = compose.onNodeWithTag("shopping_inline_input")
            input.performScrollTo().performClick().performTextInput("Checklist item $index")
            if (index % 2 == 0) input.performImeAction() else device.pressEnter()
            compose.waitForIdle()
        }
        val input = compose.onNodeWithTag("shopping_inline_input")
        input.assertIsFocused().assertIsDisplayed().assertTextEquals("")
        compose.runOnUiThread {
            val decor = compose.activity.window.decorView
            val insets = requireNotNull(ViewCompat.getRootWindowInsets(decor))
            assertTrue(
                "Enter must retain the keyboard",
                insets.isVisible(WindowInsetsCompat.Type.ime()),
            )
        }
        capture(name = "shopping-keyboard")
        val inputBottom = input.fetchSemanticsNode().boundsInWindow.bottom
        compose.runOnUiThread {
            val decor = compose.activity.window.decorView
            val keyboard =
                requireNotNull(ViewCompat.getRootWindowInsets(decor))
                    .getInsets(WindowInsetsCompat.Type.ime())
                    .bottom
            assertTrue(
                "The next entry must remain above the keyboard",
                inputBottom <= decor.height - keyboard + 2f,
            )
        }
        device.pressBack() // The keyboard consumes the first Back.
        compose.onNodeWithTag("shopping_content").assertIsDisplayed()
        device.pressBack() // Then the peer tab returns Home.
        compose.onNodeWithTag("home_content").assertIsDisplayed()
    }

    @Test
    fun cancellingSplitAndBackgroundingRetainTheExpenseDraftThenBackReturnsToBalances() {
        compose.onNodeWithTag("navigation_money").performClick()
        compose.onNodeWithTag("add_expense").performClick()
        compose.onNodeWithTag("expense_title").performTextInput("Holiday groceries")
        compose.onNodeWithTag("expense_amount").performTextInput("30.00")
        hideKeyboard()
        compose.onNodeWithText("Split", substring = false).performScrollTo().performClick()
        compose.onNodeWithText("By shares").performClick()
        capture(name = "expense-split-sheet")
        device.pressBack()
        compose.onNodeWithTag("expense_title").assertTextContains("Holiday groceries")
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        compose.onNodeWithTag("expense_amount").assertTextContains("30.00")
        hideKeyboard()
        device.pressBack()
        compose.onNodeWithTag("expenses_content").assertIsDisplayed()
        device.pressBack()
        compose.onNodeWithTag("home_content").assertIsDisplayed()
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val directory =
            File(compose.activity.getExternalFilesDir(null), "usability").apply { mkdirs() }
        device.takeScreenshot(File(directory, "$name.png"))
    }

    private fun hideKeyboard() {
        var visible = false
        compose.runOnUiThread {
            visible =
                ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                    ?.isVisible(WindowInsetsCompat.Type.ime()) == true
        }
        if (visible) device.pressBack()
        compose.waitForIdle()
    }
}
