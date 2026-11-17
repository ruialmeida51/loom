package com.eyther.lumbridge.features.feed.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.EmptyNotice
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.SectionTitle
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.feed.domain.model.StarterFeedDomain
import com.eyther.lumbridge.features.feed.generated.resources.Res
import com.eyther.lumbridge.features.feed.generated.resources.feed_caught_up
import com.eyther.lumbridge.features.feed.generated.resources.feed_caught_up_description
import com.eyther.lumbridge.features.feed.generated.resources.feed_empty_description
import com.eyther.lumbridge.features.feed.generated.resources.feed_load_more
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
    PullToRefreshContentContainer(isRefreshing = state.refreshing, onRefresh = viewModel::onRetry) {
        InnerFeedScreen(
            items = state.articleCards,
            suggestions = state.suggestions,
            onStarterFeed = viewModel::onStarterFeedSelected,
            sources = state.sourceFilters,
            selectedSourceIds = state.selectedSourceIds,
            onSource = viewModel::onSourceSelected,
            onAllSources = viewModel::onAllSourcesSelected,
            hasMore = state.nextCursor != null,
            loadingMore = state.loadingMore,
            pageError = state.pageError,
            onLoadMore = viewModel::onLoadMore,
            unreadOnly = state.unreadOnly,
            loading = state.loading,
            hasLoaded = state.hasLoaded,
            error = state.displayError,
            onOpen = onOpen,
            onUnreadOnly = viewModel::onUnreadOnlyChanged,
            onDismiss = viewModel::onDismissError,
            onRetry = viewModel::onRetry,
            onMarkRead = viewModel::onMarkRead,
            onManageFeeds = onManageFeeds,
        )
    }
}

@Composable
private fun InnerFeedScreen(
    items: ImmutableList<ArticlePreviewUi>,
    suggestions: ImmutableList<StarterFeedDomain>,
    onStarterFeed: (StarterFeedDomain) -> Unit,
    sources: ImmutableList<FeedSourceFilterUi>,
    selectedSourceIds: ImmutableSet<String>,
    onSource: (String) -> Unit,
    onAllSources: () -> Unit,
    hasMore: Boolean,
    loadingMore: Boolean,
    pageError: TextResource?,
    onLoadMore: () -> Unit,
    unreadOnly: Boolean,
    loading: Boolean,
    hasLoaded: Boolean,
    error: TextResource?,
    onOpen: (String) -> Unit,
    onUnreadOnly: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onMarkRead: (String, Boolean) -> Unit,
    onManageFeeds: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("feed_content"),
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
                ErrorNotice(message = message, onDismiss = onDismiss)
                FilledTonalButton(onClick = onRetry) { Text(stringResource(Res.string.feed_retry)) }
            }
        }
        item(key = "source_filters") {
            FeedSourceFilters(
                sources = sources,
                selectedIds = selectedSourceIds,
                unreadOnly = unreadOnly,
                onSource = onSource,
                onAll = onAllSources,
                onUnreadOnly = onUnreadOnly,
            )
        }
        if (loading) item { LoadingRows() }
        if (hasLoaded && !loading && error == null && items.isEmpty())
            item {
                EmptyNotice(
                    stringResource(
                        if (unreadOnly) Res.string.feed_caught_up else Res.string.feed_no_articles,
                    ),
                    stringResource(
                        if (unreadOnly) Res.string.feed_caught_up_description
                        else Res.string.feed_empty_description,
                    ),
                )
            }
        if (
            hasLoaded &&
                !loading &&
                items.isEmpty() &&
                !unreadOnly &&
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
                    onOpen = {
                        onMarkRead(article.id, true)
                        onOpen(article.url)
                    },
                    onReadChanged = { onMarkRead(article.id, !article.read) },
                )
            }
        if (!loading && hasMore)
            item(key = "pagination") {
                pageError?.let { message -> ErrorNotice(message = message, onDismiss = onDismiss) }
                LoomButton(
                    text =
                        stringResource(
                            if (pageError == null) Res.string.feed_load_more
                            else Res.string.feed_retry,
                        ),
                    onClick = onLoadMore,
                    loading = loadingMore,
                    modifier = Modifier.fillMaxWidth().testTag("feed_load_more"),
                )
            }
    }
}
