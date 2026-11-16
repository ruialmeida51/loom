package com.eyther.lumbridge.features.home.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayFirstAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.presentation.mapper.toTextResource
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeOverviewStreamUseCase
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModel(
    private val getHomeOverviewStreamUseCase: GetHomeOverviewStreamUseCase,
    context: HouseholdContextDomain,
) : HomeScreenViewModelContract() {

    private val refreshRequests = MutableStateFlow(0)

    private val mutableState =
        MutableStateFlow(HomeState(timeZone = context.timeZone)).observeWith(viewModelScope) { state
            ->
            refreshRequests
                .flatMapLatest { getHomeOverviewStreamUseCase().delayFirstAtLeast() }
                .updateState(state) { result ->
                    when (result) {
                        is AppResult.Success ->
                            copy(
                                overview = result.value,
                                loading = false,
                                refreshing = false,
                                error = null,
                            )

                        is AppResult.Failure ->
                            copy(
                                loading = false,
                                refreshing = false,
                                error = result.error.toTextResource(),
                            )
                    }
                }
        }

    override val state = mutableState.asStateFlow()

    override fun onRetry() {
        if (state.value.refreshing) return
        mutableState.update { oldState ->
            oldState.copy(refreshing = true, loading = !oldState.hasOverview, error = null)
        }
        refreshRequests.update { oldState -> oldState + 1 }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null, overview = oldState.overview?.copy(shoppingError = null))
        }
    }
}
