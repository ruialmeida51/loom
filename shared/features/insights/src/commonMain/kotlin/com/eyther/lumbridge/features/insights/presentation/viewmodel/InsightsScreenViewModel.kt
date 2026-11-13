package com.eyther.lumbridge.features.insights.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.*
import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.expenses.domain.usecase.ObserveExpensesUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.ObserveSettlementsUseCase
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.insights.domain.model.MonthlyStatementDomain
import com.eyther.lumbridge.features.insights.domain.usecase.SaveStatementUseCase
import com.eyther.lumbridge.features.insights.presentation.viewmodel.contracts.InsightsScreenViewModelContract
import com.eyther.lumbridge.features.insights.presentation.viewmodel.contracts.InsightsState
import kotlinx.coroutines.flow.*

class InsightsScreenViewModel(
    observeExpenses: ObserveExpensesUseCase,
    observeSettlements: ObserveSettlementsUseCase,
    private val saveStatementUseCase: SaveStatementUseCase,
    private val context: HouseholdContextDomain,
) : InsightsScreenViewModelContract() {
    private val month = MutableStateFlow(todayIso(context.timeZone).take(7))
    private val mutableState = MutableStateFlow(InsightsState())
    override val state = mutableState.asStateFlow()

    init {
        mutableState.observeWith(viewModelScope) {
            combine(observeExpenses(), observeSettlements(), month) { expenses, settlements, month
                    ->
                    MonthlyStatementDomain.build(month, context.userId, expenses, settlements)
                }
                .catch { error -> mutableState.update { it.copy(error = error.message) } }
                .onEach { statement ->
                    mutableState.update { it.copy(statement = statement, month = statement.month) }
                }
        }
    }

    override fun previous() {
        changeMonth(-1)
    }

    override fun next() {
        changeMonth(1)
    }

    private fun changeMonth(offset: Int) {
        val parts = month.value.split('-')
        val index = parts[0].toInt() * 12 + parts[1].toInt() - 1 + offset
        month.value = "${index/12}-${(index%12+1).toString().padStart(2,'0')}"
    }

    override fun dismiss() {
        mutableState.update { it.copy(error = null, message = null) }
    }

    override fun save() {
        if (mutableState.value.busy) return
        val month = mutableState.value.month
        mutableState.update { it.copy(busy = true, error = null) }
        launch {
            when (val result = saveStatementUseCase(month)) {
                is AppResult.Success ->
                    mutableState.update {
                        it.copy(
                            busy = false,
                            message =
                                "insights.message_statement_saved_later_changes_create_a_new_revision",
                        )
                    }
                is AppResult.Failure ->
                    mutableState.update { it.copy(busy = false, error = result.message) }
            }
        }
    }
}
