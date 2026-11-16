package com.eyther.lumbridge.features.feed.data.local

import com.eyther.lumbridge.features.feed.domain.datasource.FeedCatalogDataSource
import com.eyther.lumbridge.features.feed.domain.model.StarterFeedDomain

class CommonFeedCatalogDataSource : FeedCatalogDataSource {
    override fun getSources(): List<StarterFeedDomain> =
        listOf(
            StarterFeedDomain(
                publisher = "BBC News · World",
                url = "https://feeds.bbci.co.uk/news/world/rss.xml",
            ),
            StarterFeedDomain(publisher = "NASA", url = "https://www.nasa.gov/feed/"),
        )
}
