package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.launchWith
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.usecase.DeleteExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseFormUseCase
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseDetailsScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseDetailsState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ExpenseDetailsScreenViewModel(
    private val params: ExpenseDetailsScreenViewModelContract.Params,
    private val getExpenseFormUseCase: GetExpenseFormUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    context: HouseholdContextDomain,
) : ExpenseDetailsScreenViewModelContract() {
    private val mutableState =
        MutableStateFlow(ExpenseDetailsState(userId = context.userId)).launchWith(viewModelScope) {
            state ->
            loadDetails(state = state)
        }
    override val state = mutableState.asStateFlow()

    override fun onRefresh() {
        if (state.value.loading || state.value.refreshing || state.value.busy) return
        mutableState.update { oldState -> oldState.copy(refreshing = true, error = null) }
        launch { loadDetails(state = mutableState) }
    }

    override fun onDeleteRequested() {
        if (state.value.canEdit)
            mutableState.update { oldState ->
                oldState.copy(showDeleteConfirmation = true, error = null)
            }
    }

    override fun onDeleteCancelled() {
        if (!state.value.busy)
            mutableState.update { oldState -> oldState.copy(showDeleteConfirmation = false) }
    }

    override fun onDeleteConfirmed() {
        if (!state.value.showDeleteConfirmation || !state.value.canEdit) return
        val expense = state.value.expense ?: return
        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }
        launch {
            val result = deleteExpenseUseCase(expense = expense)
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(busy = false, deleted = true)
                    is AppResult.Failure ->
                        oldState.copy(
                            busy = false,
                            showDeleteConfirmation = false,
                            error = result.error.toExpensesTextResource(),
                        )
                }
            }
        }
    }

    override fun onDismissError() {
        mutableState.update { oldState -> oldState.copy(error = null) }
    }

    private suspend fun loadDetails(state: MutableStateFlow<ExpenseDetailsState>) {
        when (
            val result =
                getExpenseFormUseCase(
                    accountId = params.accountId,
                    expenseId = params.expenseId,
                    refreshReferences = true,
                )
        ) {
            is AppResult.Success ->
                state.update { oldState ->
                    oldState.copy(
                        account = result.value.account,
                        expense = result.value.expense,
                        members = result.value.members,
                        loading = false,
                        refreshing = false,
                        error = null,
                    )
                }
            is AppResult.Failure ->
                state.update { oldState ->
                    oldState.copy(
                        loading = false,
                        refreshing = false,
                        error = result.error.toExpensesTextResource(),
                    )
                }
        }
    }
}
