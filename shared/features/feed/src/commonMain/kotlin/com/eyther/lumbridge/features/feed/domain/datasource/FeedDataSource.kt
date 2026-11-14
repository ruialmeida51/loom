package com.eyther.lumbridge.features.feed.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import kotlinx.coroutines.flow.Flow

interface FeedDataSource {
    fun stream(): Flow<AppResult<List<ArticleDomain>>>

    /** Rejects malformed addresses before saving; a failed initial refresh preserves the subscription. */
    suspend fun followFeed(url: String): AppResult<Unit>

    suspend fun markArticleRead(id: String, read: Boolean): AppResult<Unit>
}
