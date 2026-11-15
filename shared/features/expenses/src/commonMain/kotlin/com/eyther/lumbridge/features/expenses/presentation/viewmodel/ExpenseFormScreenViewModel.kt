package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.launchWith
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsActionDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain
import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveExpenseUseCase
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseFormScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseFormState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersStreamUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ExpenseFormScreenViewModel(
    private val params: ExpenseFormScreenViewModelContract.Params,
    private val getExpenseFormUseCase: GetExpenseFormUseCase,
    private val saveExpenseUseCase: SaveExpenseUseCase,
    private val getMembersStreamUseCase: GetMembersStreamUseCase,
    private val context: HouseholdContextDomain,
    private val trackAnalyticsEventUseCase: TrackAnalyticsEventUseCase,
) : ExpenseFormScreenViewModelContract() {
    private data class InnerInputState(
        val title: String = "",
        val amount: String = "",
        val paidBy: String = "",
        val occurredOn: String = "",
        val category: String = "Other",
        val recurring: Boolean = false,
        val split: ExpenseSplitSelectionDomain =
            ExpenseSplitSelectionDomain(ExpenseSplitMethodDomain.EQUAL, emptySet()),
    )

    private val inputState = MutableStateFlow(InnerInputState())
    private val expenseId: String = params.expenseId ?: newId()
    private var initialized: Boolean = false
    private val mutableState =
        MutableStateFlow(ExpenseFormState(userId = context.userId))
            .observeWith(viewModelScope) { state ->
                inputState.updateState(state) { input ->
                    copy(
                        title = input.title,
                        amount = input.amount,
                        paidBy = input.paidBy,
                        occurredOn = input.occurredOn,
                        category = input.category,
                        recurring = input.recurring,
                        split = input.split,
                    )
                }
            }
            .observeWith(viewModelScope) { state ->
                getMembersStreamUseCase(householdId = context.id).updateState(state) { result ->
                    if (result is AppResult.Success)
                        copy(
                            members = result.value.filter { it.id in account?.memberIds.orEmpty() },
                        )
                    else this
                }
            }
            .launchWith(viewModelScope) { state -> loadInitialData(state = state) }
    override val state = mutableState.asStateFlow()

    override fun onTitleChanged(value: String) {
        inputState.update { oldState -> oldState.copy(title = value) }
    }

    override fun onAmountChanged(value: String) {
        inputState.update { oldState -> oldState.copy(amount = value) }
    }

    override fun onPayerChanged(value: String) {
        inputState.update { oldState -> oldState.copy(paidBy = value) }
    }

    override fun onDateChanged(value: String) {
        inputState.update { oldState -> oldState.copy(occurredOn = value) }
    }

    override fun onCategoryChanged(value: String) {
        inputState.update { oldState -> oldState.copy(category = value) }
    }

    override fun onRecurringChanged(value: Boolean) {
        if (state.value.expense != null) return
        inputState.update { oldState -> oldState.copy(recurring = value) }
    }

    override fun onDetailsVisibilityChanged(value: Boolean) {
        mutableState.update { oldState -> oldState.copy(showDetails = value) }
    }

    override fun onEditSplit() {
        if (state.value.busy || state.value.loading) return
        val result = safeCall {
            MoneyDomain.parse(text = inputState.value.amount, currency = state.value.currency)
        }
        mutableState.update { oldState ->
            when (result) {
                is AppResult.Success ->
                    oldState.copy(
                        redirectTo = RedirectTo.EditSplit(amountMinor = result.value.minorUnits),
                    )
                is AppResult.Failure -> oldState.copy(error = result.error.toExpensesTextResource())
            }
        }
    }

    override fun onSplitConfirmed(selection: ExpenseSplitSelectionDomain) {
        inputState.update { oldState -> oldState.copy(split = selection) }
    }

    override fun onSave() {
        if (!state.value.canSave) return
        val account = state.value.account ?: return
        val input = inputState.value
        val before = state.value.expense
        mutableState.update { oldState -> oldState.copy(busy = true, error = null) }
        launch {
            val result = safeCall {
                val amount = MoneyDomain.parse(text = input.amount, currency = account.currency)
                val expense =
                    ExpenseDomain(
                        id = expenseId,
                        title = input.title,
                        amountMinor = amount.minorUnits,
                        paidBy = input.paidBy,
                        category = input.category,
                        occurredOn = input.occurredOn,
                        shares =
                            ExpenseSplitDomain.resolve(
                                amountMinor = amount.minorUnits,
                                selection = input.split,
                            ),
                        recurring = input.recurring,
                        accountId = account.id,
                        version = before?.version ?: 0,
                        createdBy = before?.createdBy ?: context.userId,
                        splitMethod = input.split.method,
                        splitValues = input.split.values,
                    )
                saveExpenseUseCase(expense = expense).getOrThrow()
            }
            if (result is AppResult.Success) {
                trackAnalyticsEventUseCase(
                    event =
                        AnalyticsEventDomain.ActionCompleted(
                            action =
                                if (params.expenseId == null) AnalyticsActionDomain.EXPENSE_RECORDED
                                else AnalyticsActionDomain.EXPENSE_UPDATED,
                        ),
                )
            }
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(busy = false, redirectTo = RedirectTo.Saved)
                    is AppResult.Failure ->
                        oldState.copy(busy = false, error = result.error.toExpensesTextResource())
                }
            }
        }
    }

    override fun onRetry() {
        launch { loadInitialData(state = mutableState) }
    }

    override fun onDismissError() {
        mutableState.update { oldState -> oldState.copy(error = null) }
    }

    override fun onNavigationHandled() {
        mutableState.update { oldState -> oldState.copy(redirectTo = null) }
    }

    /** Refreshing identities must not replace an in-progress expense draft. */
    private suspend fun loadInitialData(state: MutableStateFlow<ExpenseFormState>) {
        when (
            val result =
                getExpenseFormUseCase(accountId = params.accountId, expenseId = params.expenseId)
        ) {
            is AppResult.Success -> {
                val data = result.value
                if (!initialized) {
                    val expense = data.expense
                    val split =
                        if (expense == null)
                            ExpenseSplitSelectionDomain(
                                method = ExpenseSplitMethodDomain.EQUAL,
                                participants = data.members.map { it.id }.toSet(),
                            )
                        else
                            ExpenseSplitSelectionDomain(
                                method = expense.splitMethod,
                                participants = expense.shares.keys,
                                values = expense.splitValues.ifEmpty { expense.shares },
                            )
                    inputState.value =
                        InnerInputState(
                            title = expense?.title.orEmpty(),
                            amount =
                                expense
                                    ?.let {
                                        MoneyDomain(it.amountMinor, data.account.currency)
                                            .inputText()
                                    }
                                    .orEmpty(),
                            paidBy = expense?.paidBy ?: context.userId,
                            occurredOn = expense?.occurredOn ?: todayIso(context.timeZone),
                            category = expense?.category ?: "Other",
                            recurring = expense?.recurring ?: false,
                            split = split,
                        )
                    initialized = true
                }
                state.update { oldState ->
                    oldState.copy(
                        account = data.account,
                        members = data.members,
                        expense = if (oldState.expense == null) data.expense else oldState.expense,
                        loading = false,
                        error = null,
                    )
                }
            }
            is AppResult.Failure ->
                state.update { oldState ->
                    oldState.copy(loading = false, error = result.error.toExpensesTextResource())
                }
        }
    }
}
