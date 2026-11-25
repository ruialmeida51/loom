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
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseReceiptUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.PrepareReceiptPhotoUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveExpenseReceiptUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveExpenseUseCase
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseFormScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseFormState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ExpenseFormScreenViewModel(
    private val getExpenseReceiptUseCase: GetExpenseReceiptUseCase,
    private val prepareReceiptPhotoUseCase: PrepareReceiptPhotoUseCase,
    private val saveExpenseReceiptUseCase: SaveExpenseReceiptUseCase,
    private val params: ExpenseFormScreenViewModelContract.Params,
    private val getExpenseFormUseCase: GetExpenseFormUseCase,
    private val saveExpenseUseCase: SaveExpenseUseCase,
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

    private val inputState =
        MutableStateFlow(
            InnerInputState(
                paidBy = context.userId,
                occurredOn = todayIso(context.timeZone),
                recurring = params.recurring && params.expenseId == null,
            ),
        )
    private var accountId: String = params.accountId
    private val expenseId: String = params.expenseId ?: newId()
    private var receiptAtDraft: ExpenseReceiptDomain? = null
    private var initialized: Boolean = false
    private val mutableState =
        MutableStateFlow(
                ExpenseFormState(
                    userId = context.userId,
                    editing = params.expenseId != null,
                    receipt =
                        ExpenseReceiptDomain(expenseId = expenseId, accountId = params.accountId),
                    receiptLoading = params.expenseId != null,
                    receiptReady = params.expenseId == null,
                ),
            )
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
            .launchWith(viewModelScope) { state -> loadInitialData(state = state) }
    override val state = mutableState.asStateFlow()

    override fun onReceiptPicked(bytes: ByteArray) {
        if (
            state.value.busy ||
                state.value.receiptPending ||
                state.value.receiptLoading ||
                state.value.receiptPreparing
        )
            return
        receiptAtDraft = state.value.receipt
        mutableState.update { oldState ->
            oldState.copy(receiptPreparing = true, receiptError = null)
        }
        launch {
            val result = prepareReceiptPhotoUseCase(bytes = bytes)
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(receiptPhoto = result.value, receiptPreparing = false)
                    is AppResult.Failure ->
                        oldState.copy(
                            receiptPreparing = false,
                            receiptError = result.error.toExpensesTextResource(),
                        )
                }
            }
        }
    }

    override fun onReceiptPickFailed(error: ReceiptError) {
        mutableState.update { oldState ->
            oldState.copy(receiptError = error.toExpensesTextResource())
        }
    }

    override fun onReceiptDiscarded() {
        if (state.value.busy || state.value.receiptPending) return
        receiptAtDraft = null
        mutableState.update { oldState -> oldState.copy(receiptPhoto = null, receiptError = null) }
    }

    override fun onReceiptRetry() {
        if (state.value.receiptPending) {
            onSave()
            return
        }
        if (state.value.receiptLoading || state.value.busy) return
        mutableState.update { oldState ->
            oldState.copy(receiptLoading = true, receiptError = null)
        }
        launch { loadReceipt() }
    }

    override fun onKeepExpense() {
        if (!state.value.receiptPending || state.value.busy) return
        mutableState.update { oldState ->
            oldState.copy(
                receiptPending = false,
                receiptPhoto = null,
                redirectTo = RedirectTo.Saved,
            )
        }
        launch { trackExpenseSaved() }
    }

    private suspend fun loadReceipt() {
        val result = getExpenseReceiptUseCase(expenseId = expenseId, accountId = accountId)
        mutableState.update { oldState ->
            when (result) {
                is AppResult.Success ->
                    oldState.copy(
                        receipt = result.value,
                        receiptLoading = false,
                        receiptReady = true,
                        receiptError = null,
                    )
                is AppResult.Failure ->
                    oldState.copy(
                        receiptLoading = false,
                        receiptError = result.error.toExpensesTextResource(),
                    )
            }
        }
    }

    override fun onTitleChanged(value: String) {
        if (state.value.receiptPending || state.value.busy) return
        inputState.update { oldState -> oldState.copy(title = value) }
    }

    override fun onAmountChanged(value: String) {
        if (state.value.receiptPending || state.value.busy) return
        inputState.update { oldState -> oldState.copy(amount = value) }
    }

    override fun onPayerChanged(value: String) {
        if (state.value.receiptPending || state.value.busy) return
        inputState.update { oldState -> oldState.copy(paidBy = value) }
    }

    override fun onDateChanged(value: String) {
        if (state.value.receiptPending || state.value.busy) return
        inputState.update { oldState -> oldState.copy(occurredOn = value) }
    }

    override fun onCategoryChanged(value: String) {
        if (state.value.receiptPending || state.value.busy) return
        inputState.update { oldState -> oldState.copy(category = value) }
    }

    override fun onRecurringChanged(value: Boolean) {
        if (state.value.receiptPending || state.value.busy) return
        if (state.value.expense != null) return
        inputState.update { oldState -> oldState.copy(recurring = value) }
    }

    override fun onAccountChanged(id: String) {
        if (!state.value.canChooseAccount || id == accountId) return
        mutableState.update { oldState -> oldState.copy(switchingAccount = true, error = null) }
        launch {
            val result = getExpenseFormUseCase(accountId = id, expenseId = null)
            if (result is AppResult.Success) {
                val data = result.value
                accountId = data.account.id
                inputState.update { oldState ->
                    oldState.copy(
                        paidBy =
                            if (data.members.any { it.id == oldState.paidBy }) oldState.paidBy
                            else context.userId,
                        split =
                            ExpenseSplitSelectionDomain(
                                method = ExpenseSplitMethodDomain.EQUAL,
                                participants = data.members.map { it.id }.toSet(),
                            ),
                    )
                }
            }
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            switchingAccount = false,
                            account = result.value.account,
                            members = result.value.members,
                            receipt = oldState.receipt?.copy(accountId = result.value.account.id),
                        )
                    is AppResult.Failure ->
                        oldState.copy(
                            switchingAccount = false,
                            error = result.error.toExpensesTextResource(),
                        )
                }
            }
        }
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
        if (state.value.receiptPending || state.value.busy) return
        inputState.update { oldState -> oldState.copy(split = selection) }
    }

    override fun onSave() {
        if (!state.value.canSave) return
        val account = state.value.account ?: return
        val input = inputState.value
        val before = state.value.expense
        val photo = state.value.receiptPhoto
        val receipt = receiptAtDraft ?: state.value.receipt
        val financialAlreadySaved = state.value.receiptPending
        mutableState.update { oldState -> oldState.copy(busy = true, error = null) }
        launch {
            var savedReceipt: ExpenseReceiptDomain? = null
            val result = safeCall {
                if (!financialAlreadySaved) {
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
                    if (photo != null)
                        mutableState.update { oldState -> oldState.copy(receiptPending = true) }
                }
                if (photo != null && receipt != null)
                    savedReceipt =
                        saveExpenseReceiptUseCase(receipt = receipt, photo = photo).getOrThrow()
            }
            if (result is AppResult.Success) {
                trackExpenseSaved()
            }
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            busy = false,
                            receiptPending = false,
                            receiptPhoto = null,
                            receipt = savedReceipt ?: oldState.receipt,
                            redirectTo = RedirectTo.Saved,
                        )
                    is AppResult.Failure ->
                        oldState.copy(
                            busy = false,
                            error =
                                if (oldState.receiptPending) null
                                else result.error.toExpensesTextResource(),
                            receiptError =
                                if (oldState.receiptPending) result.error.toExpensesTextResource()
                                else oldState.receiptError,
                        )
                }
            }
        }
    }

    private suspend fun trackExpenseSaved() {
        trackAnalyticsEventUseCase(
            event =
                AnalyticsEventDomain.ActionCompleted(
                    action =
                        if (params.expenseId == null) AnalyticsActionDomain.EXPENSE_RECORDED
                        else AnalyticsActionDomain.EXPENSE_UPDATED,
                ),
        )
    }

    override fun onRetry() {
        mutableState.update { oldState ->
            oldState.copy(loading = oldState.account == null, error = null)
        }
        launch { loadInitialData(state = mutableState, refreshReferences = true) }
    }

    override fun onDismissError() {
        mutableState.update { oldState -> oldState.copy(error = null) }
    }

    override fun onNavigationHandled() {
        mutableState.update { oldState -> oldState.copy(redirectTo = null) }
    }

    /** Refreshing identities must not replace an in-progress expense draft. */
    private suspend fun loadInitialData(
        state: MutableStateFlow<ExpenseFormState>,
        refreshReferences: Boolean = false,
    ) {
        when (
            val result =
                getExpenseFormUseCase(
                    accountId = accountId,
                    expenseId = params.expenseId,
                    refreshReferences = refreshReferences,
                )
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
                    inputState.update { oldState ->
                        if (expense == null) oldState.copy(split = split)
                        else
                            InnerInputState(
                                title = expense.title,
                                amount =
                                    MoneyDomain(expense.amountMinor, data.account.currency)
                                        .inputText(),
                                paidBy = expense.paidBy,
                                occurredOn = expense.occurredOn,
                                category = expense.category,
                                recurring = expense.recurring,
                                split = split,
                            )
                    }
                    initialized = true
                }
                state.update { oldState ->
                    oldState.copy(
                        account = data.account,
                        accounts = data.accounts,
                        members = data.members,
                        expense = if (oldState.expense == null) data.expense else oldState.expense,
                        loading = false,
                        error = null,
                    )
                }
                if (params.expenseId != null && !state.value.receiptReady) loadReceipt()
            }
            is AppResult.Failure ->
                state.update { oldState ->
                    oldState.copy(loading = false, error = result.error.toExpensesTextResource())
                }
        }
    }
}
