package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.app.home.data.HomeRetryFixture
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.home.presentation.screen.HomeScreen
import kotlin.test.Test
import kotlinx.coroutines.cancel

@OptIn(ExperimentalTestApi::class)
class HomeLoadingUiTest {
    @Test
    fun slowShoppingAndNewsDoNotHideTasksDuringLoadOrRefresh() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            val fixture = HomeRetryFixture(failFirstTask = false, blockSecondaryReads = true)
            val viewModel = fixture.viewModel()
            try {
                setContent {
                    LoomTheme(mode = ThemeMode.LIGHT) {
                        HomeScreen(
                            viewModel = viewModel,
                            onShopping = {},
                            onTasks = {},
                            onAddExpense = {},
                            onAddTask = {},
                            onBalances = {},
                            onFeed = {},
                            onOpenStory = {},
                        )
                    }
                }
                waitUntil(timeoutMillis = 5000) { viewModel.state.value.taskItems != null }
                onNodeWithTag("home_content").performScrollToNode(hasText("Water the plants"))
                onNodeWithText("Water the plants").assertIsDisplayed()
                viewModel.onRetry()
                waitUntil(timeoutMillis = 5000) { !viewModel.state.value.tasksRefreshing }
                onNodeWithTag("home_content").performScrollToNode(hasText("Water the plants"))
                onNodeWithText("Water the plants").assertIsDisplayed()
                fixture.releaseSecondaryReads()
                waitUntil(timeoutMillis = 5000) { !viewModel.state.value.refreshing }
                onNodeWithTag("home_content").performScrollToNode(hasText("Avocados"))
                onNodeWithText("Avocados").assertIsDisplayed()
                onNodeWithTag("home_content").performScrollToNode(hasTestTag("home_feed_preview"))
                onNodeWithTag("home_feed_preview").assertExists()
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }
}
