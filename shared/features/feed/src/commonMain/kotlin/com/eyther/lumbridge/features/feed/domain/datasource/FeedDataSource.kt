package com.eyther.lumbridge.features.feed.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedCursorDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedPageDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedQueryDomain
import kotlinx.coroutines.flow.Flow

interface FeedDataSource {
    /** Live, bounded Home preview. The reading destination requests explicit pages. */
    fun stream(): Flow<AppResult<List<ArticleDomain>>>

    suspend fun getPage(query: FeedQueryDomain, after: FeedCursorDomain?): AppResult<FeedPageDomain>

    /**
     * Rejects malformed addresses before saving; a failed initial refresh preserves the
     * subscription.
     */
    suspend fun followFeed(url: String): AppResult<Unit>

}
