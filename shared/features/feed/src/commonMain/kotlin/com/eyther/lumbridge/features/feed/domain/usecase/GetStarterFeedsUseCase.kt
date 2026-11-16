package com.eyther.lumbridge.features.feed.domain.usecase

import com.eyther.lumbridge.features.feed.domain.datasource.FeedCatalogDataSource
import com.eyther.lumbridge.features.feed.domain.model.StarterFeedDomain

class GetStarterFeedsUseCase(private val feedCatalogDataSource: FeedCatalogDataSource) {
    operator fun invoke(): List<StarterFeedDomain> = feedCatalogDataSource.getSources()
}
