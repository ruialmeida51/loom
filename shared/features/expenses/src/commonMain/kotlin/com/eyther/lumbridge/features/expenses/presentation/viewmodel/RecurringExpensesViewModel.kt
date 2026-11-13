package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.*
import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.features.expenses.domain.model.*
import com.eyther.lumbridge.features.expenses.domain.usecase.*
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.*
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class RecurringExpensesViewModel(
    observe: ObserveRecurringExpensesUseCase,
    private val controlSchedule: ControlRecurringExpenseUseCase,
    getMembers: GetMembersUseCase,
    context: HouseholdContextDomain,
) : RecurringExpensesViewModelContract() {
    private val mutableState = MutableStateFlow(RecurringExpensesState())
    private val refresh = MutableStateFlow(0)
    override val state = mutableState.asStateFlow()

    init {
        launch {
            when (val result = getMembers(context.id)) {
                is AppResult.Success ->
                    mutableState.update {
                        it.copy(
                            isOwner =
                                result.value.any { member ->
                                    member.id == context.userId && member.role == "owner"
                                },
                            payerNames =
                                result.value.associate { member -> member.id to member.name },
                        )
                    }
                is AppResult.Failure -> mutableState.update { it.copy(error = result.message) }
            }
        }
        mutableState.observeWith(viewModelScope) {
            refresh
                .flatMapLatest {
                    observe().catch { error ->
                        mutableState.update {
                            it.copy(
                                error =
                                    error.message
                                        ?: "expenses.message_couldn_t_load_recurring_expenses",
                            )
                        }
                    }
                }
                .onEach { rules ->
                    mutableState.update {
                        it.copy(
                            rules =
                                rules.sortedWith(
                                    compareBy<RecurringExpenseDomain> { it.cancelled }
                                        .thenBy { it.nextOn },
                                ),
                        )
                    }
                }
        }
    }

    override fun control(rule: RecurringExpenseDomain, action: RecurringExpenseActionDomain) {
        if (state.value.busy) return
        mutableState.update { it.copy(busy = true, error = null) }
        launch {
            when (val result = controlSchedule(rule, action)) {
                is AppResult.Success ->
                    mutableState.update { old ->
                        old.copy(
                            busy = false,
                            rules = old.rules.map { if (it.id == rule.id) result.value else it },
                        )
                    }
                is AppResult.Failure ->
                    mutableState.update { it.copy(busy = false, error = result.message) }
            }
        }
    }

    override fun dismiss() {
        mutableState.update { it.copy(error = null) }
    }

    override fun retry() {
        dismiss()
        refresh.update { it + 1 }
    }
}
