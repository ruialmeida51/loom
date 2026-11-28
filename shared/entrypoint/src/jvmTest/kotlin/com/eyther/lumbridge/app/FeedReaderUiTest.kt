package com.eyther.lumbridge.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import com.eyther.lumbridge.app.feed.data.FeedReaderFixture
import com.eyther.lumbridge.app.testutil.capture
import com.eyther.lumbridge.core.presentation.components.toolbar.Toolbar
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.feed.presentation.screen.FeedScreen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class FeedReaderUiTest {
    @Test
    fun scrollingAppendsPagesAndStopsAfterTheLastStory() =
        runSkikoComposeUiTest(size = Size(440f, 800f)) {
            val fixture = FeedReaderFixture()
            try {
                startReader(fixture, ThemeMode.LIGHT)
                assertEquals(20, fixture.viewModel.state.value.items.size)
                capture("feed-paged-light")
                // Scrolling near the end loads the next page; there is no button to press.
                val first = fixture.viewModel.state.value.items.last().id
                onNodeWithTag("feed_content").performScrollToNode(hasTestTag("article_card_$first"))
                waitUntil(timeoutMillis = 5_000) { fixture.viewModel.state.value.items.size == 40 }
                val second = fixture.viewModel.state.value.items.last().id
                onNodeWithTag("feed_content").performScrollToNode(hasTestTag("article_card_$second"))
                waitUntil(timeoutMillis = 5_000) { fixture.viewModel.state.value.items.size == 45 }
                assertNull(fixture.viewModel.state.value.nextCursor)
                onNodeWithTag("pagination_loading").assertDoesNotExist()
            } finally {
                fixture.close()
            }
        }

    @Test
    fun multipleSourcePillsCombineWithUnreadAndRemainUsableWithLargeText() =
        runSkikoComposeUiTest(size = Size(360f, 740f), density = Density(1f, fontScale = 1.3f)) {
            val fixture = FeedReaderFixture()
            try {
                startReader(fixture, ThemeMode.DARK)
                onNodeWithTag("feed_source_filters")
                    .performScrollToNode(hasTestTag("feed_source_weekend"))
                onNodeWithTag("feed_source_weekend").performClick()
                waitUntil(timeoutMillis = 5_000) { !fixture.viewModel.state.value.loading }
                assertTrue(
                    fixture.viewModel.state.value.items.all { it.subscriptionId == "weekend" },
                )
                onNodeWithTag("feed_source_weekend").assertIsSelected()
                onNodeWithTag("feed_source_filters")
                    .performScrollToNode(hasTestTag("feed_source_table"))
                onNodeWithTag("feed_source_table").performClick()
                waitUntil(timeoutMillis = 5_000) { !fixture.viewModel.state.value.loading }
                assertEquals(
                    setOf("weekend", "table"),
                    fixture.viewModel.state.value.selectedSourceIds,
                )
                // A plain feed: stories carry no read state or per-story menu.
                onNodeWithTag("feed_article_actions_story-1").assertDoesNotExist()
                capture("feed-filtered-dark-narrow")
            } finally {
                fixture.close()
            }
        }

    private fun SkikoComposeUiTest.startReader(fixture: FeedReaderFixture, theme: ThemeMode) {
        setContent {
            LoomTheme(theme) {
                Surface {
                    Column(Modifier.fillMaxSize()) {
                        Toolbar(text = "The reading corner", showBackButton = true, onBack = {})
                        Box(Modifier.weight(1f)) {
                            FeedScreen(
                                viewModel = fixture.viewModel,
                                onOpen = {},
                                onManageFeeds = {},
                            )
                        }
                    }
                }
            }
        }
        waitUntil(timeoutMillis = 5_000) {
            fixture.viewModel.state.value.hasLoaded &&
                fixture.viewModel.state.value.sourceFilters.size == 2 &&
                onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
        }
        onNodeWithTag("feed_content").assertIsDisplayed()
    }

}
