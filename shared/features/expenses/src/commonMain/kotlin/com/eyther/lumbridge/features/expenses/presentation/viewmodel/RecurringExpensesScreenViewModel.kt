package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.launchWith
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseActionDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.ControlRecurringExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetRecurringExpensesStreamUseCase
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpensesScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpensesState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
import com.eyther.lumbridge.features.household.presentation.mapper.toHouseholdTextResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class RecurringExpensesScreenViewModel(
    private val getRecurringExpensesStreamUseCase: GetRecurringExpensesStreamUseCase,
    private val controlRecurringExpenseUseCase: ControlRecurringExpenseUseCase,
    private val getMembersUseCase: GetMembersUseCase,
    private val context: HouseholdContextDomain,
) : RecurringExpensesScreenViewModelContract() {

    private val refreshRequests = MutableStateFlow(0)

    private val mutableState = MutableStateFlow(RecurringExpensesState())
        .observeWith(viewModelScope) { state ->
            refreshRequests.flatMapLatest {
                getRecurringExpensesStreamUseCase()
            }.updateState(state) { result ->
                when (result) {
                    is AppResult.Success -> copy(
                        rules = result.value.sortedWith(
                            compareBy<RecurringExpenseDomain> { it.cancelled }.thenBy { it.nextOn },
                        ),
                        error = null,
                    )

                    is AppResult.Failure -> copy(error = result.error.toExpensesTextResource())
                }
            }
        }
        .launchWith(viewModelScope) { state ->
            state.loadMembers()
        }

    override val state = mutableState.asStateFlow()

    override fun onControlSchedule(rule: RecurringExpenseDomain, action: RecurringExpenseActionDomain) {
        if (state.value.busy) return

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            val result = controlRecurringExpenseUseCase(rule = rule, action = action)

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(
                        busy = false,
                        rules = oldState.rules.map { schedule ->
                            if (schedule.id == rule.id) result.value else schedule
                        },
                    )

                    is AppResult.Failure -> oldState.copy(
                        busy = false,
                        error = result.error.toExpensesTextResource(),
                    )
                }
            }
        }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null)
        }
    }

    override fun onRetry() {
        onDismissError()
        refreshRequests.update { oldVersion -> oldVersion + 1 }
    }

    private suspend fun MutableStateFlow<RecurringExpensesState>.loadMembers() {
        val result = getMembersUseCase(context.id)

        update { oldState ->
            when (result) {
                is AppResult.Success -> oldState.copy(
                    role = result.value.firstOrNull { it.id == context.userId }?.role,
                    payerNames = result.value.associate { it.id to it.name },
                )

                is AppResult.Failure -> oldState.copy(error = result.error.toHouseholdTextResource())
            }
        }
    }
}
