package com.eyther.lumbridge.features.feed.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayFirstAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.presentation.components.snackbar.DEFAULT_ALERT_DURATION
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.feed.domain.errors.FeedError
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionDomain
import com.eyther.lumbridge.features.feed.domain.usecase.FollowFeedUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedSubscriptionsStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.RefreshFeedSubscriptionUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.RemoveFeedSubscriptionUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.UpdateFeedSubscriptionUseCase
import com.eyther.lumbridge.features.feed.generated.resources.Res
import com.eyther.lumbridge.features.feed.generated.resources.feed_refreshed
import com.eyther.lumbridge.features.feed.generated.resources.feed_removed
import com.eyther.lumbridge.features.feed.generated.resources.feed_saved
import com.eyther.lumbridge.features.feed.generated.resources.feed_saved_pending
import com.eyther.lumbridge.features.feed.presentation.mapper.toFeedTextResource
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedSubscriptionsScreenViewModelContract
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedSubscriptionsState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class FeedSubscriptionsScreenViewModel(
    private val getFeedSubscriptionsStreamUseCase: GetFeedSubscriptionsStreamUseCase,
    private val followFeedUseCase: FollowFeedUseCase,
    private val refreshFeedSubscriptionUseCase: RefreshFeedSubscriptionUseCase,
    private val updateFeedSubscriptionUseCase: UpdateFeedSubscriptionUseCase,
    private val removeFeedSubscriptionUseCase: RemoveFeedSubscriptionUseCase,
) : FeedSubscriptionsScreenViewModelContract() {
    private data class InnerInputState(val url: String = "")

    private val inputState = MutableStateFlow(InnerInputState())
    private val refreshRequests = MutableStateFlow(0)
    private var editingSubscription: FeedSubscriptionDomain? = null
    private var feedbackJob: Job? = null
    private val mutableState =
        MutableStateFlow(FeedSubscriptionsState())
            .observeWith(viewModelScope) { state ->
                inputState.updateState(state) { input -> copy(url = input.url) }
            }
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest {
                        getFeedSubscriptionsStreamUseCase()
                            .delayFirstAtLeast(
                                enabled = state.value.loading || state.value.refreshing,
                            )
                    }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(
                                    items = result.value.toImmutableList(),
                                    loading = false,
                                    refreshing = false,
                                    hasLoaded = true,
                                    loadError = null,
                                )
                            is AppResult.Failure ->
                                copy(
                                    loading = false,
                                    refreshing = false,
                                    loadError = result.error.toFeedTextResource(),
                                )
                        }
                    }
            }
    override val state = mutableState.asStateFlow()

    override fun onRefresh() {
        if (state.value.refreshing) return
        mutableState.update { oldState -> oldState.copy(refreshing = true, loadError = null) }
        reload()
    }

    override fun onAddFeed() {
        if (state.value.busy) return
        editingSubscription = null
        inputState.value = InnerInputState()
        mutableState.update { oldState ->
            oldState.copy(showForm = true, editing = false, formError = null)
        }
    }

    override fun onEditFeed(subscription: FeedSubscriptionDomain) {
        if (state.value.busy) return
        editingSubscription = subscription
        inputState.value = InnerInputState(url = subscription.url)
        mutableState.update { oldState ->
            oldState.copy(showForm = true, editing = true, formError = null)
        }
    }

    override fun onUrlChanged(value: String) {
        if (state.value.busy) return
        inputState.value = InnerInputState(url = value)
        mutableState.update { oldState -> oldState.copy(formError = null) }
    }

    override fun onSaveFeed() {
        if (!state.value.showForm || !state.value.canSave) return
        val target = editingSubscription
        val url = inputState.value.url
        mutableState.update { oldState -> oldState.copy(busy = true, formError = null) }
        launch {
            if (target == null) {
                when (val result = followFeedUseCase(url = url)) {
                    is AppResult.Success -> saved()
                    is AppResult.Failure ->
                        if (result.error is FeedError.RefreshFailed) {
                            saved()
                            mutableState.update { oldState ->
                                oldState.copy(
                                    error = TextResource.Resource(Res.string.feed_saved_pending),
                                    feedback = null,
                                )
                            }
                        } else formFailed(result)
                }
            } else {
                when (
                    val result = updateFeedSubscriptionUseCase(subscription = target, url = url)
                ) {
                    is AppResult.Success -> {
                        saved()
                        refresh(id = result.value)
                    }
                    is AppResult.Failure -> formFailed(result)
                }
            }
        }
    }

    override fun onRefreshFeed(subscription: FeedSubscriptionDomain) {
        if (state.value.busy) return
        mutableState.update { oldState -> oldState.copy(busy = true, busyId = subscription.id) }
        launch { refresh(id = subscription.id) }
    }

    private suspend fun refresh(id: String) {
        mutableState.update { oldState -> oldState.copy(busy = true, busyId = id, error = null) }
        val result = refreshFeedSubscriptionUseCase(id = id)
        mutableState.update { oldState ->
            when (result) {
                is AppResult.Success ->
                    oldState.copy(
                        busy = false,
                        busyId = null,
                        feedback = TextResource.Resource(Res.string.feed_refreshed),
                    )
                is AppResult.Failure ->
                    oldState.copy(
                        busy = false,
                        busyId = null,
                        error = result.error.toFeedTextResource(),
                    )
            }
        }
        reload()
        if (result is AppResult.Success) scheduleFeedbackDismissal()
    }

    override fun onRemoveFeed(subscription: FeedSubscriptionDomain) {
        if (state.value.busy) return
        mutableState.update { oldState -> oldState.copy(removing = subscription) }
    }

    override fun onConfirmRemoval() {
        if (state.value.busy) return
        val target = state.value.removing ?: return
        mutableState.update { oldState ->
            oldState.copy(busy = true, busyId = target.id, error = null)
        }
        launch {
            val result = removeFeedSubscriptionUseCase(id = target.id)
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            busy = false,
                            busyId = null,
                            removing = null,
                            feedback = TextResource.Resource(Res.string.feed_removed),
                        )
                    is AppResult.Failure ->
                        oldState.copy(
                            busy = false,
                            busyId = null,
                            removing = null,
                            error = result.error.toFeedTextResource(),
                        )
                }
            }
            reload()
            if (result is AppResult.Success) scheduleFeedbackDismissal()
        }
    }

    override fun onDismissRemoval() {
        if (!state.value.busy) mutableState.update { oldState -> oldState.copy(removing = null) }
    }

    override fun onCloseForm() {
        if (!state.value.busy)
            mutableState.update { oldState -> oldState.copy(showForm = false, formError = null) }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null, loadError = null, formError = null)
        }
    }

    override fun onFeedbackDismissed() {
        mutableState.update { oldState -> oldState.copy(feedback = null) }
    }

    private fun reload() {
        refreshRequests.update { oldState -> oldState + 1 }
    }

    private fun saved() {
        inputState.value = InnerInputState()
        editingSubscription = null
        mutableState.update { oldState ->
            oldState.copy(
                busy = false,
                showForm = false,
                formError = null,
                feedback = TextResource.Resource(Res.string.feed_saved),
            )
        }
        reload()
        scheduleFeedbackDismissal()
    }

    private fun formFailed(result: AppResult.Failure) {
        mutableState.update { oldState ->
            oldState.copy(busy = false, formError = result.error.toFeedTextResource())
        }
    }

    private fun scheduleFeedbackDismissal() {
        feedbackJob?.cancel()
        feedbackJob = launch {
            delay(DEFAULT_ALERT_DURATION)
            onFeedbackDismissed()
        }
    }
}
