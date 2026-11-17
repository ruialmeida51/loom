package com.eyther.lumbridge.features.feed.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.feed.domain.datasource.FeedSubscriptionDataSource
import com.eyther.lumbridge.features.feed.domain.errors.FeedError
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

private const val MAX_CONCURRENT_REFRESHES = 3

/** Manual news refresh fetches publishers, with bounded concurrency and per-source rate limits. */
class RefreshFeedsUseCase(private val feedSubscriptionDataSource: FeedSubscriptionDataSource) {
    suspend operator fun invoke(): AppResult<Unit> = safeCall {
        val subscriptions = feedSubscriptionDataSource.stream().first().getOrThrow()
        val permits = Semaphore(permits = MAX_CONCURRENT_REFRESHES)
        val results = coroutineScope {
            subscriptions
                .map { subscription ->
                    async {
                        permits.withPermit {
                            feedSubscriptionDataSource.refresh(id = subscription.id)
                        }
                    }
                }
                .awaitAll()
        }
        results
            .filterIsInstance<AppResult.Failure>()
            .firstOrNull { it.error !is FeedError.RefreshRateLimited }
            ?.let { throw it.error }
        Unit
    }
}
