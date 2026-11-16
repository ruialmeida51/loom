package com.eyther.lumbridge.features.home.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.MarkArticleReadUseCase
import com.eyther.lumbridge.features.feed.presentation.mapper.toFeedTextResource
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeRemindersStreamUseCase
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeShoppingStreamUseCase
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.reminders.presentation.mapper.toRemindersTextResource
import com.eyther.lumbridge.features.shopping.presentation.mapper.toShoppingTextResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModel(
    private val getHomeRemindersStreamUseCase: GetHomeRemindersStreamUseCase,
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
                    .flatMapLatest { getHomeRemindersStreamUseCase() }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(
                                    reminderItems = result.value.reminders,
                                    today = result.value.today,
                                    remindersLoading = false,
                                    remindersRefreshing = false,
                                    remindersError = null,
                                )
                            is AppResult.Failure ->
                                copy(
                                    remindersLoading = false,
                                    remindersRefreshing = false,
                                    remindersError = result.error.toRemindersTextResource(),
                                )
                        }
                    }
            }
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest { getHomeShoppingStreamUseCase() }
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
                    .flatMapLatest { getFeedStreamUseCase() }
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
                remindersRefreshing = true,
                shoppingRefreshing = true,
                feedRefreshing = true,
                remindersLoading = oldState.reminderItems == null,
                shoppingLoading = oldState.shoppingPreview == null,
                feedLoading = oldState.feedItems == null,
                remindersError = null,
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

    override fun onRemindersErrorDismissed() {
        mutableState.update { oldState -> oldState.copy(remindersError = null) }
    }

    override fun onShoppingErrorDismissed() {
        mutableState.update { oldState -> oldState.copy(shoppingError = null) }
    }

    override fun onFeedErrorDismissed() {
        mutableState.update { oldState -> oldState.copy(feedError = null) }
    }
}
