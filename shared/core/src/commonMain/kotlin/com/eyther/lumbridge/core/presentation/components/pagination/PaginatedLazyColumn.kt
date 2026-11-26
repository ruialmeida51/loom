package com.eyther.lumbridge.core.presentation.components.pagination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.domain.pagination.DEFAULT_PAGINATION_PREFETCH_THRESHOLD
import com.eyther.lumbridge.core.generated.resources.Res
import com.eyther.lumbridge.core.generated.resources.ui_try_again
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoadingRow
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import org.jetbrains.compose.resources.stringResource

/**
 * The one list for paged content. [content] lays out the destination's own rows (headers, empty and
 * error states, records); this appends the paging footer: a skeleton row while the next page loads,
 * or the failed page's message with a retry. The next page is requested through [onLoadMore] as the
 * reader nears the end (see [HandleOnLoadMore]); the ViewModel guards duplicate requests.
 */
@Composable
fun PaginatedLazyColumn(
    listState: LazyListState,
    pagination: PaginationUi,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(LoomSpacing.small),
    fetchThreshold: Int = DEFAULT_PAGINATION_PREFETCH_THRESHOLD,
    loadingMoreContent: @Composable LazyItemScope.() -> Unit = {
        LoadingRow(modifier = Modifier.testTag("pagination_loading"))
    },
    content: LazyListScope.() -> Unit,
) {
    HandleOnLoadMore(
        listState = listState,
        canLoadMore = pagination.canLoadMore,
        onLoadMore = onLoadMore,
        threshold = fetchThreshold,
    )
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
    ) {
        content()
        if (!pagination.enabled) return@LazyColumn
        val loadMoreError = pagination.loadMoreError
        when {
            loadMoreError != null ->
                item(key = PAGINATION_ERROR_KEY, contentType = PAGINATION_ERROR_KEY) {
                    Column(
                        modifier = Modifier.testTag("pagination_error"),
                        verticalArrangement = Arrangement.spacedBy(LoomSpacing.small),
                    ) {
                        ErrorNotice(message = loadMoreError)
                        LoomSecondaryButton(
                            text = stringResource(Res.string.ui_try_again),
                            onClick = onRetryLoadMore,
                            modifier = Modifier.testTag("pagination_retry"),
                        )
                    }
                }
            pagination.loadingMore ->
                item(key = PAGINATION_LOADING_KEY, contentType = PAGINATION_LOADING_KEY) {
                    loadingMoreContent()
                }
        }
    }
}

private const val PAGINATION_LOADING_KEY = "pagination_loading"
private const val PAGINATION_ERROR_KEY = "pagination_error"
