package com.eyther.lumbridge.demo

import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.text.AnnotatedString
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.eyther.lumbridge.app.demo.data.loadDemoReceiptBytes
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.regex.Pattern
import kotlin.math.abs
import kotlinx.coroutines.runBlocking
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
    fun pausedRecurringBillEditRetainsDraftAboveKeyboardAndAcrossResume() {
        compose.onNodeWithTag("navigation_money").performClick()
        awaitDestination(tag = "expenses_content")
        compose.onNodeWithTag("balances_actions").performClick()
        compose.onNodeWithText("Recurring expenses").performClick()
        awaitDestination(tag = "recurring_content")
        compose.onNodeWithTag("recurring_actions_Internet").performScrollTo().performClick()
        compose.onNodeWithText("Pause").performClick()
        compose.onNodeWithText("Paused").assertExists()
        compose.onNodeWithTag("recurring_actions_Internet").performClick()
        compose.onNodeWithText("Edit").performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("expense_amount").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("expense_amount").performTextClearance()
        compose.onNodeWithTag("expense_amount").performTextInput("42.00")
        compose.waitUntil(timeoutMillis = 5_000) { keyboardVisible() }
        val submitBottom =
            compose.onNodeWithTag("save_recurring").fetchSemanticsNode().boundsInWindow.bottom
        compose.runOnIdle {
            val decor = compose.activity.window.decorView
            val keyboard =
                ViewCompat.getRootWindowInsets(decor)
                    ?.getInsets(WindowInsetsCompat.Type.ime())
                    ?.bottom ?: 0
            assertTrue(
                "Recurring Save must stay above the keyboard",
                submitBottom <= decor.height - keyboard + 2f,
            )
        }
        capture(name = "recurring-form-keyboard")
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        awaitActivityWindowFocus()
        compose.onNodeWithTag("expense_amount").assertTextContains("42.00")
        hideKeyboard()
        compose.onNodeWithTag("save_recurring").performClick()
        awaitDestination(tag = "recurring_content")
        compose.onNodeWithText("Paused").assertExists()
        compose.onNodeWithText("42.00", substring = true).assertExists()
        capture(name = "recurring-saved-paused")
        pressSystemBack()
        awaitDestination(tag = "expenses_content")
        pressSystemBack()
        awaitDestination(tag = "home_content")
    }

    @Test
    fun paymentFormKeepsItsDraftAcrossDateBackAndResumeThenOpensConsistentDetails() {
        compose.onNodeWithTag("navigation_money").performClick()
        awaitDestination(tag = "expenses_content")
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithTag("balances_view_balances").performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("record_payment").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onAllNodesWithTag("record_payment")[0].performScrollTo().performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("repayment_amount").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("modal_sheet").assertDoesNotExist()
        compose.onNodeWithTag("repayment_amount").performClick()
        compose.onNodeWithTag("repayment_amount").performTextClearance()
        compose.onNodeWithTag("repayment_amount").performTextInput("1.00")
        compose.waitUntil(timeoutMillis = 5_000) { keyboardVisible() }
        val submitBottom =
            compose
                .onNodeWithTag("record_repayment_submit")
                .fetchSemanticsNode()
                .boundsInWindow
                .bottom
        compose.runOnUiThread {
            val decor = compose.activity.window.decorView
            val keyboard =
                requireNotNull(ViewCompat.getRootWindowInsets(decor))
                    .getInsets(WindowInsetsCompat.Type.ime())
                    .bottom
            assertTrue(
                "Payment Save must stay above the keyboard",
                submitBottom <= decor.height - keyboard + 2f,
            )
        }
        capture(name = "payment-form-keyboard")
        hideKeyboard()
        compose.onNodeWithTag("repayment_date").performScrollTo().performClick()
        compose.onNodeWithTag("form_sheet").assertIsDisplayed()
        pressSystemBack()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("modal_sheet").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithTag("repayment_amount").assertTextContains("1.00")
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        awaitActivityWindowFocus()
        hideKeyboard()
        compose.onNodeWithTag("repayment_amount").assertTextContains("1.00")
        compose.onNodeWithTag("record_repayment_submit").performClick()
        awaitDestination(tag = "expenses_content")
        compose.onNodeWithTag("balances_view_expenses").performClick()
        val paymentCard =
            SemanticsMatcher("Recorded payment card") { node ->
                node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("payment_card_") ==
                    true
            }
        compose.onNodeWithTag("expenses_content").performScrollToNode(paymentCard)
        compose.onNode(paymentCard).performTouchInput {
            click(Offset(x = width * .1f, y = height / 2f))
        }
        awaitDestination(tag = "payment_details_content")
        compose.onNodeWithText("€1.00").assertIsDisplayed()
        compose.onNodeWithTag("payment_details_actions").performClick()
        compose.onNodeWithText("Edit").performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("repayment_amount").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("repayment_amount").performClick()
        compose.onNodeWithTag("repayment_amount").performTextClearance()
        compose.onNodeWithTag("repayment_amount").performTextInput("2.00")
        hideKeyboard()
        compose.onNodeWithTag("record_repayment_submit").performClick()
        awaitDestination(tag = "payment_details_content")
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText("€2.00").fetchSemanticsNodes().isNotEmpty()
        }
        capture(name = "payment-details")
        pressSystemBack()
        awaitDestination(tag = "expenses_content")
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

    @Test
    fun titleOnlyTaskSavesFromHomeAndSystemBackFromTasksReturnsHome() {
        val taskTitle = "Check hallway smoke alarm"
        awaitDestination(tag = "home_content")
        // Add task now sits below Home's balance card; bring it fully on screen first.
        compose.onNodeWithTag("home_content").performScrollToNode(hasTestTag("home_add_task"))
        compose.onNodeWithTag("home_add_task").performClick()
        compose.onNodeWithTag("task_title").performTextInput(taskTitle)
        compose.onNodeWithTag("task_notification").assertDoesNotExist()
        // The sheet footer remains usable while its own window holds the IME.
        compose.onNodeWithTag("save_task").assertIsDisplayed().performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("modal_sheet").fetchSemanticsNodes().isEmpty()
        }
        awaitDestination(tag = "home_content")
        compose.onNodeWithTag("navigation_tasks").performClick()
        awaitDestination(tag = "tasks_content")
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithTag("tasks_content").performScrollToNode(hasContentDescription(taskTitle))
        compose.onNodeWithContentDescription(taskTitle).performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText("$taskTitle completed").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("$taskTitle completed").assertIsDisplayed()
        compose.onNodeWithTag("tasks_completed").performScrollTo().performClick()
        compose.onNodeWithTag("tasks_content").performScrollToNode(hasContentDescription(taskTitle))
        compose.onNodeWithContentDescription(taskTitle).assertIsDisplayed()
        capture(name = "tasks-completed")
        pressSystemBack()
        awaitDestination(tag = "home_content")
    }

    @Test
    fun taskFabKeepsSubmitReachableWithTheRealKeyboardAndReturnsToTheList() {
        val title = "Book an annual boiler inspection"
        compose.onNodeWithTag("navigation_tasks").performClick()
        awaitDestination(tag = "tasks_content")
        compose.onNodeWithTag("add_task").assertIsDisplayed().performClick()
        compose.onNodeWithTag("task_title").performClick().performTextInput(title)
        compose.waitUntil(timeoutMillis = 5_000) { keyboardVisible() }
        val submit = compose.onNodeWithTag("save_task").assertIsDisplayed()
        val submitBottom = submit.fetchSemanticsNode().boundsInWindow.bottom
        compose.runOnUiThread {
            val decor = compose.activity.window.decorView
            val keyboard =
                requireNotNull(ViewCompat.getRootWindowInsets(decor))
                    .getInsets(WindowInsetsCompat.Type.ime())
                    .bottom
            assertTrue(
                "Save stays above the keyboard",
                submitBottom <= decor.height - keyboard + 2f,
            )
        }
        capture(name = "task-fab-keyboard")
        submit.performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("modal_sheet").fetchSemanticsNodes().isEmpty()
        }
        hideKeyboard()
        awaitDestination(tag = "tasks_content")
        compose.onNodeWithTag("tasks_content").performScrollToNode(hasContentDescription(title))
        compose.onNodeWithContentDescription(title).assertIsDisplayed()
        compose.onNodeWithTag("add_task").assertIsDisplayed()
        pressSystemBack()
        awaitDestination(tag = "home_content")
    }

    @Test
    fun systemBackCancelsDangerousActionsWithoutLeavingTheCurrentScreen() {
        compose.onNodeWithTag("navigation_tasks").performClick()
        awaitDestination(tag = "tasks_content")
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
        }
        compose
            .onNodeWithContentDescription("Task options: Water the plants")
            .performScrollTo()
            .performClick()
        compose.onNodeWithText("Delete").performClick()
        compose.onNodeWithTag("confirm_delete_task").assertIsDisplayed()
        capture(name = "task-delete-confirmation")
        pressSystemBack()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("confirm_delete_task").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithContentDescription("Water the plants").assertIsDisplayed()
        compose.onNodeWithTag("open_my_account").performClick()
        compose.onNodeWithTag("account_content").performScrollToNode(hasTestTag("sign_out"))
        compose.onNodeWithTag("sign_out").performClick()
        compose.onNodeWithTag("confirm_sign_out").assertIsDisplayed()
        pressSystemBack()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("confirm_sign_out").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithTag("account_content").assertIsDisplayed()
    }

    @Test
    fun groupingUsesTheRealSwipeGestureAndOutdentKeepsTheChecklistAligned() {
        compose.onNodeWithTag("navigation_shopping").performClick()
        awaitDestination(tag = "shopping_content")
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithTag("edit_Cherry tomatoes").performTouchInput { swipeRight() }
        compose.waitUntil(timeoutMillis = 5_000) {
            compose
                .onNodeWithTag("check_Cherry tomatoes")
                .fetchSemanticsNode()
                .boundsInWindow
                .left >
                compose.onNodeWithTag("check_Avocados").fetchSemanticsNode().boundsInWindow.left +
                    20f
        }
        capture(name = "shopping-nested")
        compose.onNodeWithTag("edit_Cherry tomatoes").performTouchInput { swipeLeft() }
        compose.waitUntil(timeoutMillis = 5_000) {
            abs(
                compose
                    .onNodeWithTag("check_Cherry tomatoes")
                    .fetchSemanticsNode()
                    .boundsInWindow
                    .left -
                    compose
                        .onNodeWithTag("check_Avocados")
                        .fetchSemanticsNode()
                        .boundsInWindow
                        .left,
            ) < 2f
        }
        compose.onNodeWithTag("shopping_inline_input").assertIsDisplayed()
    }

    @Test
    fun systemPhotoPickerKeepsTheExpenseDraftAndTheSavedPhotoOpensInItsViewer() {
        val resolver = compose.activity.contentResolver
        val values =
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "Loom receipt fixture.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/Loom-receipt-test",
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        val uri =
            requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        try {
            resolver.openOutputStream(uri).use { output ->
                requireNotNull(output).write(runBlocking { loadDemoReceiptBytes() })
            }
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                null,
                null,
            )
            val title = "Receipt through gallery"
            compose.onNodeWithTag("home_add_expense").performClick()
            compose.onNodeWithTag("expense_title").performTextInput(title)
            compose.onNodeWithTag("expense_amount").performTextInput("8.40")
            hideKeyboard()
            compose.onNodeWithTag("expense_receipt").performScrollTo().performClick()
            compose.onNodeWithText("Choose photo").performClick()
            val thumbnail =
                device.wait(
                    Until.findObject(By.res(Pattern.compile(".*:id/icon_thumbnail"))),
                    15_000,
                )
            requireNotNull(thumbnail) { "The Android photo picker did not show the test image" }
                .click()
            device.findObject(By.text(Pattern.compile("Add|Add [(]1[)]")))?.click()
            compose.waitUntil(timeoutMillis = 10_000) {
                compose.onAllNodesWithText("Ready to save").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("expense_title").assertTextContains(title)
            compose.onNodeWithTag("expense_amount").assertTextContains("8.40")
            hideKeyboard()
            capture(name = "expense-receipt-draft")
            compose.onNodeWithTag("save_expense").performClick()
            awaitDestination(tag = "home_content")
            compose.onNodeWithTag("navigation_money").performClick()
            awaitDestination(tag = "expenses_content")
            compose.waitUntil(timeoutMillis = 5_000) {
                compose.onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
            }
            compose.onNodeWithText(title).performScrollTo().performClick()
            awaitDestination(tag = "expense_details_content")
            compose.waitUntil(timeoutMillis = 5_000) {
                compose.onAllNodesWithText("View photo").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("expense_receipt").performScrollTo().performClick()
            compose.waitUntil(timeoutMillis = 10_000) {
                compose.onAllNodesWithTag("receipt_zoom_reset").fetchSemanticsNodes().isNotEmpty()
            }
            compose.waitUntil(timeoutMillis = 5_000) {
                compose
                    .onAllNodesWithContentDescription("Receipt photo")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            compose.onNodeWithContentDescription("Receipt photo").assertIsDisplayed()
            capture(name = "receipt-gallery-viewer")
            pressSystemBack()
            awaitDestination(tag = "expense_details_content")
        } finally {
            resolver.delete(uri, null, null)
        }
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
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onNodeWithTag(tag).isDisplayed() &&
                compose
                    .onAllNodes(hasTestTag("loading_rows") and hasAnyAncestor(hasTestTag(tag)))
                    .fetchSemanticsNodes()
                    .isEmpty()
        }
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
