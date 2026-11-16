package com.eyther.lumbridge.features.feed.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.EmptyNotice
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.ListRow
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.PageHeading
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.generated.resources.Res
import com.eyther.lumbridge.features.feed.generated.resources.feed_retry
import com.eyther.lumbridge.features.feed.generated.resources.ui_a_fresh_page
import com.eyther.lumbridge.features.feed.generated.resources.ui_follow_a_feed
import com.eyther.lumbridge.features.feed.generated.resources.ui_follow_an_rss_or_atom_feed_to_bring_your
import com.eyther.lumbridge.features.feed.generated.resources.ui_mark_as_read
import com.eyther.lumbridge.features.feed.generated.resources.ui_mark_unread
import com.eyther.lumbridge.features.feed.generated.resources.ui_read_story
import com.eyther.lumbridge.features.feed.generated.resources.ui_the_reading_corner
import com.eyther.lumbridge.features.feed.generated.resources.ui_unread_only
import com.eyther.lumbridge.features.feed.generated.resources.ui_your_favourite_corners_of_the_internet_in_one_quiet
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun FeedScreen(viewModel: FeedScreenViewModelContract, onOpen: (String) -> Unit) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    PullToRefreshContentContainer(isRefreshing = state.refreshing, onRefresh = viewModel::onRetry) {
        InnerFeedScreen(
            items = state.visibleArticles,
            unreadOnly = state.unreadOnly,
            loading = state.loading || state.refreshing,
            hasLoaded = state.hasLoaded,
            error = state.displayError,
            onOpen = onOpen,
            onShowFollow = viewModel::onFollowSheetVisibilityChanged,
            onUnreadOnly = viewModel::onUnreadOnlyChanged,
            onDismiss = viewModel::onDismissError,
            onRetry = viewModel::onRetry,
            onMarkRead = viewModel::onMarkRead,
        )
    }
}

@Composable
private fun InnerFeedScreen(
    items: List<ArticleDomain>,
    unreadOnly: Boolean,
    loading: Boolean,
    hasLoaded: Boolean,
    error: TextResource?,
    onOpen: (String) -> Unit,
    onShowFollow: (Boolean) -> Unit,
    onUnreadOnly: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onMarkRead: (ArticleDomain, Boolean) -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PageHeading(
                stringResource(Res.string.ui_the_reading_corner),
                stringResource(Res.string.ui_your_favourite_corners_of_the_internet_in_one_quiet),
            )
        }
        error?.let { message ->
            item(key = "error") {
                ErrorNotice(message = message, onDismiss = onDismiss)
                FilledTonalButton(onClick = onRetry) { Text(stringResource(Res.string.feed_retry)) }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    unreadOnly,
                    { onUnreadOnly(!unreadOnly) },
                    label = { Text(stringResource(Res.string.ui_unread_only)) },
                )
                FilledTonalButton(onClick = { onShowFollow(true) }) {
                    Text(stringResource(Res.string.ui_follow_a_feed))
                }
            }
        }
        if (loading) item { LoadingRows() }
        if (hasLoaded && !loading && error == null && items.isEmpty())
            item {
                EmptyNotice(
                    stringResource(Res.string.ui_a_fresh_page),
                    stringResource(Res.string.ui_follow_an_rss_or_atom_feed_to_bring_your),
                )
            }
        if (!loading)
            items(items, key = { it.id }) { article ->
                ListRow {
                    Text(
                        article.source.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(article.title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        article.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = { onMarkRead(article, !article.read) }) {
                            Text(
                                if (article.read) stringResource(Res.string.ui_mark_unread)
                                else stringResource(Res.string.ui_mark_as_read),
                            )
                        }
                        TextButton(
                            onClick = {
                                onMarkRead(article, true)
                                onOpen(article.url)
                            },
                        ) {
                            Text(stringResource(Res.string.ui_read_story))
                            Spacer(Modifier.width(6.dp))
                            LoomIcon(
                                Glyph.ARROW,
                                Modifier.size(16.dp),
                                MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
    }
}
