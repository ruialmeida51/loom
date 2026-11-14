package com.eyther.lumbridge.features.feed.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource

class FollowFeedUseCase(
    private val feedDataSource: FeedDataSource,
) {

    suspend operator fun invoke(url: String): AppResult<Unit> = feedDataSource.followFeed(url = url)
}
