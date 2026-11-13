package com.eyther.lumbridge.features.feed.domain.usecase

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource

class ObserveFeedUseCase(private val source: FeedDataSource) {
    operator fun invoke() = source.observe()
}
