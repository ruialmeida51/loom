package com.eyther.lumbridge.features.feed.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayAtLeast
import com.eyther.lumbridge.core.coroutines.extensions.delayFirstAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.errors.FeedError
import com.eyther.lumbridge.features.feed.domain.model.StarterFeedDomain
import com.eyther.lumbridge.features.feed.domain.usecase.FollowFeedUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetStarterFeedsUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.MarkArticleReadUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.RefreshFeedsUseCase
import com.eyther.lumbridge.features.feed.presentation.mapper.toFeedTextResource
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class FeedScreenViewModel(
    private val getFeedStreamUseCase: GetFeedStreamUseCase,
    private val getStarterFeedsUseCase: GetStarterFeedsUseCase,
    private val followFeedUseCase: FollowFeedUseCase,
    private val markArticleReadUseCase: MarkArticleReadUseCase,
    private val refreshFeedsUseCase: RefreshFeedsUseCase,
    timeZone: String = "UTC",
) : FeedScreenViewModelContract() {

    private data class InnerInputState(
        val url: String = "",
    )

    private val refreshRequests = MutableStateFlow(0)
    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState =
        MutableStateFlow(
                FeedState(
                    timeZone = timeZone,
                    suggestions = getStarterFeedsUseCase().toImmutableList(),
                ),
            )
            .observeWith(viewModelScope) { state ->
                inputState.updateState(state) { input ->
                    copy(url = input.url)
                }
            }
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest {
                        getFeedStreamUseCase()
                            .delayFirstAtLeast(
                                enabled = state.value.loading,
                            )
                    }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(
                                    items = result.value,
                                    loadError = null,
                                    loading = false,
                                    refreshing = publishersRefreshing,
                                    hasLoaded = true,
                                )
                            is AppResult.Failure ->
                                copy(
                                    loadError = result.error.toFeedTextResource(),
                                    loading = false,
                                    refreshing = publishersRefreshing,
                                )
                        }
                    }
            }

    override val state = mutableState.asStateFlow()

    override fun onSourcesChanged() {
        mutableState.update { oldState -> oldState.copy(error = null) }
        refreshRequests.update { oldState -> oldState + 1 }
    }

    override fun onStarterFeedSelected(feed: StarterFeedDomain) {
        if (state.value.busy) return
        inputState.value = InnerInputState(url = feed.url)
        mutableState.update { oldState -> oldState.copy(showFollow = true, formError = null) }
    }

    override fun onRetry() {
        if (state.value.refreshing) return
        mutableState.update { oldState ->
            oldState.copy(refreshing = true, publishersRefreshing = true)
        }
        launch {
            val result = delayAtLeast { refreshFeedsUseCase() }
            mutableState.update { oldState ->
                oldState.copy(
                    publishersRefreshing = false,
                    error =
                        if (result is AppResult.Failure) result.error.toFeedTextResource()
                        else null,
                )
            }
            refreshRequests.update { oldState -> oldState + 1 }
        }
    }

    override fun onUrlChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(url = value)
        }
    }

    override fun onFollowSheetVisibilityChanged(value: Boolean) {
        if (!value && state.value.busy) return

        if (value) inputState.value = InnerInputState()

        mutableState.update { oldState ->
            oldState.copy(showFollow = value, formError = null)
        }
    }

    override fun onUnreadOnlyChanged(value: Boolean) {
        mutableState.update { oldState ->
            oldState.copy(unreadOnly = value)
        }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null, loadError = null, formError = null)
        }
    }

    override fun onFollowFeed() {
        if (state.value.busy) return
        val url = inputState.value.url

        mutableState.update { oldState ->
            oldState.copy(busy = true, formError = null)
        }

        launch {
            when (val result = followFeedUseCase(url)) {
                is AppResult.Success -> {
                    inputState.update { oldState ->
                        oldState.copy(url = "")
                    }
                    mutableState.update { oldState ->
                        oldState.copy(showFollow = false, busy = false)
                    }
                    onSourcesChanged()
                }

                is AppResult.Failure -> {
                    if (result.error is FeedError.RefreshFailed) {
                        inputState.value = InnerInputState()
                        mutableState.update { oldState ->
                            oldState.copy(
                                showFollow = false,
                                busy = false,
                                error = result.error.toFeedTextResource(),
                            )
                        }
                    } else
                        mutableState.update { oldState ->
                            oldState.copy(
                                busy = false,
                                formError = result.error.toFeedTextResource(),
                            )
                        }
                }
            }
        }
    }

    override fun onMarkRead(id: String, read: Boolean) {
        launch {
            val result = markArticleReadUseCase(id = id, read = read)

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            items =
                                oldState.items.map { article ->
                                    if (article.id == id) article.copy(read = read) else article
                                },
                        )

                    is AppResult.Failure ->
                        oldState.copy(
                            error = result.error.toFeedTextResource(),
                        )
                }
            }
        }
    }
}
