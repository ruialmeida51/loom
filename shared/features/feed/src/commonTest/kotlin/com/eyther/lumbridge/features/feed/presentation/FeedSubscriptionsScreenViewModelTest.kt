package com.eyther.lumbridge.features.feed.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.data.fixtures.FeedSubscriptionFixture
import com.eyther.lumbridge.features.feed.data.fixtures.FollowFeedFixture
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionStatusDomain
import com.eyther.lumbridge.features.feed.domain.usecase.FollowFeedUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedSubscriptionsStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.RefreshFeedSubscriptionUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.RemoveFeedSubscriptionUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.UpdateFeedSubscriptionUseCase
import com.eyther.lumbridge.features.feed.presentation.viewmodel.FeedSubscriptionsScreenViewModel
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
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class FeedSubscriptionsScreenViewModelTest {
    @Test
    fun failedFirstRefreshLeavesTheSavedSubscriptionVisibleAndManageable() = runTest {
        withViewModel { source, viewModel ->
            source.updates.value = AppResult.Success(emptyList())
            runCurrent()
            viewModel.onAddFeed()
            viewModel.onUrlChanged(source.subscription.url)
            runCurrent()
            viewModel.onSaveFeed()
            runCurrent()
            assertFalse(viewModel.state.value.showForm)
            assertFalse(viewModel.state.value.busy)
            assertEquals(source.subscription.id, viewModel.state.value.items.single().id)
            assertEquals(
                FeedSubscriptionStatusDomain.REFRESH_FAILED,
                viewModel.state.value.items.single().status,
            )
            assertNotNull(viewModel.state.value.error)
            assertNull(viewModel.state.value.feedback)
        }
    }

    @Test
    fun refreshingWhileEditingRetainsTheDraftAndThePreviousSubscriptionsOnFailure() = runTest {
        withViewModel { source, viewModel ->
            viewModel.onEditFeed(source.subscription)
            viewModel.onUrlChanged("https://feeds.bbci.co.uk/news/rss.xml")
            runCurrent()
            viewModel.onRefresh()
            source.updates.value = AppResult.Failure(CommonError.NoConnection())
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            assertTrue(viewModel.state.value.showForm)
            assertEquals("https://feeds.bbci.co.uk/news/rss.xml", viewModel.state.value.url)
            assertEquals(source.subscription.id, viewModel.state.value.items.single().id)
            assertNotNull(viewModel.state.value.loadError)
            assertFalse(viewModel.state.value.refreshing)
        }
    }

    @Test
    fun repeatedRemovalTapsProduceOneWriteAndSuccessFeedbackExpires() = runTest {
        withViewModel { source, viewModel ->
            source.removeGate = CompletableDeferred()
            viewModel.onRemoveFeed(source.subscription)
            viewModel.onConfirmRemoval()
            viewModel.onConfirmRemoval()
            runCurrent()
            assertEquals(1, source.removeCalls)
            assertTrue(viewModel.state.value.busy)
            assertNull(viewModel.state.value.feedback)
            source.removeGate?.complete(Unit)
            runCurrent()
            assertTrue(viewModel.state.value.items.isEmpty())
            assertNotNull(viewModel.state.value.feedback)
            advanceTimeBy(4_000)
            runCurrent()
            assertNull(viewModel.state.value.feedback)
        }
    }

    private suspend fun TestScope.withViewModel(
        block:
            suspend TestScope.(FeedSubscriptionFixture, FeedSubscriptionsScreenViewModel) -> Unit,
    ) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = FeedSubscriptionFixture()
        val store = ViewModelStore()
        val viewModel =
            FeedSubscriptionsScreenViewModel(
                getFeedSubscriptionsStreamUseCase = GetFeedSubscriptionsStreamUseCase(source),
                followFeedUseCase = FollowFeedUseCase(FollowFeedFixture(source)),
                refreshFeedSubscriptionUseCase = RefreshFeedSubscriptionUseCase(source),
                updateFeedSubscriptionUseCase = UpdateFeedSubscriptionUseCase(source),
                removeFeedSubscriptionUseCase = RemoveFeedSubscriptionUseCase(source),
            )
        store.put("subscriptions", viewModel)
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            block(source, viewModel)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
}
