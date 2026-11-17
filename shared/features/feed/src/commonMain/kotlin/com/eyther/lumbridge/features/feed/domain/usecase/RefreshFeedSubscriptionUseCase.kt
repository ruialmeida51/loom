package com.eyther.lumbridge.features.feed.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.datasource.FeedSubscriptionDataSource

class RefreshFeedSubscriptionUseCase(
    private val feedSubscriptionDataSource: FeedSubscriptionDataSource,
) {
    suspend operator fun invoke(id: String): AppResult<Unit> =
        feedSubscriptionDataSource.refresh(id = id)
}
