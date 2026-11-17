package com.eyther.lumbridge.features.feed.data.fixtures

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.errors.FeedError
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedCursorDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedPageDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedQueryDomain
import kotlinx.coroutines.flow.flowOf

internal class FollowFeedFixture(private val subscriptions: FeedSubscriptionFixture) :
    FeedDataSource {
    override fun stream() = flowOf<AppResult<List<ArticleDomain>>>(AppResult.Success(emptyList()))

    override suspend fun getPage(
        query: FeedQueryDomain,
        after: FeedCursorDomain?,
    ): AppResult<FeedPageDomain> =
        AppResult.Success(FeedPageDomain(items = emptyList(), nextCursor = null))

    override suspend fun followFeed(url: String): AppResult<Unit> {
        subscriptions.updates.value = AppResult.Success(listOf(subscriptions.subscription))
        return AppResult.Failure(FeedError.RefreshFailed())
    }

    override suspend fun markArticleRead(id: String, read: Boolean): AppResult<Unit> =
        AppResult.Success(Unit)
}
