package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.nowIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.feed.data.network.validation.parseFeedUrl
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class DemoFeedDataSource(private val store: DemoHouseholdStore) : FeedDataSource {

    override fun stream() = store.articles.map { AppResult.Success(it) }

    override suspend fun followFeed(url: String) = safeCall {
        val address = parseFeedUrl(value = url)
        val article = ArticleDomain(
            id = newId(),
            title = "Preview subscription",
            summary = "A preview of your new feed.",
            url = address,
            source = "Preview feed",
            publishedAt = nowIso(),
            read = false,
        )
        store.articles.update { articles ->
            articles + article
        }
        Unit
    }

    override suspend fun markArticleRead(id: String, read: Boolean) = safeCall {
        store.articles.update { articles ->
            articles.map { article ->
                if (article.id == id) article.copy(read = read) else article
            }
        }
        Unit
    }
}
