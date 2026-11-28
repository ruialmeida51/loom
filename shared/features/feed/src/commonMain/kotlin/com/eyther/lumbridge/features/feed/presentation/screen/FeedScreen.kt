package com.eyther.lumbridge.features.feed.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.EmptyNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.RetryNotice
import com.eyther.lumbridge.core.presentation.components.SectionTitle
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.pagination.PaginatedLazyColumn
import com.eyther.lumbridge.core.presentation.components.pagination.PaginationUi
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.feed.domain.model.StarterFeedDomain
import com.eyther.lumbridge.features.feed.generated.resources.Res
import com.eyther.lumbridge.features.feed.generated.resources.feed_empty_description
import com.eyther.lumbridge.features.feed.generated.resources.feed_manage
import com.eyther.lumbridge.features.feed.generated.resources.feed_no_articles
import com.eyther.lumbridge.features.feed.generated.resources.feed_retry
import com.eyther.lumbridge.features.feed.generated.resources.feed_starter_sources
import com.eyther.lumbridge.features.feed.presentation.components.ArticleCard
import com.eyther.lumbridge.features.feed.presentation.components.FeedSourceFilters
import com.eyther.lumbridge.features.feed.presentation.model.ArticlePreviewUi
import com.eyther.lumbridge.features.feed.presentation.model.FeedSourceFilterUi
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import org.jetbrains.compose.resources.stringResource

@Composable
fun FeedScreen(
    viewModel: FeedScreenViewModelContract,
    onOpen: (String) -> Unit,
    onManageFeeds: () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    val listState = rememberLazyListState()
    PullToRefreshContentContainer(isRefreshing = state.refreshing, onRefresh = viewModel::onRetry) {
        InnerFeedScreen(
            listState = listState,
            items = state.articleCards,
            suggestions = state.suggestions,
            onStarterFeed = viewModel::onStarterFeedSelected,
            sources = state.sourceFilters,
            selectedSourceIds = state.selectedSourceIds,
            onSource = viewModel::onSourceSelected,
            onAllSources = viewModel::onAllSourcesSelected,
            pagination = state.pagination,
            onLoadMore = viewModel::onLoadMore,
            onRetryLoadMore = viewModel::onRetryLoadMore,
            loading = state.loading,
            hasLoaded = state.hasLoaded,
            error = state.displayError,
            onOpen = onOpen,
            onDismiss = viewModel::onDismissError,
            onRetry = viewModel::onRetry,
            onManageFeeds = onManageFeeds,
        )
    }
}

@Composable
private fun InnerFeedScreen(
    listState: LazyListState,
    items: ImmutableList<ArticlePreviewUi>,
    suggestions: ImmutableList<StarterFeedDomain>,
    onStarterFeed: (StarterFeedDomain) -> Unit,
    sources: ImmutableList<FeedSourceFilterUi>,
    selectedSourceIds: ImmutableSet<String>,
    onSource: (String) -> Unit,
    onAllSources: () -> Unit,
    pagination: PaginationUi,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    loading: Boolean,
    hasLoaded: Boolean,
    error: TextResource?,
    onOpen: (String) -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onManageFeeds: () -> Unit,
) {
    PaginatedLazyColumn(
        listState = listState,
        pagination = pagination,
        onLoadMore = onLoadMore,
        onRetryLoadMore = onRetryLoadMore,
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("feed_content"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "manage_feeds") {
            GroupedSurface {
                NavigationRow(
                    title = stringResource(Res.string.feed_manage),
                    glyph = Glyph.FEED,
                    onClick = onManageFeeds,
                    modifier = Modifier.testTag("manage_feeds"),
                )
            }
        }

        error?.let { message ->
            item(key = "error") {
                RetryNotice(
                    message = message,
                    onRetry = onRetry,
                    onDismiss = onDismiss,
                    retryText = stringResource(Res.string.feed_retry),
                )
            }
        }
        item(key = "source_filters") {
            FeedSourceFilters(
                sources = sources,
                selectedIds = selectedSourceIds,
                onSource = onSource,
                onAll = onAllSources,
            )
        }
        if (loading) item { LoadingRows() }
        if (hasLoaded && !loading && error == null && items.isEmpty())
            item {
                EmptyNotice(
                    stringResource(Res.string.feed_no_articles),
                    stringResource(Res.string.feed_empty_description),
                )
            }
        if (
            hasLoaded &&
                !loading &&
                items.isEmpty() &&
                selectedSourceIds.isEmpty() &&
                suggestions.isNotEmpty()
        )
            item {
                SectionTitle(title = stringResource(Res.string.feed_starter_sources))
                GroupedSurface {
                    suggestions.forEach { feed ->
                        NavigationRow(title = feed.publisher, onClick = { onStarterFeed(feed) })
                    }
                }
            }
        if (!loading)
            items(items, key = { it.id }) { article ->
                ArticleCard(
                    article = article,
                    onOpen = { onOpen(article.url) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
    }
}
