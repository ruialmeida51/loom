package com.eyther.lumbridge.features.feed.domain.datasource

import com.eyther.lumbridge.features.feed.domain.model.StarterFeedDomain

interface FeedCatalogDataSource {
    fun getSources(): List<StarterFeedDomain>
}
