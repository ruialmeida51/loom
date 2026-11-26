package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.hasContentDescription
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
import com.eyther.lumbridge.app.testutil.capture
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TasksUiTest {
    @Test
    fun titleOnlyQuickAddStaysOnHomeAndTasksCanBeCompletedAndReopened() =
        runSkikoComposeUiTest(size = Size(440f, 800f)) {
            startApp()
            onNodeWithTag("home_content").performScrollToNode(hasTestTag("home_add_task"))
            onNodeWithTag("home_add_task").performClick()
            onNodeWithTag("task_title").performTextInput("Fix hallway lamp")
            onNodeWithTag("task_notification").assertDoesNotExist()
            onNodeWithTag("task_time").assertDoesNotExist()
            capture("task-quick-add-light")
            onNodeWithTag("save_task").performClick()
            awaitSheetDismissed()
            onNodeWithTag("home_content").assertIsDisplayed()
            onNodeWithText("Task added").assertExists()
            onNodeWithContentDescription("Back").assertDoesNotExist()
            onNodeWithText("All tasks").performScrollTo().performClick()
            awaitLoadedDestination("tasks_content")
            onNodeWithTag("tasks_content").performScrollToNode(hasText("Fix hallway lamp"))
            onNodeWithContentDescription("Fix hallway lamp").performClick()
            onNodeWithText("Fix hallway lamp completed").assertExists()
            onNodeWithContentDescription("Fix hallway lamp").assertDoesNotExist()
            // The completion snackbar can sit over the Completed header; let it clear first.
            waitUntil(timeoutMillis = 6_000) {
                onAllNodesWithTag("success_snackbar").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("tasks_completed").performScrollTo().performClick()
            // Completed rows are lazy: scroll the list until the reopened task is composed.
            onNodeWithTag("tasks_content")
                .performScrollToNode(hasContentDescription("Fix hallway lamp"))
            onNodeWithContentDescription("Fix hallway lamp").performClick()
            onNodeWithText("Fix hallway lamp reopened").assertExists()
            onNodeWithTag("tasks_completed").assertDoesNotExist()
            onNodeWithTag("tasks_content").performScrollToNode(hasText("Fix hallway lamp"))
            onNodeWithContentDescription("Fix hallway lamp").assertIsDisplayed()
            capture("tasks-light")
        }

    @Test
    fun dateRepeatAndNotificationAreOptionalAndCanBeRemovedBeforeSaving() =
        runSkikoComposeUiTest(size = Size(440f, 800f)) {
            startApp()
            onNodeWithTag("navigation_tasks").performClick()
            awaitLoadedDestination("tasks_content")
            onNodeWithTag("add_task").performClick()
            onNodeWithTag("task_title").performTextInput("Water balcony plants")
            onNodeWithTag("task_date").performClick()
            val saveBounds = onNodeWithTag("confirm_task_date").fetchSemanticsNode().boundsInRoot
            val cancelBounds = onNodeWithTag("dialog_dismiss").fetchSemanticsNode().boundsInRoot
            assertTrue(abs(saveBounds.width - cancelBounds.width) < 1f)
            capture("task-calendar-equal-actions")
            onNodeWithTag("confirm_task_date").performClick()
            onNodeWithTag("task_notification").assertIsOff()
            onNodeWithText("Daily").performScrollTo().performClick()
            onNodeWithTag("task_notification").performScrollTo().performClick()
            onNodeWithTag("task_time").performScrollTo().performClick()
            onNodeWithTag("confirm_task_time").performClick()
            capture("task-schedule-light")
            onNodeWithContentDescription("Remove due date").performScrollTo().performClick()
            onNodeWithTag("task_notification").assertDoesNotExist()
            onNodeWithTag("task_time").assertDoesNotExist()
            onNodeWithText("Daily").assertDoesNotExist()
            onNodeWithTag("save_task").performClick()
            awaitSheetDismissed()
            onNodeWithTag("tasks_content").performScrollToNode(hasText("Water balcony plants"))
            onNodeWithContentDescription("Water balcony plants").assertIsDisplayed()
        }

    @Test
    fun taskActionsAndFiveTabsFitANarrowViewportWithLargeText() =
        runSkikoComposeUiTest(size = Size(360f, 640f), density = Density(1f, 1.3f)) {
            startApp(theme = "DARK")
            onNodeWithTag("navigation_tasks").performClick()
            awaitLoadedDestination("tasks_content")
            onNodeWithTag("navigation_money").assertIsDisplayed()
            onNodeWithTag("navigation_more").assertIsDisplayed()
            onNodeWithContentDescription("Back").assertDoesNotExist()
            capture("tasks-dark-narrow")
            onNodeWithTag("add_task").performClick()
            onNodeWithTag("task_title").performTextInput("Book a boiler service")
            onNodeWithTag("save_task").assertIsDisplayed()
            capture("task-sheet-dark-narrow")
            onNodeWithTag("save_task").performClick()
            awaitSheetDismissed()
            onNodeWithTag("tasks_content").performScrollToNode(hasText("Book a boiler service"))
            onNodeWithContentDescription("Book a boiler service").assertIsDisplayed()
        }

    private fun SkikoComposeUiTest.startApp(theme: String = "LIGHT") {
        ThemePreferences.set(theme)
        setContent { LoomDemoApp() }
        awaitLoadedDestination("home_content")
    }

    private fun SkikoComposeUiTest.awaitSheetDismissed() {
        waitUntil(timeoutMillis = 5_000) {
            onAllNodesWithTag("modal_sheet").fetchSemanticsNodes().isEmpty()
        }
    }

}
