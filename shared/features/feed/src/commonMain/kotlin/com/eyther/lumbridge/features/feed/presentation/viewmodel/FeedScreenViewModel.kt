package com.eyther.lumbridge.features.feed.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.launchWith
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.errors.FeedError
import com.eyther.lumbridge.features.feed.domain.model.FeedQueryDomain
import com.eyther.lumbridge.features.feed.domain.model.StarterFeedDomain
import com.eyther.lumbridge.features.feed.domain.usecase.FollowFeedUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedPageUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedSubscriptionsStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetStarterFeedsUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.RefreshFeedsUseCase
import com.eyther.lumbridge.features.feed.presentation.mapper.toFeedTextResource
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedState
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class FeedScreenViewModel(
    private val getFeedPageUseCase: GetFeedPageUseCase,
    private val getFeedSubscriptionsStreamUseCase: GetFeedSubscriptionsStreamUseCase,
    private val getStarterFeedsUseCase: GetStarterFeedsUseCase,
    private val followFeedUseCase: FollowFeedUseCase,
    private val refreshFeedsUseCase: RefreshFeedsUseCase,
    timeZone: String = "UTC",
) : FeedScreenViewModelContract() {

    private data class InnerInputState(val url: String = "")

    private val sourcesRefreshRequests = MutableStateFlow(0)
    private val inputState = MutableStateFlow(InnerInputState())
    private var requestGeneration = 0

    /** The in-flight next-page read; touched on the main thread only. */
    private var loadMoreJob: Job? = null

    private val mutableState =
        MutableStateFlow(
                FeedState(
                    timeZone = timeZone,
                    suggestions = getStarterFeedsUseCase().toImmutableList(),
                ),
            )
            .launchWith(viewModelScope) { state ->
                val generation = ++requestGeneration
                loadFirstPage(
                    state = state,
                    query = state.value.query,
                    generation = generation,
                    minimumDelay = true,
                )
            }
            .observeWith(viewModelScope) { state ->
                inputState.updateState(state) { input -> copy(url = input.url) }
            }
            .observeWith(viewModelScope) { state ->
                sourcesRefreshRequests
                    .flatMapLatest { getFeedSubscriptionsStreamUseCase() }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(sources = result.value.toImmutableList(), sourcesError = null)
                            is AppResult.Failure ->
                                copy(sourcesError = result.error.toFeedTextResource())
                        }
                    }
                    .onEach { result ->
                        if (result is AppResult.Success) {
                            val available = result.value.map { it.id }.toSet()
                            val selected = state.value.selectedSourceIds
                            if (!available.containsAll(selected)) {
                                state.update { oldState ->
                                    oldState.copy(
                                        selectedSourceIds =
                                            oldState.selectedSourceIds
                                                .intersect(available)
                                                .toPersistentSet(),
                                    )
                                }
                                requestFirstPage(clearItems = true)
                            }
                        }
                    }
            }

    override val state = mutableState.asStateFlow()

    override fun onSourceSelected(id: String) {
        mutableState.update { oldState ->
            oldState.copy(
                selectedSourceIds =
                    if (id in oldState.selectedSourceIds) oldState.selectedSourceIds.remove(id)
                    else oldState.selectedSourceIds.add(id),
            )
        }
        requestFirstPage(clearItems = true)
    }

    override fun onAllSourcesSelected() {
        if (state.value.selectedSourceIds.isEmpty()) return
        mutableState.update { oldState -> oldState.copy(selectedSourceIds = persistentSetOf()) }
        requestFirstPage(clearItems = true)
    }

    override fun onSourcesChanged() {
        sourcesRefreshRequests.update { oldState -> oldState + 1 }
        requestFirstPage(clearItems = false)
    }

    private fun requestFirstPage(clearItems: Boolean) {
        loadMoreJob?.cancel()
        val generation = ++requestGeneration
        val query = state.value.query
        mutableState.update { oldState ->
            oldState.copy(
                items = if (clearItems) emptyList() else oldState.items,
                nextCursor = if (clearItems) null else oldState.nextCursor,
                loading = clearItems || !oldState.hasLoaded,
                hasLoaded = !clearItems && oldState.hasLoaded,
                loadingMore = false,
                loadMoreError = null,
                loadError = null,
                refreshing = false,
            )
        }
        launch {
            loadFirstPage(
                state = mutableState,
                query = query,
                generation = generation,
                minimumDelay = false,
            )
        }
    }

    private suspend fun loadFirstPage(
        state: MutableStateFlow<FeedState>,
        query: FeedQueryDomain,
        generation: Int,
        minimumDelay: Boolean,
    ) {
        val result =
            if (minimumDelay) delayAtLeast { getFeedPageUseCase(query = query) }
            else getFeedPageUseCase(query = query)
        if (generation != requestGeneration) return
        state.update { oldState ->
            when (result) {
                is AppResult.Success ->
                    oldState.copy(
                        items = result.value.items,
                        nextCursor = result.value.nextCursor,
                        loadError = null,
                        loadMoreError = null,
                        loading = false,
                        loadingMore = false,
                        refreshing = false,
                        hasLoaded = true,
                    )
                is AppResult.Failure ->
                    oldState.copy(
                        loadError = result.error.toFeedTextResource(),
                        loading = false,
                        refreshing = false,
                        loadingMore = false,
                    )
            }
        }
    }

    override fun onLoadMore() {
        // The job, not a state flag, stops a second read of the same page.
        if (loadMoreJob?.isActive == true) return
        val current = state.value
        val cursor = current.nextCursor ?: return
        if (!current.pagination.canLoadMore) return
        val generation = requestGeneration
        val query = current.query
        mutableState.update { oldState -> oldState.copy(loadingMore = true, loadMoreError = null) }
        loadMoreJob = launch {
            val result = getFeedPageUseCase(query = query, after = cursor)
            if (generation != requestGeneration) return@launch
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            items = (oldState.items + result.value.items).distinctBy { it.id },
                            nextCursor = result.value.nextCursor,
                            loadingMore = false,
                        )
                    is AppResult.Failure ->
                        oldState.copy(
                            loadingMore = false,
                            loadMoreError = result.error.toFeedTextResource(),
                        )
                }
            }
        }
    }

    override fun onRetryLoadMore() {
        mutableState.update { oldState -> oldState.copy(loadMoreError = null) }
        onLoadMore()
    }

    override fun onRetry() {
        if (state.value.refreshing) return
        loadMoreJob?.cancel()
        val generation = ++requestGeneration
        val query = state.value.query
        mutableState.update { oldState ->
            oldState.copy(
                refreshing = true,
                loadingMore = false,
                loadMoreError = null,
            )
        }
        launch {
            val result = delayAtLeast { refreshFeedsUseCase() }
            if (generation != requestGeneration) return@launch
            mutableState.update { oldState ->
                oldState.copy(
                    error =
                        if (result is AppResult.Failure) result.error.toFeedTextResource()
                        else null,
                )
            }
            sourcesRefreshRequests.update { oldState -> oldState + 1 }
            loadFirstPage(
                state = mutableState,
                query = query,
                generation = generation,
                minimumDelay = false,
            )
        }
    }

    override fun onStarterFeedSelected(feed: StarterFeedDomain) {
        if (state.value.busy) return
        inputState.value = InnerInputState(url = feed.url)
        mutableState.update { oldState -> oldState.copy(showFollow = true, formError = null) }
    }

    override fun onUrlChanged(value: String) {
        inputState.update { oldState -> oldState.copy(url = value) }
    }

    override fun onFollowSheetVisibilityChanged(value: Boolean) {
        if (!value && state.value.busy) return
        if (value) inputState.value = InnerInputState()
        mutableState.update { oldState -> oldState.copy(showFollow = value, formError = null) }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(
                error = null,
                loadError = null,
                formError = null,
                sourcesError = null,
                loadMoreError = null,
            )
        }
    }

    override fun onFollowFeed() {
        if (state.value.busy) return
        val url = inputState.value.url
        mutableState.update { oldState -> oldState.copy(busy = true, formError = null) }
        launch {
            when (val result = followFeedUseCase(url)) {
                is AppResult.Success -> {
                    inputState.value = InnerInputState()
                    mutableState.update { oldState ->
                        oldState.copy(showFollow = false, busy = false)
                    }
                    onSourcesChanged()
                }
                is AppResult.Failure ->
                    if (result.error is FeedError.RefreshFailed) {
                        inputState.value = InnerInputState()
                        mutableState.update { oldState ->
                            oldState.copy(
                                showFollow = false,
                                busy = false,
                                error = result.error.toFeedTextResource(),
                            )
                        }
                        onSourcesChanged()
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
