package com.eyther.lumbridge.features.feed.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.errors.FeedError

class FollowFeedUseCase(
    private val feedDataSource: FeedDataSource,
) {

    suspend operator fun invoke(url: String): AppResult<Unit> {
        if (!url.trim().startsWith("https://") || url.length > 2048)
            return AppResult.Failure(FeedError.InvalidUrl())
        return feedDataSource.followFeed(url.trim())
    }
}
