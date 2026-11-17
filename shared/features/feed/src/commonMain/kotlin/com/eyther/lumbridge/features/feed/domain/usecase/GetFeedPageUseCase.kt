package com.eyther.lumbridge.features.feed.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.model.FeedCursorDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedPageDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedQueryDomain

class GetFeedPageUseCase(private val feedDataSource: FeedDataSource) {
    suspend operator fun invoke(
        query: FeedQueryDomain,
        after: FeedCursorDomain? = null,
    ): AppResult<FeedPageDomain> = feedDataSource.getPage(query = query, after = after)
}
