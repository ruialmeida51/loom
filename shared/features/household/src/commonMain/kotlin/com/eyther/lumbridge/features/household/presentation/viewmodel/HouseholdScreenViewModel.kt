package com.eyther.lumbridge.features.household.presentation.viewmodel

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsUseCase
import com.eyther.lumbridge.features.household.presentation.mapper.toHouseholdTextResource
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HouseholdScreenViewModel(
    private val getHouseholdsUseCase: GetHouseholdsUseCase,
) : HouseholdScreenViewModelContract() {

    private val mutableState = MutableStateFlow(HouseholdState())

    override val state = mutableState.asStateFlow()

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null)
        }
    }

    override fun onRefresh() {
        if (state.value.busy) return
        mutableState.update { oldState ->
            oldState.copy(items = emptyList(), busy = true, error = null)
        }
        launch {
            val result = getHouseholdsUseCase()
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(items = result.value, busy = false)
                    is AppResult.Failure -> oldState.copy(busy = false, error = result.error.toHouseholdTextResource())
                }
            }
        }
    }
}
