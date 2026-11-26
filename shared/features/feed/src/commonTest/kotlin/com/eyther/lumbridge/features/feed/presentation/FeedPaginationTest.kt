package com.eyther.lumbridge.features.feed.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.data.fixtures.FeedPagingFixtureDataSource
import com.eyther.lumbridge.features.feed.data.fixtures.FeedSubscriptionFixture
import com.eyther.lumbridge.features.feed.data.local.CommonFeedCatalogDataSource
import com.eyther.lumbridge.features.feed.domain.model.FeedPageDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionStatusDomain
import com.eyther.lumbridge.features.feed.domain.usecase.FollowFeedUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedPageUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedSubscriptionsStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetStarterFeedsUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.MarkArticleReadUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.RefreshFeedsUseCase
import com.eyther.lumbridge.features.feed.presentation.viewmodel.FeedScreenViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class FeedPaginationTest {
    @Test
    fun pagesKeepEqualTimestampStoriesExactlyOnceAndStopAtTheEnd() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = FeedPagingFixtureDataSource()
        val store = ViewModelStore()
        val viewModel = model(source).also { store.put("feed", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            assertEquals(20, viewModel.state.value.items.size)
            assertNotNull(viewModel.state.value.nextCursor)
            viewModel.onLoadMore()
            runCurrent()
            assertEquals(40, viewModel.state.value.items.size)
            viewModel.onLoadMore()
            runCurrent()
            assertEquals(45, viewModel.state.value.items.size)
            assertEquals(45, viewModel.state.value.items.map { it.id }.toSet().size)
            assertNull(viewModel.state.value.nextCursor)
            viewModel.onLoadMore()
            runCurrent()
            assertEquals(3, source.requests.size)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun sourceUnionAndUnreadSelectionApplyBeforeTheFirstPage() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = FeedPagingFixtureDataSource()
        source.articles =
            source.articles.map { if (it.id in setOf("45", "43")) it.copy(read = true) else it }
        val store = ViewModelStore()
        val viewModel = model(source).also { store.put("feed", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            viewModel.onSourceSelected("business")
            viewModel.onUnreadOnlyChanged(true)
            runCurrent()
            assertEquals(20, viewModel.state.value.items.size)
            assertTrue(
                viewModel.state.value.items.all { it.subscriptionId == "business" && !it.read },
            )
            assertEquals("41", viewModel.state.value.items.first().id)
            viewModel.onSourceSelected("world")
            runCurrent()
            assertEquals(setOf("business", "world"), source.requests.last().first.subscriptionIds)
            assertTrue(viewModel.state.value.items.map { it.subscriptionId }.toSet().size == 2)
            viewModel.onAllSourcesSelected()
            runCurrent()
            assertTrue(source.requests.last().first.subscriptionIds.isEmpty())
            assertTrue(source.requests.last().first.unreadOnly)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun aSlowInitialPageCannotReplaceASelectionMadeWhileOpeningTheReader() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val gate = CompletableDeferred<AppResult<FeedPageDomain>>()
        val source = FeedPagingFixtureDataSource().also { it.firstPageGate = gate }
        val store = ViewModelStore()
        val viewModel = model(source).also { store.put("feed", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            assertTrue(viewModel.state.value.loading)
            viewModel.onSourceSelected("world")
            runCurrent()
            val expected = viewModel.state.value.items
            val cursor = viewModel.state.value.nextCursor
            assertEquals(20, expected.size)
            gate.complete(
                AppResult.Success(
                    FeedPageDomain(items = source.articles.take(3), nextCursor = null),
                ),
            )
            advanceTimeBy(300)
            runCurrent()
            assertEquals(expected, viewModel.state.value.items)
            assertEquals(cursor, viewModel.state.value.nextCursor)
            assertTrue(viewModel.state.value.items.all { it.subscriptionId == "world" })
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun aLatePageFromAnOldFilterCannotReplaceOrAppendToTheNewSelection() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = FeedPagingFixtureDataSource()
        val store = ViewModelStore()
        val viewModel = model(source).also { store.put("feed", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            val gate = CompletableDeferred<AppResult<FeedPageDomain>>()
            source.nextPageGate = gate
            viewModel.onLoadMore()
            runCurrent()
            assertTrue(viewModel.state.value.loadingMore)
            viewModel.onLoadMore()
            runCurrent()
            assertEquals(2, source.requests.size)
            viewModel.onSourceSelected("world")
            runCurrent()
            val expected = viewModel.state.value.items
            val cursor = viewModel.state.value.nextCursor
            gate.complete(
                AppResult.Success(
                    FeedPageDomain(
                        items = source.articles.filter { it.subscriptionId == "business" }.take(4),
                        nextCursor = null,
                    ),
                ),
            )
            runCurrent()
            assertEquals(expected, viewModel.state.value.items)
            assertEquals(cursor, viewModel.state.value.nextCursor)
            assertTrue(viewModel.state.value.items.all { it.subscriptionId == "world" })
            assertFalse(viewModel.state.value.loadingMore)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun aFailedPageRetainsRowsAndRetriesTheSameCursorWithoutSkeletons() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = FeedPagingFixtureDataSource()
        val store = ViewModelStore()
        val viewModel = model(source).also { store.put("feed", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            val cursor = viewModel.state.value.nextCursor
            source.failNextPage = true
            viewModel.onLoadMore()
            runCurrent()
            assertEquals(20, viewModel.state.value.items.size)
            assertNotNull(viewModel.state.value.loadMoreError)
            assertFalse(viewModel.state.value.loading)
            assertFalse(viewModel.state.value.refreshing)
            // Scrolling does not ask for the failed page again; only the explicit retry does.
            viewModel.onLoadMore()
            runCurrent()
            assertEquals(2, source.requests.size)
            viewModel.onRetryLoadMore()
            runCurrent()
            assertEquals(40, viewModel.state.value.items.size)
            assertNull(viewModel.state.value.loadMoreError)
            assertEquals(cursor, source.requests[1].second)
            assertEquals(cursor, source.requests[2].second)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    private fun model(source: FeedPagingFixtureDataSource): FeedScreenViewModel {
        val subscriptions =
            FeedSubscriptionFixture().also { fixture ->
                fixture.updates.value =
                    AppResult.Success(
                        listOf("world", "business").map { id ->
                            FeedSubscriptionDomain(
                                id = id,
                                url = "https://example.test/$id",
                                publisher = id,
                                lastFetchedAt = null,
                                status = FeedSubscriptionStatusDomain.READY,
                            )
                        },
                    )
            }
        return FeedScreenViewModel(
            getFeedPageUseCase = GetFeedPageUseCase(feedDataSource = source),
            getFeedSubscriptionsStreamUseCase =
                GetFeedSubscriptionsStreamUseCase(feedSubscriptionDataSource = subscriptions),
            getStarterFeedsUseCase =
                GetStarterFeedsUseCase(feedCatalogDataSource = CommonFeedCatalogDataSource()),
            followFeedUseCase = FollowFeedUseCase(feedDataSource = source),
            markArticleReadUseCase = MarkArticleReadUseCase(feedDataSource = source),
            refreshFeedsUseCase = RefreshFeedsUseCase(feedSubscriptionDataSource = subscriptions),
        )
    }
}
