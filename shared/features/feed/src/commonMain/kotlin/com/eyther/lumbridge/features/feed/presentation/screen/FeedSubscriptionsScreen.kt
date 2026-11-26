package com.eyther.lumbridge.features.feed.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.IllustratedEmptyState
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.RetryNotice
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.snackbar.SuccessSnackbar
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionDomain
import com.eyther.lumbridge.features.feed.generated.resources.Res
import com.eyther.lumbridge.features.feed.generated.resources.feed_add
import com.eyther.lumbridge.features.feed.generated.resources.feed_no_subscriptions
import com.eyther.lumbridge.features.feed.generated.resources.feed_retry
import com.eyther.lumbridge.features.feed.presentation.components.FeedSubscriptionCard
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedSubscriptionsScreenViewModelContract
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun FeedSubscriptionsScreen(viewModel: FeedSubscriptionsScreenViewModelContract) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    PullToRefreshContentContainer(
        isRefreshing = state.refreshing,
        onRefresh = viewModel::onRefresh,
        snackbarHost = { SuccessSnackbar(message = state.feedback) },
    ) {
        InnerFeedSubscriptionsScreen(
            items = state.items,
            loading = state.loading,
            hasLoaded = state.hasLoaded,
            busy = state.busy,
            busyId = state.busyId,
            error = state.displayError,
            onAdd = viewModel::onAddFeed,
            onEdit = viewModel::onEditFeed,
            onRefresh = viewModel::onRefreshFeed,
            onRemove = viewModel::onRemoveFeed,
            onRetry = viewModel::onRefresh,
            onDismiss = viewModel::onDismissError,
        )
    }
}

@Composable
private fun InnerFeedSubscriptionsScreen(
    items: ImmutableList<FeedSubscriptionDomain>,
    loading: Boolean,
    hasLoaded: Boolean,
    busy: Boolean,
    busyId: String?,
    error: TextResource?,
    onAdd: () -> Unit,
    onEdit: (FeedSubscriptionDomain) -> Unit,
    onRefresh: (FeedSubscriptionDomain) -> Unit,
    onRemove: (FeedSubscriptionDomain) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("feed_subscriptions"),
        contentPadding = PaddingValues(LoomSpacing.content),
        verticalArrangement = Arrangement.spacedBy(LoomSpacing.content),
    ) {
        item(key = "add") {
            GroupedSurface {
                NavigationRow(
                    title = stringResource(Res.string.feed_add),
                    glyph = Glyph.PLUS,
                    onClick = onAdd,
                    enabled = !busy,
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
        if (loading) item(key = "loading") { LoadingRows() }
        else {
            if (hasLoaded && items.isEmpty() && error == null)
                item(key = "empty") {
                    IllustratedEmptyState(
                        title = stringResource(Res.string.feed_no_subscriptions),
                        glyph = Glyph.FEED,
                    )
                }
            items(items, key = { it.id }) { subscription ->
                FeedSubscriptionCard(
                    subscription = subscription,
                    enabled = !busy,
                    loading = busyId == subscription.id,
                    onEdit = { onEdit(subscription) },
                    onRefresh = { onRefresh(subscription) },
                    onRemove = { onRemove(subscription) },
                )
            }
        }
    }
}
