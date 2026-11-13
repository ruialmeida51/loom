package com.eyther.lumbridge.features.feed.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import kotlinx.coroutines.flow.Flow

class GetFeedStreamUseCase(
    private val feedDataSource: FeedDataSource,
) {

    operator fun invoke(): Flow<AppResult<List<ArticleDomain>>> = feedDataSource.stream()
}
