package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayFirstAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.presentation.components.snackbar.DEFAULT_ALERT_DURATION
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.account.domain.usecase.GetPaymentHandlesUseCase
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerPageDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.DeleteExpenseAccountUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.DeleteExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetAccountLedgerStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseAccountsStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetRecurringExpensesStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RemoveSettlementUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveExpenseAccountUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SelectExpenseAccountUseCase
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.detail_deleted
import com.eyther.lumbridge.features.expenses.generated.resources.payment_deleted
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.expenses.presentation.model.ExpensesViewUi
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersStreamUseCase
import com.eyther.lumbridge.features.household.presentation.mapper.toHouseholdTextResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class ExpensesScreenViewModel(
    private val getAccountLedgerStreamUseCase: GetAccountLedgerStreamUseCase,
    private val getExpenseAccountsStreamUseCase: GetExpenseAccountsStreamUseCase,
    private val selectExpenseAccountUseCase: SelectExpenseAccountUseCase,
    private val saveExpenseAccountUseCase: SaveExpenseAccountUseCase,
    private val deleteExpenseAccountUseCase: DeleteExpenseAccountUseCase,
    private val removeSettlementUseCase: RemoveSettlementUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val getMembersStreamUseCase: GetMembersStreamUseCase,
    private val getRecurringExpensesStreamUseCase: GetRecurringExpensesStreamUseCase,
    private val getPaymentHandlesUseCase: GetPaymentHandlesUseCase,
    private val context: HouseholdContextDomain,
) : ExpensesScreenViewModelContract() {
    private val refreshRequests = MutableStateFlow(0)
    private val loadMoreRequests =
        MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private var successJob: Job? = null
    private val mutableState =
        MutableStateFlow(ExpensesState(userId = context.userId))
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest {
                        getAccountLedgerStreamUseCase(
                                loadMoreRequests = loadMoreRequests,
                                // Reloading keeps the pages already scrolled through.
                                initialSize =
                                    maxOf(
                                        LedgerPageDomain.PAGE_SIZE,
                                        state.value.expenses.size + state.value.settlements.size,
                                    ),
                            )
                            .delayFirstAtLeast(
                                enabled = state.value.loading || state.value.refreshing,
                            )
                    }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(
                                    account = result.value.account,
                                    expenses = result.value.history.expenses,
                                    settlements = result.value.history.settlements,
                                    balances = result.value.balances,
                                    hasMoreHistory = result.value.history.nextCursor != null,
                                    loadingMoreHistory = result.value.loadingMore,
                                    loadMoreHistoryError =
                                        result.value.loadMoreError?.toExpensesTextResource(),
                                    loading = false,
                                    refreshing = false,
                                    loadError = null,
                                )
                            is AppResult.Failure ->
                                copy(
                                    loading = false,
                                    refreshing = false,
                                    loadingMoreHistory = false,
                                    loadMoreHistoryError = null,
                                    loadError = result.error.toExpensesTextResource(),
                                )
                        }
                    }
            }
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest { getExpenseAccountsStreamUseCase() }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success -> copy(accounts = result.value)
                            is AppResult.Failure ->
                                copy(error = result.error.toExpensesTextResource())
                        }
                    }
            }
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest {
                        getMembersStreamUseCase(householdId = context.id, includeInactive = true)
                    }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success -> copy(members = result.value)
                            is AppResult.Failure ->
                                copy(error = result.error.toHouseholdTextResource())
                        }
                    }
            }
            .observeWith(viewModelScope) { state ->
                // The recurring bills summary is a shortcut; failing to load it hides it quietly.
                refreshRequests
                    .flatMapLatest { getRecurringExpensesStreamUseCase() }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success -> copy(recurring = result.value.rules)
                            is AppResult.Failure -> this
                        }
                    }
            }
    override val state = mutableState.asStateFlow()

    init {
        launch { loadOwnPaymentHandles() }
    }

    private suspend fun loadOwnPaymentHandles() {
        // Optional: shared balances simply leave out how to pay you when this fails.
        val result = getPaymentHandlesUseCase(userIds = listOf(context.userId))
        if (result is AppResult.Success)
            mutableState.update { oldState ->
                oldState.copy(ownPaymentHandles = result.value.firstOrNull())
            }
    }

    override fun onViewChanged(view: ExpensesViewUi) {
        mutableState.update { oldState -> oldState.copy(selectedView = view) }
    }

    override fun onAccountsVisibilityChanged(value: Boolean) {
        if (state.value.busy) return
        mutableState.update { oldState ->
            oldState.copy(showAccounts = value)
        }
    }

    override fun onMemberBalancesVisibilityChanged(value: Boolean) {
        mutableState.update { oldState -> oldState.copy(showMemberBalances = value) }
    }

    override fun onAccountSelected(id: String) {
        if (state.value.busy) return
        if (state.value.account?.id == id) {
            onAccountsVisibilityChanged(false)
            return
        }
        mutableState.update { oldState ->
            oldState.copy(
                account = oldState.accounts.firstOrNull { it.id == id },
                expenses = emptyList(),
                settlements = emptyList(),
                balances = emptyMap(),
                hasMoreHistory = false,
                loadingMoreHistory = false,
                loadMoreHistoryError = null,
                loading = true,
                showAccounts = false,
                error = null,
            )
        }
        launch {
            when (val result = selectExpenseAccountUseCase(id = id)) {
                is AppResult.Success -> {
                    // Selecting the cached ID must also restart a stream that previously failed.
                    onContentChanged()
                }
                is AppResult.Failure ->
                    mutableState.update { oldState ->
                        oldState.copy(
                            loading = false,
                            refreshing = false,
                            error = result.error.toExpensesTextResource(),
                        )
                    }
            }
        }
    }

    override fun onDeleteAccountRequested(account: ExpenseAccountDomain) {
        if (
            state.value.busy ||
                !account.canManage(userId = context.userId, isOwner = state.value.isOwner)
        )
            return
        mutableState.update { oldState -> oldState.copy(deletingAccount = account, error = null) }
    }

    override fun onCancelDeleteAccount() {
        if (state.value.busy) return
        mutableState.update { oldState -> oldState.copy(deletingAccount = null) }
    }

    override fun onConfirmDeleteAccount() {
        val account = state.value.deletingAccount ?: return
        if (state.value.busy) return
        mutableState.update { oldState -> oldState.copy(busy = true, error = null) }
        launch {
            when (val result = deleteExpenseAccountUseCase(account = account)) {
                is AppResult.Success -> {
                    mutableState.update { oldState ->
                        oldState.copy(
                            busy = false,
                            deletingAccount = null,
                            accounts = oldState.accounts.filterNot { it.id == account.id },
                        )
                    }
                    onContentChanged()
                }
                is AppResult.Failure ->
                    mutableState.update { oldState ->
                        oldState.copy(
                            busy = false,
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

    override fun onDeleteExpenseRequested(expense: ExpenseDomain) {
        if (
            state.value.busy ||
                !expense.canManage(userId = context.userId, isOwner = state.value.isOwner)
        )
            return
        mutableState.update { oldState ->
            oldState.copy(
                deletingExpense = expense,
                deletingSettlement = null,
                entryDeleteError = null,
            )
        }
    }

    override fun onDeleteSettlementRequested(settlement: SettlementDomain) {
        if (
            state.value.busy ||
                !settlement.canManage(userId = context.userId, isOwner = state.value.isOwner)
        )
            return
        mutableState.update { oldState ->
            oldState.copy(
                deletingSettlement = settlement,
                deletingExpense = null,
                entryDeleteError = null,
            )
        }
    }

    override fun onEntryDeleteCancelled() {
        if (state.value.busy) return
        mutableState.update { oldState ->
            oldState.copy(
                deletingExpense = null,
                deletingSettlement = null,
                entryDeleteError = null,
            )
        }
    }

    override fun onEntryDeleteConfirmed() {
        if (state.value.busy) return
        val expense = state.value.deletingExpense
        val payment = state.value.deletingSettlement
        if (expense == null && payment == null) return
        mutableState.update { oldState -> oldState.copy(busy = true, entryDeleteError = null) }
        launch {
            val result =
                if (expense != null) deleteExpenseUseCase(expense = expense)
                else removeSettlementUseCase(settlement = requireNotNull(payment))
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            busy = false,
                            deletingExpense = null,
                            deletingSettlement = null,
                            expenses = oldState.expenses.filterNot { it.id == expense?.id },
                            settlements = oldState.settlements.filterNot { it.id == payment?.id },
                        )
                    is AppResult.Failure ->
                        oldState.copy(
                            busy = false,
                            entryDeleteError = result.error.toExpensesTextResource(),
                        )
                }
            }
            if (result is AppResult.Success) {
                showSuccess(
                    TextResource.Resource(
                        if (expense != null) Res.string.detail_deleted
                        else Res.string.payment_deleted,
                    ),
                )
                onContentChanged()
            }
        }
    }

    private fun showSuccess(message: TextResource) {
        successJob?.cancel()
        mutableState.update { oldState -> oldState.copy(successMessage = message) }
        successJob = launch {
            delay(DEFAULT_ALERT_DURATION)
            mutableState.update { oldState -> oldState.copy(successMessage = null) }
        }
    }

    override fun onLoadMoreHistory() {
        // After a failure the reader retries explicitly, rather than every scroll asking again.
        // The flag is set before the request on the main thread, so a second call returns here.
        if (!state.value.historyPagination.canLoadMore) return
        mutableState.update { oldState -> oldState.copy(loadingMoreHistory = true) }
        loadMoreRequests.tryEmit(Unit)
    }

    override fun onRetryLoadMoreHistory() {
        mutableState.update { oldState -> oldState.copy(loadMoreHistoryError = null) }
        onLoadMoreHistory()
    }

    override fun onContentChanged() {
        refreshRequests.update { oldState -> oldState + 1 }
    }

    override fun onRefresh() {
        if (state.value.refreshing) return
        mutableState.update { oldState -> oldState.copy(refreshing = true) }
        refreshRequests.update { oldVersion -> oldVersion + 1 }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null, loadError = null)
        }
    }
}
