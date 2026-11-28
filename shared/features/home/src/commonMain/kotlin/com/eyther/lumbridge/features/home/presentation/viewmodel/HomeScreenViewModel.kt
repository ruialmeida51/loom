package com.eyther.lumbridge.features.home.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayFirstAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedStreamUseCase
import com.eyther.lumbridge.features.feed.presentation.mapper.toFeedTextResource
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeBalanceStreamUseCase
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeShoppingStreamUseCase
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeTasksStreamUseCase
import com.eyther.lumbridge.features.home.presentation.model.HomeGreetingUi
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersStreamUseCase
import com.eyther.lumbridge.features.shopping.presentation.mapper.toShoppingTextResource
import com.eyther.lumbridge.features.tasks.presentation.mapper.toTasksTextResource
import kotlin.time.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModel(
    private val getHomeTasksStreamUseCase: GetHomeTasksStreamUseCase,
    private val getHomeShoppingStreamUseCase: GetHomeShoppingStreamUseCase,
    private val getFeedStreamUseCase: GetFeedStreamUseCase,
    private val getHomeBalanceStreamUseCase: GetHomeBalanceStreamUseCase,
    private val getMembersStreamUseCase: GetMembersStreamUseCase,
    context: HouseholdContextDomain,
) : HomeScreenViewModelContract() {
    private val refreshRequests = MutableStateFlow(0)
    private val mutableState =
        MutableStateFlow(
                HomeState(
                    today = todayIso(context.timeZone),
                    timeZone = context.timeZone,
                    userId = context.userId,
                    currency = context.currency,
                    greeting =
                        HomeGreetingUi.forHour(
                            Clock.System.now().toLocalDateTime(TimeZone.of(context.timeZone)).hour,
                        ),
                ),
            )
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest {
                        getHomeBalanceStreamUseCase()
                            .delayFirstAtLeast(
                                enabled =
                                    state.value.balanceLoading || state.value.balanceRefreshing,
                            )
                    }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(
                                    balances = result.value,
                                    balanceLoading = false,
                                    balanceRefreshing = false,
                                    balanceError = null,
                                )
                            is AppResult.Failure ->
                                copy(
                                    balanceLoading = false,
                                    balanceRefreshing = false,
                                    balanceError = result.error.toExpensesTextResource(),
                                )
                        }
                    }
            }
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest {
                        getMembersStreamUseCase(householdId = context.id, includeInactive = true)
                    }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success -> copy(members = result.value)
                            // Names are optional decoration; balances still show without them.
                            is AppResult.Failure -> this
                        }
                    }
            }
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
                balanceRefreshing = true,
                balanceLoading = oldState.balances == null,
                balanceError = null,
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

    override fun onTasksErrorDismissed() {
        mutableState.update { oldState -> oldState.copy(tasksError = null) }
    }

    override fun onShoppingErrorDismissed() {
        mutableState.update { oldState -> oldState.copy(shoppingError = null) }
    }

    override fun onBalanceErrorDismissed() {
        mutableState.update { oldState -> oldState.copy(balanceError = null) }
    }

    override fun onFeedErrorDismissed() {
        mutableState.update { oldState -> oldState.copy(feedError = null) }
    }
}
