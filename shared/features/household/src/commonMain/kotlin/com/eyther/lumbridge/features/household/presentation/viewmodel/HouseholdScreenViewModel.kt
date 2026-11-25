package com.eyther.lumbridge.features.household.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.launchWith
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsStreamUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsUseCase
import com.eyther.lumbridge.features.household.presentation.mapper.toHouseholdTextResource
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HouseholdScreenViewModel(
    private val getHouseholdsUseCase: GetHouseholdsUseCase,
    private val getHouseholdsStreamUseCase: GetHouseholdsStreamUseCase,
) : HouseholdScreenViewModelContract() {

    private val mutableState =
        MutableStateFlow(HouseholdState())
            .launchWith(viewModelScope) { state -> loadHouseholds(state = state) }
            // Picks up this app's later changes (such as a new household picture) silently.
            .observeWith(viewModelScope) { state ->
                getHouseholdsStreamUseCase().updateState(state) { result ->
                    when (result) {
                        is AppResult.Success -> copy(items = result.value)
                        is AppResult.Failure -> this
                    }
                }
            }

    override val state = mutableState.asStateFlow()

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null)
        }
    }

    override fun onRefresh() {
        if (state.value.loading || state.value.refreshing) return
        mutableState.update { oldState -> oldState.copy(refreshing = true, error = null) }
        launch { loadHouseholds(state = mutableState) }
    }

    private suspend fun loadHouseholds(state: MutableStateFlow<HouseholdState>) {
        val result =
            if (state.value.loading || state.value.refreshing) {
                delayAtLeast { getHouseholdsUseCase() }
            } else getHouseholdsUseCase()
        state.update { oldState ->
            when (result) {
                is AppResult.Success ->
                    oldState.copy(
                        items = result.value,
                        loading = false,
                        refreshing = false,
                        error = null,
                    )
                is AppResult.Failure ->
                    oldState.copy(
                        loading = false,
                        refreshing = false,
                        error = result.error.toHouseholdTextResource(),
                    )
            }
        }
    }
}
