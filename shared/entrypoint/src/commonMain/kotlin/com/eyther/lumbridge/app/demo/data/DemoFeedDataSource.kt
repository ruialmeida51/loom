package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.nowIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class DemoFeedDataSource(private val store: DemoHouseholdStore) : FeedDataSource {

    override fun stream() = store.articles.map { AppResult.Success(it) }

    override suspend fun followFeed(url: String) = safeCall {
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

    override suspend fun markArticleRead(id: String, read: Boolean) = safeCall {
        store.articles.update { it.map { row -> if (row.id == id) row.copy(read = read) else row } }
        Unit
    }
}
