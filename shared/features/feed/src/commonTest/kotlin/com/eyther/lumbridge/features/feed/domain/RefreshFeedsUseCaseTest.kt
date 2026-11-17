package com.eyther.lumbridge.features.feed.domain

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.data.fixtures.FeedSubscriptionFixture
import com.eyther.lumbridge.features.feed.domain.errors.FeedError
import com.eyther.lumbridge.features.feed.domain.usecase.RefreshFeedsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class RefreshFeedsUseCaseTest {
    @Test
    fun aRecentlyFetchedSourceDoesNotPreventRefreshingTheOthers() = runTest {
        val source = fixture(count = 2)
        source.refreshResults["1"] = AppResult.Failure(FeedError.RefreshRateLimited())
        assertIs<AppResult.Success<Unit>>(RefreshFeedsUseCase(source)())
        assertEquals(setOf("1", "2"), source.refreshedIds.toSet())
    }

    @Test
    fun aPublisherFailureIsPreservedWhileOtherPublishersStillRun() = runTest {
        val source = fixture(count = 2)
        val failure = FeedError.RefreshFailed()
        source.refreshResults["1"] = AppResult.Failure(failure)
        assertSame(failure, assertIs<AppResult.Failure>(RefreshFeedsUseCase(source)()).error)
        assertEquals(setOf("1", "2"), source.refreshedIds.toSet())
    }

    @Test
    fun refreshHasAtMostThreeConcurrentPublisherRequests() = runTest {
        val source = fixture(count = 6)
        source.refreshGate = CompletableDeferred()
        val refresh = async { RefreshFeedsUseCase(source)() }
        runCurrent()
        assertFalse(refresh.isCompleted)
        assertEquals(3, source.activeRefreshes)
        assertEquals(3, source.refreshedIds.size)
        source.refreshGate?.complete(Unit)
        assertIs<AppResult.Success<Unit>>(refresh.await())
        assertEquals(6, source.refreshedIds.size)
        assertEquals(3, source.peakRefreshes)
        assertEquals(0, source.activeRefreshes)
    }

    private fun fixture(count: Int): FeedSubscriptionFixture =
        FeedSubscriptionFixture().also { source ->
            source.updates.value =
                AppResult.Success(
                    (1..count).map { index ->
                        source.subscription.copy(
                            id = index.toString(),
                            url = "https://rss.impresa.pt/feed/$index.rss",
                        )
                    },
                )
        }
}
