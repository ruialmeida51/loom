package com.eyther.lumbridge.features.home.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayFirstAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.MarkArticleReadUseCase
import com.eyther.lumbridge.features.feed.presentation.mapper.toFeedTextResource
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeShoppingStreamUseCase
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeTasksStreamUseCase
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.shopping.presentation.mapper.toShoppingTextResource
import com.eyther.lumbridge.features.tasks.presentation.mapper.toTasksTextResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModel(
    private val getHomeTasksStreamUseCase: GetHomeTasksStreamUseCase,
    private val getHomeShoppingStreamUseCase: GetHomeShoppingStreamUseCase,
    private val getFeedStreamUseCase: GetFeedStreamUseCase,
    private val markArticleReadUseCase: MarkArticleReadUseCase,
    context: HouseholdContextDomain,
) : HomeScreenViewModelContract() {
    private val refreshRequests = MutableStateFlow(0)
    private val mutableState =
        MutableStateFlow(HomeState(today = todayIso(context.timeZone), timeZone = context.timeZone))
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest {
                        getHomeTasksStreamUseCase()
                            .delayFirstAtLeast(
                                enabled = state.value.tasksLoading || state.value.tasksRefreshing,
                            )
                    }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(
                                    taskItems = result.value.tasks,
                                    today = result.value.today,
                                    tasksLoading = false,
                                    tasksRefreshing = false,
                                    tasksError = null,
                                )
                            is AppResult.Failure ->
                                copy(
                                    tasksLoading = false,
                                    tasksRefreshing = false,
                                    tasksError = result.error.toTasksTextResource(),
                                )
                        }
                    }
            }
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest {
                        getHomeShoppingStreamUseCase()
                            .delayFirstAtLeast(
                                enabled =
                                    state.value.shoppingLoading || state.value.shoppingRefreshing,
                            )
                    }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(
                                    shoppingPreview = result.value,
                                    shoppingLoading = false,
                                    shoppingRefreshing = false,
                                    shoppingError = null,
                                )
                            is AppResult.Failure ->
                                copy(
                                    shoppingLoading = false,
                                    shoppingRefreshing = false,
                                    shoppingError = result.error.toShoppingTextResource(),
                                )
                        }
                    }
            }
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest {
                        getFeedStreamUseCase()
                            .delayFirstAtLeast(
                                enabled = state.value.feedLoading || state.value.feedRefreshing,
                            )
                    }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(
                                    feedItems = result.value,
                                    feedLoading = false,
                                    feedRefreshing = false,
                                    feedError = null,
                                )
                            is AppResult.Failure ->
                                copy(
                                    feedLoading = false,
                                    feedRefreshing = false,
                                    feedError = result.error.toFeedTextResource(),
                                )
                        }
                    }
            }
    override val state = mutableState.asStateFlow()

    override fun onRetry() {
        if (state.value.refreshing) return
        mutableState.update { oldState ->
            oldState.copy(
                tasksRefreshing = true,
                shoppingRefreshing = true,
                feedRefreshing = true,
                tasksLoading = oldState.taskItems == null,
                shoppingLoading = oldState.shoppingPreview == null,
                feedLoading = oldState.feedItems == null,
                tasksError = null,
                shoppingError = null,
                feedError = null,
            )
        }
        refreshRequests.update { oldState -> oldState + 1 }
    }

    override fun onArticleOpened(id: String) {
        launch {
            when (val result = markArticleReadUseCase(id = id, read = true)) {
                is AppResult.Success -> Unit
                is AppResult.Failure ->
                    mutableState.update { oldState ->
                        oldState.copy(feedError = result.error.toFeedTextResource())
                    }
            }
        }
    }

    override fun onTasksErrorDismissed() {
        mutableState.update { oldState -> oldState.copy(tasksError = null) }
    }

    override fun onShoppingErrorDismissed() {
        mutableState.update { oldState -> oldState.copy(shoppingError = null) }
    }

    override fun onFeedErrorDismissed() {
        mutableState.update { oldState -> oldState.copy(feedError = null) }
    }
}
