package com.eyther.lumbridge.features.feed.data.fixtures

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.datasource.FeedSubscriptionDataSource
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionStatusDomain
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

internal class FeedSubscriptionFixture : FeedSubscriptionDataSource {
    val subscription =
        FeedSubscriptionDomain(
            id = "expresso",
            url = "https://rss.impresa.pt/feed/latest/expresso.rss",
            publisher = "rss.impresa.pt",
            lastFetchedAt = null,
            status = FeedSubscriptionStatusDomain.REFRESH_FAILED,
        )
    val updates =
        MutableStateFlow<AppResult<List<FeedSubscriptionDomain>>>(
            AppResult.Success(listOf(subscription)),
        )
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)
    val refreshResults = mutableMapOf<String, AppResult<Unit>>()
    val refreshedIds = mutableListOf<String>()
    var refreshGate: CompletableDeferred<Unit>? = null
    var activeRefreshes = 0
    var peakRefreshes = 0
    var removeGate: CompletableDeferred<Unit>? = null
    var removeCalls = 0

    override fun stream() = updates

    override suspend fun refresh(id: String): AppResult<Unit> {
        refreshedIds += id
        activeRefreshes++
        peakRefreshes = maxOf(peakRefreshes, activeRefreshes)
        try {
            refreshGate?.await()
            val result = refreshResults[id] ?: refreshResult
            if (result is AppResult.Success)
                updates.value =
                    AppResult.Success(
                        (updates.value as? AppResult.Success)?.value.orEmpty().map { subscription ->
                            if (subscription.id == id)
                                subscription.copy(status = FeedSubscriptionStatusDomain.READY)
                            else subscription
                        },
                    )
            return result
        } finally {
            activeRefreshes--
        }
    }

    override suspend fun update(
        subscription: FeedSubscriptionDomain,
        url: String,
    ): AppResult<String> {
        updates.value =
            AppResult.Success(
                listOf(
                    subscription.copy(
                        id = "replacement",
                        url = url,
                        status = FeedSubscriptionStatusDomain.PENDING,
                    ),
                ),
            )
        return AppResult.Success("replacement")
    }

    override suspend fun remove(id: String): AppResult<Unit> {
        removeCalls++
        removeGate?.await()
        updates.value = AppResult.Success(emptyList())
        return AppResult.Success(Unit)
    }
}
