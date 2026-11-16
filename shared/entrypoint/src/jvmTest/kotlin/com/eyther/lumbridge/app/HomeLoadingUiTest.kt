package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
    fun slowShoppingAndNewsDoNotHideRemindersDuringLoadOrRefresh() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            val fixture = HomeRetryFixture(failFirstReminder = false, blockSecondaryReads = true)
            val viewModel = fixture.viewModel()
            try {
                setContent {
                    LoomTheme(mode = ThemeMode.LIGHT) {
                        HomeScreen(
                            viewModel = viewModel,
                            onShopping = {},
                            onReminders = {},
                            onAddExpense = {},
                            onAddReminder = {},
                            onFeed = {},
                            onOpenStory = {},
                        )
                    }
                }
                waitUntil(timeoutMillis = 5000) { viewModel.state.value.reminderItems != null }
                onNodeWithText("Water the plants").assertIsDisplayed()
                viewModel.onRetry()
                waitForIdle()
                onNodeWithText("Water the plants").assertIsDisplayed()
                fixture.releaseSecondaryReads()
                waitUntil(timeoutMillis = 5000) { !viewModel.state.value.refreshing }
                onNodeWithText("Avocados").assertIsDisplayed()
                onNodeWithTag("home_feed_preview").assertExists()
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }
}
