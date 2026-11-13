package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import kotlinx.coroutines.flow.update

class DemoFeedDataSource(private val store: DemoHouseholdStore) : FeedDataSource {
    override fun observe() = store.articles

    override suspend fun follow(url: String) = safeCall {
        store.articles.update {
            it +
                ArticleDomain(
                    newId(),
                    "Preview subscription",
                    "A preview of your new feed.",
                    url,
                    "Preview feed",
                    nowIso(),
                    false,
                )
        }
        Unit
    }

    override suspend fun markRead(id: String, read: Boolean) = safeCall {
        store.articles.update { it.map { row -> if (row.id == id) row.copy(read = read) else row } }
        Unit
    }
}
