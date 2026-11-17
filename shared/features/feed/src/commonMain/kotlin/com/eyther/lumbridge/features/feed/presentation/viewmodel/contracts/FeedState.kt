package com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedCursorDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedQueryDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionDomain
import com.eyther.lumbridge.features.feed.domain.model.StarterFeedDomain
import com.eyther.lumbridge.features.feed.presentation.mapper.toPreviewUi
import com.eyther.lumbridge.features.feed.presentation.model.FeedSourceFilterUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Load, action and sheet errors have separate lifetimes; refreshed articles never erase a form
 * failure.
 */
data class FeedState(
    val timeZone: String = "UTC",
    val items: List<ArticleDomain> = emptyList(),
    val sources: ImmutableList<FeedSubscriptionDomain> = persistentListOf(),
    val selectedSourceIds: PersistentSet<String> = persistentSetOf(),
    val nextCursor: FeedCursorDomain? = null,
    val loadingMore: Boolean = false,
    val pageError: TextResource? = null,
    val sourcesError: TextResource? = null,
    val suggestions: ImmutableList<StarterFeedDomain> = persistentListOf(),
    val url: String = "",
    val showFollow: Boolean = false,
    val unreadOnly: Boolean = false,
    val busy: Boolean = false,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val hasLoaded: Boolean = false,
    val error: TextResource? = null,
    val loadError: TextResource? = null,
    val formError: TextResource? = null,
) {

    val displayError: TextResource? = error ?: loadError ?: sourcesError

    val query: FeedQueryDomain =
        FeedQueryDomain(subscriptionIds = selectedSourceIds, unreadOnly = unreadOnly)
    val sourceFilters =
        sources.map { FeedSourceFilterUi(id = it.id, label = it.publisher) }.toImmutableList()
    val visibleArticles: List<ArticleDomain> = items.filter {
        (!unreadOnly || !it.read) &&
            (selectedSourceIds.isEmpty() || it.subscriptionId in selectedSourceIds)
    }
    val articleCards = visibleArticles.map { it.toPreviewUi(timeZone = timeZone) }.toImmutableList()

    val canFollow: Boolean = url.isNotBlank()
}
