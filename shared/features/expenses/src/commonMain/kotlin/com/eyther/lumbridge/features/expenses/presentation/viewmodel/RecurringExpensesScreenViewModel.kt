package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayFirstAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsActionDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain
import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseActionDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.ControlRecurringExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.DeleteRecurringExpenseUseCase
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class RecurringExpensesScreenViewModel(
    private val getRecurringExpensesStreamUseCase: GetRecurringExpensesStreamUseCase,
    private val controlRecurringExpenseUseCase: ControlRecurringExpenseUseCase,
    private val getMembersStreamUseCase: GetMembersStreamUseCase,
    private val deleteRecurringExpenseUseCase: DeleteRecurringExpenseUseCase,
    private val context: HouseholdContextDomain,
    private val trackAnalyticsEventUseCase: TrackAnalyticsEventUseCase,
) : RecurringExpensesScreenViewModelContract() {

    private val refreshRequests = MutableStateFlow(0)

    private val mutableState =
        MutableStateFlow(RecurringExpensesState(userId = context.userId))
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest { requestVersion ->
                        getRecurringExpensesStreamUseCase()
                            .delayFirstAtLeast(
                                enabled = state.value.loading || state.value.refreshing,
                            )
                            .filter { requestVersion == refreshRequests.value && !state.value.busy }
                    }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(
                                    account = result.value.account,
                                    loading = false,
                                    refreshing = false,
                                    rules =
                                        result.value.rules.sortedWith(
                                            compareBy<RecurringExpenseDomain> { it.cancelled }
                                                .thenBy { it.nextOn },
                                        ),
                                    loadError = null,
                                )

                            is AppResult.Failure ->
                                copy(
                                    loadError = result.error.toExpensesTextResource(),
                                    loading = false,
                                    refreshing = false,
                                )
                        }
                    }
            }
            .observeWith(viewModelScope) { state ->
                getMembersStreamUseCase(householdId = context.id, includeInactive = true)
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(
                                    role =
                                        result.value.firstOrNull { it.id == context.userId }?.role,
                                    payerNames = result.value.associate { it.id to it.name },
                                )
                            is AppResult.Failure ->
                                copy(error = result.error.toHouseholdTextResource())
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

            refreshRequests.update { oldVersion -> oldVersion + 1 }
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            busy = false,
                            deletion = null,
                            rules =
                                oldState.rules.map { schedule ->
                                    if (schedule.id == rule.id) result.value else schedule
                                },
                        )

                    is AppResult.Failure ->
                        oldState.copy(
                            busy = false,
                            deletion = null,
                            error = result.error.toExpensesTextResource(),
                        )
                }
            }
        }
    }

    override fun onDeleteRequested(rule: RecurringExpenseDomain) {
        if (
            state.value.busy ||
                !rule.canDelete(userId = context.userId, isOwner = state.value.isOwner)
        )
            return
        mutableState.update { oldState -> oldState.copy(deletion = rule) }
    }

    override fun onDeleteDismissed() {
        if (!state.value.busy) mutableState.update { oldState -> oldState.copy(deletion = null) }
    }

    override fun onDeleteConfirmed() {
        val rule = state.value.deletion ?: return
        if (state.value.busy) return
        mutableState.update { oldState -> oldState.copy(busy = true, error = null) }
        launch {
            val result = deleteRecurringExpenseUseCase(rule = rule)
            refreshRequests.update { oldVersion -> oldVersion + 1 }
            when (result) {
                is AppResult.Success ->
                    mutableState.update { oldState ->
                        oldState.copy(
                            busy = false,
                            deletion = null,
                            rules = oldState.rules.filterNot { it.id == rule.id },
                        )
                    }
                is AppResult.Failure ->
                    mutableState.update { oldState ->
                        oldState.copy(busy = false, error = result.error.toExpensesTextResource())
                    }
            }
        }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null, loadError = null)
        }
    }

    override fun onContentChanged() {
        if (state.value.loading || state.value.refreshing || state.value.busy) return
        refreshRequests.update { oldVersion -> oldVersion + 1 }
    }

    override fun onRetry() {
        if (state.value.refreshing) return
        mutableState.update { oldState -> oldState.copy(refreshing = true) }
        onDismissError()
        refreshRequests.update { oldVersion -> oldVersion + 1 }
    }
}
