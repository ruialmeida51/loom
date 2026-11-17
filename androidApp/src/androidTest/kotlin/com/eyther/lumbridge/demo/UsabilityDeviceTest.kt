package com.eyther.lumbridge.demo

import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.text.AnnotatedString
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** These checks exercise Android's actual window/IME and Back dispatch, not desktop substitutes. */
class UsabilityDeviceTest {
    @get:Rule(order = 0) val compose = createAndroidComposeRule<UsabilityTestActivity>()
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @get:Rule(order = 1)
    val failureEvidence =
        object : TestWatcher() {
            override fun failed(error: Throwable, description: Description) {
                runCatching { capture(name = "failure-${description.methodName}") }
                    .onFailure { Log.e("LoomTestFailure", "Failure capture unavailable", it) }
                runCatching {
                    val hierarchy = ByteArrayOutputStream()
                    device.dumpWindowHierarchy(hierarchy)
                    hierarchy.toString(Charsets.UTF_8.name()).chunked(3000).forEach {
                        Log.e("LoomTestFailure", it)
                    }
                }
            }
        }

    @Before
    fun awaitHome() {
        compose.waitUntil(timeoutMillis = 15_000) {
            compose.onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun continuousEntryStaysFocusedAndAboveTheRealKeyboardOnALongList() {
        compose.onNodeWithTag("navigation_shopping").performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("shopping_inline_input").fetchSemanticsNodes().isNotEmpty()
        }
        repeat(30) { index ->
            val input = compose.onNodeWithTag("shopping_inline_input")
            input.performScrollTo().performClick().performTextInput("Checklist item $index")
            if (index % 2 == 0) input.performImeAction() else device.pressEnter()
            compose.waitForIdle()
        }
        val input = compose.onNodeWithTag("shopping_inline_input")
        input
            .assertIsFocused()
            .assertIsDisplayed()
            .assert(
                SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("")),
            )
        compose.runOnUiThread {
            val decor = compose.activity.window.decorView
            val insets = requireNotNull(ViewCompat.getRootWindowInsets(decor))
            assertTrue(
                "Enter must retain the keyboard",
                insets.isVisible(WindowInsetsCompat.Type.ime()),
            )
        }
        compose.onNodeWithText("Checklist item 29").assertIsDisplayed()
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
        pressSystemBack() // The keyboard consumes the first Back.
        compose.waitUntil(timeoutMillis = 5_000) { !keyboardVisible() }
        device.waitForIdle()
        awaitDestination(tag = "shopping_content")
        pressSystemBack() // Then the peer tab returns Home.
        awaitDestination(tag = "home_content")
    }

    @Test
    fun cancellingSplitAndBackgroundingRetainTheExpenseDraftThenBackReturnsToBalances() {
        compose.onNodeWithTag("navigation_money").performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("add_expense").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("add_expense").performClick()
        compose.onNodeWithTag("expense_title").performTextInput("Holiday groceries")
        compose.onNodeWithTag("expense_amount").performTextInput("30.00")
        hideKeyboard()
        compose.onNodeWithText("Split", substring = false).performScrollTo().performClick()
        compose.onNodeWithTag("split_method").performClick()
        compose.onNodeWithText("By shares").performClick()
        compose.onNodeWithContentDescription("Increase share for You").performClick()
        compose.onNodeWithContentDescription("Share for You").assertTextContains("2")
        compose.onNodeWithContentDescription("Decrease share for You").performClick()
        compose.onNodeWithContentDescription("Share for You").assertTextContains("1")
        capture(name = "expense-split-sheet")
        pressSystemBack()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("modal_sheet").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithTag("expense_title").assertTextContains("Holiday groceries")
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        awaitActivityWindowFocus()
        compose.onNodeWithTag("expense_amount").assertTextContains("30.00")
        hideKeyboard()
        pressSystemBack()
        awaitDestination(tag = "expenses_content")
        compose.onNodeWithTag("expense_title").assertDoesNotExist()
        pressSystemBack()
        awaitDestination(tag = "home_content")
    }

    @Test
    fun checkboxGeometryAndDraftSurviveSavingAndPullToRefresh() {
        compose.onNodeWithTag("navigation_shopping").performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("shopping_inline_input").fetchSemanticsNodes().isNotEmpty()
        }
        val input = compose.onNodeWithTag("shopping_inline_input")
        input.performScrollTo().performClick().performTextInput("Aligned grocery")
        val inputLeft = input.fetchSemanticsNode().boundsInWindow.left
        val checkboxCenter =
            compose
                .onNodeWithTag("shopping_entry_checkbox")
                .fetchSemanticsNode()
                .boundsInWindow
                .center
                .x
        input.performImeAction()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("check_Aligned grocery").fetchSemanticsNodes().isNotEmpty()
        }
        val savedCenter =
            compose
                .onNodeWithTag("check_Aligned grocery")
                .fetchSemanticsNode()
                .boundsInWindow
                .center
                .x
        val savedLeft =
            compose.onNodeWithTag("edit_Aligned grocery").fetchSemanticsNode().boundsInWindow.left
        assertTrue(
            "Saving must not move the checkbox column",
            abs(savedCenter - checkboxCenter) <= 2f,
        )
        assertTrue("Saving must not move the text column", abs(savedLeft - inputLeft) <= 2f)
        input.performTextInput("Next grocery draft")
        hideKeyboard()
        compose.onNodeWithTag("shopping_content").performScrollToIndex(0)
        compose.onNodeWithTag("shopping_content").performTouchInput {
            swipeDown(startY = height * .15f, endY = height * .8f, durationMillis = 600)
        }
        compose.waitForIdle()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("refresh_skeleton").fetchSemanticsNodes().isEmpty() &&
                compose.onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
        }
        assertTrue(
            "The short refresh transition finishes and restores the checklist",
            compose.onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty(),
        )
        capture(name = "shopping-refresh-retains-content")
        compose
            .onNodeWithTag("shopping_inline_input")
            .performScrollTo()
            .assertTextContains("Next grocery draft")
    }

    @Test
    fun systemBackFromFeedManagementReturnsThroughNewsAndMore() {
        compose.onNodeWithTag("navigation_more").performClick()
        compose.onNodeWithText("Feed").performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("manage_feeds").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("manage_feeds").performClick()
        awaitDestination(tag = "feed_subscriptions")
        pressSystemBack()
        awaitDestination(tag = "manage_feeds")
        compose.onNodeWithText("The reading corner").assertIsDisplayed()
        pressSystemBack()
        awaitDestination(tag = "more_content")
        pressSystemBack()
        awaitDestination(tag = "home_content")
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val file = File(compose.activity.cacheDir, "$name.png")
        check(device.takeScreenshot(file)) { "The device could not capture the current screen" }
        // Gradle uninstalls the test app after a run. Shared test media survives until collection.
        val resolver = compose.activity.contentResolver
        val values =
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$name.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/Loom-usability",
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        val uri =
            requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        resolver.openOutputStream(uri).use { output ->
            file.inputStream().use { input -> input.copyTo(requireNotNull(output)) }
        }
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        file.delete()
    }

    private fun pressSystemBack() {
        Log.d("LoomUsabilityBack", "Pressing Back; foreground=${device.currentPackageName}")
        device.pressBack()
    }

    private fun awaitDestination(tag: String) {
        compose.waitUntil(timeoutMillis = 5_000) { compose.onNodeWithTag(tag).isDisplayed() }
        compose.waitForIdle()
    }

    private fun awaitActivityWindowFocus() {
        // RESUMED can arrive before Android returns input focus from the scenario's helper window.
        compose.waitUntil(timeoutMillis = 5_000) {
            var focused = false
            compose.runOnUiThread { focused = compose.activity.hasWindowFocus() }
            focused
        }
    }

    private fun keyboardVisible(): Boolean {
        var visible = false
        compose.runOnUiThread {
            visible =
                ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                    ?.isVisible(WindowInsetsCompat.Type.ime()) == true
        }
        return visible
    }

    private fun hideKeyboard() {
        // Android's IME can restore after resume, independently of Compose's idle clock.
        device.waitForIdle()
        compose.runOnUiThread {
            val window = compose.activity.window
            WindowCompat.getInsetsController(window, window.decorView)
                .hide(WindowInsetsCompat.Type.ime())
        }
        compose.waitUntil(timeoutMillis = 5_000) { !keyboardVisible() }
        device.waitForIdle()
        compose.waitForIdle()
    }
}
