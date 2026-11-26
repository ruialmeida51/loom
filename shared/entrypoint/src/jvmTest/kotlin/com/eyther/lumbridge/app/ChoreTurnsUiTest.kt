package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
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
class ChoreTurnsUiTest {
    @Test
    fun aRotatingChoreShowsWhoseTurnItIsAndPassesItOn() =
        runSkikoComposeUiTest(size = Size(440f, 900f)) {
            startTasks()
            onNodeWithText("Alex’s turn · then Sam").assertExists()
            onNodeWithText("Take the recycling out").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Sam’s turn · then You").fetchSemanticsNodes().isNotEmpty()
            }
        }

    @Test
    fun aNewChoreCanTakeTurnsAndMineShowsOnlyYours() =
        runSkikoComposeUiTest(size = Size(440f, 900f)) {
            startTasks()
            onNodeWithTag("add_task").performClick()
            onNodeWithTag("task_title").performTextInput("Clean the bathroom")
            onNodeWithTag("task_date").performClick()
            onNodeWithTag("confirm_task_date").performClick()
            onNodeWithText("Weekly").performScrollTo().performClick()
            onNodeWithTag("task_assignee_You").performScrollTo().performClick()
            onNodeWithTag("task_take_turns").performScrollTo().performClick()
            onNodeWithTag("save_task").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Your turn · then Alex").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("tasks_filter_mine").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Take the recycling out").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithText("Clean the bathroom").assertExists()
        }

    private fun ComposeUiTest.startTasks() {
        setContent { LoomDemoApp() }
        waitUntil(timeoutMillis = 10_000) {
            onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("navigation_tasks").performClick()
        awaitLoadedDestination(tag = "tasks_content")
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithText("Alex’s turn", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
