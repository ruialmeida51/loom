package com.eyther.lumbridge.features.feed.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.datasource.FeedSubscriptionDataSource
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionDomain
import kotlinx.coroutines.flow.Flow

class GetFeedSubscriptionsStreamUseCase(
    private val feedSubscriptionDataSource: FeedSubscriptionDataSource,
) {
    operator fun invoke(): Flow<AppResult<List<FeedSubscriptionDomain>>> =
        feedSubscriptionDataSource.stream()
}
