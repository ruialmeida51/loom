package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.domain.model.SuggestedSettlementDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseAccountsStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseLedgerStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RecordSettlementUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveExpenseAccountUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SelectExpenseAccountUseCase
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.expenses.presentation.model.ExpensesViewUi
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersStreamUseCase
import com.eyther.lumbridge.features.household.presentation.mapper.toHouseholdTextResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class ExpensesScreenViewModel(
    private val getExpenseLedgerStreamUseCase: GetExpenseLedgerStreamUseCase,
    private val getExpenseAccountsStreamUseCase: GetExpenseAccountsStreamUseCase,
    private val selectExpenseAccountUseCase: SelectExpenseAccountUseCase,
    private val saveExpenseAccountUseCase: SaveExpenseAccountUseCase,
    private val recordSettlementUseCase: RecordSettlementUseCase,
    private val getMembersStreamUseCase: GetMembersStreamUseCase,
    private val context: HouseholdContextDomain,
) : ExpensesScreenViewModelContract() {
    private data class InnerInputState(
        val from: String = "",
        val to: String = "",
        val amount: String = "",
    )

    private val inputState = MutableStateFlow(InnerInputState())
    private val refreshRequests = MutableStateFlow(0)
    private var settlementId: String = newId()
    private val mutableState =
        MutableStateFlow(ExpensesState(userId = context.userId))
            .observeWith(viewModelScope) { state ->
                inputState.updateState(state) { input ->
                    copy(from = input.from, to = input.to, settleAmount = input.amount)
                }
            }
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest { getExpenseLedgerStreamUseCase() }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(
                                    account = result.value.account,
                                    expenses = result.value.expenses,
                                    settlements = result.value.settlements,
                                    balances = result.value.balances,
                                    loading = false,
                                    loadError = null,
                                )
                            is AppResult.Failure ->
                                copy(
                                    loading = false,
                                    loadError = result.error.toExpensesTextResource(),
                                )
                        }
                    }
            }
            .observeWith(viewModelScope) { state ->
                getExpenseAccountsStreamUseCase().updateState(state) { result ->
                    when (result) {
                        is AppResult.Success -> copy(accounts = result.value)
                        is AppResult.Failure -> copy(error = result.error.toExpensesTextResource())
                    }
                }
            }
            .observeWith(viewModelScope) { state ->
                getMembersStreamUseCase(householdId = context.id).updateState(state) { result ->
                    when (result) {
                        is AppResult.Success -> copy(members = result.value)
                        is AppResult.Failure -> copy(error = result.error.toHouseholdTextResource())
                    }
                }
            }
    override val state = mutableState.asStateFlow()

    override fun onViewChanged(view: ExpensesViewUi) {
        mutableState.update { oldState -> oldState.copy(selectedView = view) }
    }

    override fun onAccountsVisibilityChanged(value: Boolean) {
        if (state.value.busy) return
        mutableState.update { oldState -> oldState.copy(showAccounts = value) }
    }

    override fun onAccountSelected(id: String) {
        if (state.value.busy) return
        if (state.value.account?.id == id) {
            onAccountsVisibilityChanged(false)
            return
        }
        mutableState.update { oldState ->
            oldState.copy(
                account = null,
                expenses = emptyList(),
                settlements = emptyList(),
                balances = emptyMap(),
                loading = true,
                showAccounts = false,
                error = null,
            )
        }
        launch {
            when (val result = selectExpenseAccountUseCase(id = id)) {
                is AppResult.Success -> {
                    // Selecting the cached ID must also restart a stream that previously failed.
                    onRefresh()
                }
                is AppResult.Failure ->
                    mutableState.update { oldState ->
                        oldState.copy(
                            loading = false,
                            error = result.error.toExpensesTextResource(),
                        )
                    }
            }
        }
    }

    override fun onArchiveAccount(account: ExpenseAccountDomain) {
        if (
            state.value.busy ||
                !account.canManage(userId = context.userId, isOwner = state.value.isOwner)
        )
            return
        mutableState.update { oldState -> oldState.copy(busy = true, error = null) }
        launch {
            val result =
                saveExpenseAccountUseCase(account = account.copy(archived = !account.archived))
            mutableState.update { oldState ->
                oldState.copy(
                    busy = false,
                    error = (result as? AppResult.Failure)?.error?.toExpensesTextResource(),
                )
            }
        }
    }

    override fun onSuggestedRepayment(suggestion: SuggestedSettlementDomain) {
        if (
            !state.value.canRecordSettlement ||
                context.userId !in setOf(suggestion.from, suggestion.to)
        )
            return
        settlementId = newId()
        inputState.value =
            InnerInputState(
                from = suggestion.from,
                to = suggestion.to,
                amount = MoneyDomain(suggestion.amountMinor, state.value.currency).inputText(),
            )
        mutableState.update { oldState -> oldState.copy(showSettle = true, formError = null) }
    }

    override fun onSettlementSheetVisibilityChanged(value: Boolean) {
        if (state.value.busy || value && !state.value.canRecordSettlement) return
        if (value) {
            settlementId = newId()
            inputState.value =
                InnerInputState(
                    from = context.userId,
                    to = state.value.accountMembers.first { it.id != context.userId }.id,
                )
        }
        mutableState.update { oldState -> oldState.copy(showSettle = value, formError = null) }
    }

    override fun onFromMemberChanged(value: String) {
        inputState.update { oldState -> oldState.copy(from = value) }
    }

    override fun onToMemberChanged(value: String) {
        inputState.update { oldState -> oldState.copy(to = value) }
    }

    override fun onSettlementAmountChanged(value: String) {
        inputState.update { oldState -> oldState.copy(amount = value) }
    }

    override fun onRecordSettlement() {
        if (state.value.busy) return
        val account = state.value.account ?: return
        val input = inputState.value
        mutableState.update { oldState -> oldState.copy(busy = true, formError = null) }
        launch {
            val result = safeCall {
                val repayment =
                    SettlementDomain(
                        id = settlementId,
                        from = input.from,
                        to = input.to,
                        amountMinor =
                            MoneyDomain.parse(text = input.amount, currency = account.currency)
                                .minorUnits,
                        occurredOn = todayIso(context.timeZone),
                        accountId = account.id,
                    )
                recordSettlementUseCase(settlement = repayment).getOrThrow()
            }
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(busy = false, showSettle = false)
                    is AppResult.Failure ->
                        oldState.copy(
                            busy = false,
                            formError = result.error.toExpensesTextResource(),
                        )
                }
            }
            if (result is AppResult.Success) onRefresh()
        }
    }

    override fun onRefresh() {
        refreshRequests.update { oldVersion -> oldVersion + 1 }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null, loadError = null, formError = null)
        }
    }
}
