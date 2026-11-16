package com.eyther.lumbridge.features.feed.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayFirstAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.domain.usecase.FollowFeedUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.MarkArticleReadUseCase
import com.eyther.lumbridge.features.feed.presentation.mapper.toFeedTextResource
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class FeedScreenViewModel(
    private val getFeedStreamUseCase: GetFeedStreamUseCase,
    private val followFeedUseCase: FollowFeedUseCase,
    private val markArticleReadUseCase: MarkArticleReadUseCase,
) : FeedScreenViewModelContract() {

    private data class InnerInputState(
        val url: String = "",
    )

    private val refreshRequests = MutableStateFlow(0)
    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState =
        MutableStateFlow(FeedState())
            .observeWith(viewModelScope) { state ->
                inputState.updateState(state) { input ->
                    copy(url = input.url)
                }
            }
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest { getFeedStreamUseCase().delayFirstAtLeast() }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(
                                    items = result.value,
                                    loadError = null,
                                    loading = false,
                                    refreshing = false,
                                    hasLoaded = true,
                                )
                            is AppResult.Failure ->
                                copy(
                                    loadError = result.error.toFeedTextResource(),
                                    loading = false,
                                    refreshing = false,
                                )
                        }
                    }
            }

    override val state = mutableState.asStateFlow()

    override fun onRetry() {
        if (state.value.refreshing) return
        mutableState.update { oldState -> oldState.copy(refreshing = true) }
        refreshRequests.update { oldVersion -> oldVersion + 1 }
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
                }

                is AppResult.Failure ->
                    mutableState.update { oldState ->
                        oldState.copy(
                            busy = false,
                            formError = result.error.toFeedTextResource(),
                        )
                    }
            }
        }
    }

    override fun onMarkRead(item: ArticleDomain, read: Boolean) {
        launch {
            val result = markArticleReadUseCase(id = item.id, read = read)

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            items =
                                oldState.items.map { article ->
                                    if (article.id == item.id) article.copy(read = read)
                                    else article
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
