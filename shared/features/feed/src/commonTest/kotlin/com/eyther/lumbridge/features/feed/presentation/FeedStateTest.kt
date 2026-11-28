package com.eyther.lumbridge.features.feed.presentation

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.data.fixtures.FeedSubscriptionFixture
import com.eyther.lumbridge.features.feed.data.local.CommonFeedCatalogDataSource
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.errors.FeedError
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedCursorDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedPageDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedQueryDomain
import com.eyther.lumbridge.features.feed.domain.usecase.FollowFeedUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedPageUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedSubscriptionsStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetStarterFeedsUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.RefreshFeedsUseCase
import com.eyther.lumbridge.features.feed.presentation.viewmodel.FeedScreenViewModel
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class FeedStateTest {

    private fun article(id: String): ArticleDomain =
        ArticleDomain(
            id = id,
            title = id,
            summary = "",
            url = "https://example.com/$id",
            source = "Example",
            publishedAt = Instant.parse("2026-11-14T00:00:00Z"),
            subscriptionId = "expresso",
        )

    @Test
    fun aSuccessfulRefreshDoesNotEraseADialogOrActionFailure() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val articles =
            MutableStateFlow<AppResult<List<ArticleDomain>>>(AppResult.Success(emptyList()))
        var followRequests = 0
        val source =
            object : FeedDataSource {
                override fun stream() = articles

                override suspend fun getPage(
                    query: FeedQueryDomain,
                    after: FeedCursorDomain?,
                ): AppResult<FeedPageDomain> =
                    when (val result = articles.value) {
                        is AppResult.Success ->
                            AppResult.Success(
                                FeedPageDomain(items = result.value, nextCursor = null),
                            )
                        is AppResult.Failure -> result
                    }

                override suspend fun followFeed(url: String): AppResult<Unit> {
                    followRequests++
                    return AppResult.Failure(FeedError.InvalidUrl())
                }
            }
        val viewModel =
            FeedScreenViewModel(
                getStarterFeedsUseCase =
                    GetStarterFeedsUseCase(feedCatalogDataSource = CommonFeedCatalogDataSource()),
                getFeedPageUseCase = GetFeedPageUseCase(feedDataSource = source),
                getFeedSubscriptionsStreamUseCase =
                    GetFeedSubscriptionsStreamUseCase(
                        feedSubscriptionDataSource = FeedSubscriptionFixture(),
                    ),
                followFeedUseCase = FollowFeedUseCase(feedDataSource = source),
                refreshFeedsUseCase =
                    RefreshFeedsUseCase(feedSubscriptionDataSource = FeedSubscriptionFixture()),
            )
        try {
            backgroundScope.launch { viewModel.state.collect() }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            articles.value = AppResult.Failure(FeedError.RefreshFailed())
            viewModel.onSourcesChanged()
            runCurrent()
            assertNotNull(viewModel.state.value.loadError)
            articles.value = AppResult.Success(emptyList())
            viewModel.onSourcesChanged()
            runCurrent()
            assertNull(viewModel.state.value.displayError)

            val suggestion = viewModel.state.value.suggestions.first()
            viewModel.onStarterFeedSelected(feed = suggestion)
            runCurrent()
            assertEquals(suggestion.url, viewModel.state.value.url)
            assertTrue(viewModel.state.value.showFollow)
            assertEquals(0, followRequests)
            viewModel.onFollowSheetVisibilityChanged(value = true)
            viewModel.onUrlChanged(value = "invalid")
            runCurrent()
            viewModel.onFollowFeed()
            runCurrent()
            val formError = assertNotNull(viewModel.state.value.formError)

            articles.value = AppResult.Success(listOf(article(id = "fresh")))
            viewModel.onSourcesChanged()
            runCurrent()
            assertEquals("fresh", viewModel.state.value.items.single().id)
            assertEquals(formError, viewModel.state.value.formError)
            assertNull(viewModel.state.value.displayError)

            articles.value = AppResult.Success(listOf(article(id = "newer")))
            viewModel.onSourcesChanged()
            runCurrent()
            assertEquals("newer", viewModel.state.value.items.single().id)
            assertEquals(formError, viewModel.state.value.formError)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }
}
