package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.launchWith
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.usecase.GetMemberAccountPositionsUseCase
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.expenses.presentation.model.MemberAccountPositionUi
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.MemberAccountPositionsScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.MemberAccountPositionsState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Expenses owns the financial preview embedded in a household departure confirmation. */
class MemberAccountPositionsScreenViewModel(
    private val params: MemberAccountPositionsScreenViewModelContract.Params,
    private val getMemberAccountPositionsUseCase: GetMemberAccountPositionsUseCase,
) : MemberAccountPositionsScreenViewModelContract() {
    private val mutableState =
        MutableStateFlow(MemberAccountPositionsState()).launchWith(viewModelScope) { state ->
            load(state = state)
        }
    override val state = mutableState.asStateFlow()

    override fun onRetry() {
        if (state.value.loading) return
        mutableState.update { oldState -> oldState.copy(loading = true, error = null) }
        launch { load(state = mutableState) }
    }

    private suspend fun load(state: MutableStateFlow<MemberAccountPositionsState>) {
        when (
            val result = delayAtLeast {
                getMemberAccountPositionsUseCase(memberId = params.memberId)
            }
        ) {
            is AppResult.Success ->
                state.update { oldState ->
                    oldState.copy(
                        loading = false,
                        error = null,
                        positions = result.value.map(::MemberAccountPositionUi).toImmutableList(),
                    )
                }
            is AppResult.Failure ->
                state.update { oldState ->
                    oldState.copy(loading = false, error = result.error.toExpensesTextResource())
                }
        }
    }
}
