package com.eyther.lumbridge.features.feed.data.fixtures

import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedCursorDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedPageDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedQueryDomain
import kotlin.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flowOf

internal class FeedPagingFixtureDataSource : FeedDataSource {
    var articles =
        (1..45).map { number ->
            val source = if (number % 2 == 0) "world" else "business"
            ArticleDomain(
                id = number.toString().padStart(2, '0'),
                title = "Story $number",
                summary = "Summary",
                url = "https://example.test/story/$number",
                source = source,
                publishedAt = Instant.parse("2026-11-17T12:00:00Z"),
                read = false,
                subscriptionId = source,
            )
        }
    val requests = mutableListOf<Pair<FeedQueryDomain, FeedCursorDomain?>>()
    var firstPageGate: CompletableDeferred<AppResult<FeedPageDomain>>? = null
    var nextPageGate: CompletableDeferred<AppResult<FeedPageDomain>>? = null
    var failNextPage = false

    override fun stream() = flowOf(AppResult.Success(articles.take(3)))

    override suspend fun getPage(
        query: FeedQueryDomain,
        after: FeedCursorDomain?,
    ): AppResult<FeedPageDomain> {
        requests += query to after
        if (after == null) {
            val gate = firstPageGate
            if (gate != null) {
                firstPageGate = null
                return gate.await()
            }
        }
        if (after != null) {
            val gate = nextPageGate
            if (gate != null) {
                nextPageGate = null
                return gate.await()
            }
            if (failNextPage) {
                failNextPage = false
                return AppResult.Failure(CommonError.NoConnection())
            }
        }
        val matches =
            articles
                .filter {
                    (query.subscriptionIds.isEmpty() ||
                        it.subscriptionId in query.subscriptionIds) &&
                        (!query.unreadOnly || !it.read) &&
                        (after == null ||
                            it.publishedAt < after.publishedAt ||
                            it.publishedAt == after.publishedAt && it.id < after.id)
                }
                .sortedWith(
                    compareByDescending<ArticleDomain> { it.publishedAt }
                        .thenByDescending { it.id },
                )
        val page = matches.take(query.pageSize)
        return AppResult.Success(
            FeedPageDomain(
                items = page,
                nextCursor =
                    page
                        .lastOrNull()
                        ?.takeIf { matches.size > query.pageSize }
                        ?.let {
                            FeedCursorDomain(publishedAt = it.publishedAt, id = it.id)
                        },
            ),
        )
    }

    override suspend fun followFeed(url: String): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun markArticleRead(id: String, read: Boolean): AppResult<Unit> {
        articles = articles.map { if (it.id == id) it.copy(read = read) else it }
        return AppResult.Success(Unit)
    }
}
