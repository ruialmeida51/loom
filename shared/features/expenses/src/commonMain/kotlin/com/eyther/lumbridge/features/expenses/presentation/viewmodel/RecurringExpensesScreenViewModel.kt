package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsActionDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain
import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseActionDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.ControlRecurringExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetRecurringExpensesStreamUseCase
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpensesScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpensesState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersStreamUseCase
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
    private val getMembersStreamUseCase: GetMembersStreamUseCase,
    private val context: HouseholdContextDomain,
    private val trackAnalyticsEventUseCase: TrackAnalyticsEventUseCase,
) : RecurringExpensesScreenViewModelContract() {

    private val refreshRequests = MutableStateFlow(0)

    private val mutableState =
        MutableStateFlow(RecurringExpensesState(userId = context.userId))
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest {
                        getRecurringExpensesStreamUseCase()
                    }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(
                                    account = result.value.account,
                                    rules =
                                        result.value.rules.sortedWith(
                                            compareBy<RecurringExpenseDomain> { it.cancelled }
                                                .thenBy { it.nextOn },
                                        ),
                                    loadError = null,
                                )

                            is AppResult.Failure ->
                                copy(loadError = result.error.toExpensesTextResource())
                        }
                    }
            }
            .observeWith(viewModelScope) { state ->
                getMembersStreamUseCase(householdId = context.id).updateState(state) { result ->
                    when (result) {
                        is AppResult.Success ->
                            copy(
                                role = result.value.firstOrNull { it.id == context.userId }?.role,
                                payerNames = result.value.associate { it.id to it.name },
                            )
                        is AppResult.Failure -> copy(error = result.error.toHouseholdTextResource())
                    }
                }
            }

    override val state = mutableState.asStateFlow()

    override fun onControlSchedule(
        rule: RecurringExpenseDomain,
        action: RecurringExpenseActionDomain,
    ) {
        if (state.value.busy) return

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            val result = controlRecurringExpenseUseCase(rule = rule, action = action)
            if (result is AppResult.Success) {
                trackAnalyticsEventUseCase(
                    event =
                        AnalyticsEventDomain.ActionCompleted(
                            action = AnalyticsActionDomain.RECURRING_EXPENSE_UPDATED,
                        ),
                )
            }

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            busy = false,
                            rules =
                                oldState.rules.map { schedule ->
                                    if (schedule.id == rule.id) result.value else schedule
                                },
                        )

                    is AppResult.Failure ->
                        oldState.copy(
                            busy = false,
                            error = result.error.toExpensesTextResource(),
                        )
                }
            }
        }
    }

    override fun onCancelRequested(rule: RecurringExpenseDomain) {
        if (
            state.value.busy ||
                !rule.canManage(userId = context.userId, isOwner = state.value.isOwner)
        )
            return
        mutableState.update { oldState -> oldState.copy(cancellation = rule) }
    }

    override fun onCancelDismissed() {
        if (!state.value.busy)
            mutableState.update { oldState -> oldState.copy(cancellation = null) }
    }

    override fun onCancelConfirmed() {
        val rule = state.value.cancellation ?: return
        mutableState.update { oldState -> oldState.copy(cancellation = null) }
        onControlSchedule(rule = rule, action = RecurringExpenseActionDomain.CANCEL)
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null, loadError = null)
        }
    }

    override fun onRetry() {
        onDismissError()
        refreshRequests.update { oldVersion -> oldVersion + 1 }
    }
}
