package com.eyther.lumbridge.app

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.eyther.lumbridge.core.presentation.components.pagination.PaginatedLazyColumn
import com.eyther.lumbridge.core.presentation.components.pagination.PaginationUi
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_loom
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PaginatedLazyColumnUiTest {
    @Test
    fun scrollingToTheEndPagesUntilTheLastPageAndAFailedPageWaitsForRetry() =
        runSkikoComposeUiTest(size = Size(400f, 600f)) {
            var rows by mutableStateOf((1..20).toList())
            var pagination by mutableStateOf(PaginationUi(hasMore = true))
            var requests = 0
            var failNext = false
            fun loadMore() {
                requests++
                if (failNext) {
                    failNext = false
                    pagination = pagination.copy(loadMoreError = TextResource.Resource(Res.string.ui_loom))
                    return
                }
                rows = rows + (rows.size + 1..rows.size + 20)
                pagination = pagination.copy(hasMore = rows.size < 60)
            }
            setContent {
                LoomTheme(mode = ThemeMode.LIGHT) {
                    PaginatedLazyColumn(
                        listState = rememberLazyListState(),
                        pagination = pagination,
                        onLoadMore = ::loadMore,
                        onRetryLoadMore = {
                            pagination = pagination.copy(loadMoreError = null)
                            loadMore()
                        },
                        modifier = Modifier.testTag("paged"),
                    ) {
                        items(rows, key = { it }) { row ->
                            Text("Row $row", modifier = Modifier.testTag("row_$row"))
                        }
                    }
                }
            }
            // Each appended page re-arms the trigger while the end stays in view.
            onNodeWithTag("paged").performScrollToNode(hasTestTag("row_40"))
            onNodeWithTag("paged").performScrollToNode(hasTestTag("row_60"))
            waitForIdle()
            assertEquals(2, requests)
            assertEquals(60, rows.size)

            rows = (1..20).toList()
            pagination = PaginationUi(hasMore = true)
            requests = 0
            failNext = true
            onNodeWithTag("paged").performScrollToNode(hasTestTag("row_20"))
            waitForIdle()
            onNodeWithTag("paged").performScrollToNode(hasTestTag("pagination_retry"))
            waitForIdle()
            // The error row brought the end back into view, but paging waits for the retry.
            assertEquals(1, requests)
            onNodeWithTag("pagination_retry").performClick()
            waitForIdle()
            assertEquals(2, requests)
            assertEquals(40, rows.size)
        }
}
