package com.eyther.lumbridge.core.presentation.components.pagination

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import com.eyther.lumbridge.core.domain.pagination.DEFAULT_PAGINATION_PREFETCH_THRESHOLD
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

/**
 * Calls [onLoadMore] when the reader scrolls within [threshold] rows of the end, while
 * [canLoadMore]. It emits the row count while inside that zone and dedupes on the count, not on a
 * bare "near the end" flag: each appended page changes the count and re-arms the trigger, so paging
 * continues even when a fling never leaves the zone (Plannot ADR-0007). Restarting on
 * [canLoadMore] covers a page that finished while the end was still in view, and a list shorter
 * than the screen keeps loading until it fills it or runs out.
 */
@Composable
fun HandleOnLoadMore(
    listState: LazyListState,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    threshold: Int = DEFAULT_PAGINATION_PREFETCH_THRESHOLD,
) {
    val loadMore by rememberUpdatedState(onLoadMore)
    LaunchedEffect(listState, canLoadMore, threshold) {
        if (!canLoadMore) return@LaunchedEffect
        // Paging often becomes possible in the same frame the first page arrives; until that
        // frame is measured, the layout still describes the skeleton and looks like the end.
        withFrameNanos {}
        snapshotFlow {
                val layout = listState.layoutInfo
                val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: return@snapshotFlow null
                layout.totalItemsCount.takeIf { lastVisible >= it - 1 - threshold }
            }
            .distinctUntilChanged()
            .filterNotNull()
            .collect { loadMore() }
    }
}
