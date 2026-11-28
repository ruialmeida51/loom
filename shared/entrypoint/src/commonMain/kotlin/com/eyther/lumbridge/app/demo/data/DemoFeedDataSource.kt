package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.nowIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.feed.data.network.validation.parseFeedUrl
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedCursorDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedPageDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedQueryDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionStatusDomain
import io.ktor.http.Url
import kotlin.time.Clock
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class DemoFeedDataSource(private val store: DemoHouseholdStore) : FeedDataSource {

    override fun stream() =
        store.articles.map { articles ->
            AppResult.Success(
                articles
                    .sortedWith(
                        compareByDescending<ArticleDomain> { it.publishedAt }
                            .thenByDescending { it.id },
                    )
                    .take(3),
            )
        }

    override suspend fun getPage(
        query: FeedQueryDomain,
        after: FeedCursorDomain?,
    ): AppResult<FeedPageDomain> = safeCall {
        val rows =
            store.articles.value
                .filter { article ->
                    (query.subscriptionIds.isEmpty() ||
                        article.subscriptionId in query.subscriptionIds) &&
                        (after == null ||
                            article.publishedAt < after.publishedAt ||
                            article.publishedAt == after.publishedAt && article.id < after.id)
                }
                .sortedWith(
                    compareByDescending<ArticleDomain> { it.publishedAt }
                        .thenByDescending { it.id },
                )
        val page = rows.take(query.pageSize)
        FeedPageDomain(
            items = page,
            nextCursor =
                page
                    .lastOrNull()
                    ?.takeIf { rows.size > query.pageSize }
                    ?.let {
                        FeedCursorDomain(publishedAt = it.publishedAt, id = it.id)
                    },
        )
    }

    override suspend fun followFeed(url: String) = safeCall {
        val address = parseFeedUrl(value = url)
        val subscriptionId = newId()
        val article =
            ArticleDomain(
                id = newId(),
                title = "Preview subscription",
                summary = "A preview of your new feed.",
                url = address,
                source = Url(address).host,
                publishedAt = Clock.System.now(),
                subscriptionId = subscriptionId,
            )
        store.articles.update { articles ->
            articles + article
        }
        store.feedSubscriptions.update { subscriptions ->
            subscriptions +
                FeedSubscriptionDomain(
                    id = subscriptionId,
                    url = address,
                    publisher = Url(address).host,
                    lastFetchedAt = nowIso(),
                    status = FeedSubscriptionStatusDomain.READY,
                )
        }
        Unit
    }
}
