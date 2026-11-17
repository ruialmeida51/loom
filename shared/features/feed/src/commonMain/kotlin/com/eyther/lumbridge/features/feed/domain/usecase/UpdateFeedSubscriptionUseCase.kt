package com.eyther.lumbridge.features.feed.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.datasource.FeedSubscriptionDataSource
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionDomain

class UpdateFeedSubscriptionUseCase(
    private val feedSubscriptionDataSource: FeedSubscriptionDataSource,
) {
    suspend operator fun invoke(
        subscription: FeedSubscriptionDomain,
        url: String,
    ): AppResult<String> = feedSubscriptionDataSource.update(subscription = subscription, url = url)
}
