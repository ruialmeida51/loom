package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.launchWith
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.today
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsActionDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain
import com.eyther.lumbridge.features.analytics.domain.usecase.TrackAnalyticsEventUseCase
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import com.eyther.lumbridge.features.expenses.domain.model.EnteredAmountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.OriginalAmountDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExchangeRateUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseReceiptUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.PrepareReceiptPhotoUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveExpenseReceiptUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveExpenseUseCase
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseFormScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseFormState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlinx.datetime.LocalDate
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
    private val getExchangeRateUseCase: GetExchangeRateUseCase,
) : ExpenseFormScreenViewModelContract() {
    private data class InnerInputState(
        val title: String = "",
        val amount: String = "",
        val paidBy: String = "",
        val occurredOn: LocalDate? = null,
        val category: String = "Other",
        val recurring: Boolean = false,
        /** The currency the amount is typed in; null means the account's own. */
        val entryCurrency: String? = null,
        val rate: String? = null,
        val rateIsCustom: Boolean = false,
        val split: ExpenseSplitSelectionDomain =
            ExpenseSplitSelectionDomain(ExpenseSplitMethodDomain.EQUAL, emptySet()),
    )

    private val inputState =
        MutableStateFlow(
            InnerInputState(
                paidBy = context.userId,
                occurredOn = today(context.timeZone),
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
                        entryCurrency = input.entryCurrency,
                        rate = input.rate,
                        rateIsCustom = input.rateIsCustom,
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

    override fun onDateChanged(value: LocalDate) {
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
        // Recurring bills repeat in the account's currency, where a rate cannot go stale.
        inputState.update { oldState ->
            oldState.copy(
                recurring = value,
                entryCurrency = if (value) null else oldState.entryCurrency,
                rate = if (value) null else oldState.rate,
            )
        }
    }

    override fun onEntryCurrencyChanged(code: String) {
        if (state.value.receiptPending || state.value.busy) return
        val accountCurrency = state.value.currency
        if (code == accountCurrency) {
            inputState.update { oldState ->
                oldState.copy(entryCurrency = null, rate = null, rateIsCustom = false)
            }
            return
        }
        inputState.update { oldState ->
            oldState.copy(entryCurrency = code, rate = null, rateIsCustom = false, recurring = false)
        }
        loadRate(from = code, to = accountCurrency)
    }

    override fun onRateChanged(value: String) {
        val rate = OriginalAmountDomain.normalizeRate(value) ?: return
        inputState.update { oldState -> oldState.copy(rate = rate, rateIsCustom = true) }
        mutableState.update { oldState -> oldState.copy(rateError = null) }
    }

    private fun loadRate(from: String, to: String) {
        mutableState.update { oldState -> oldState.copy(rateLoading = true, rateError = null) }
        launch {
            val result = getExchangeRateUseCase(from = from, to = to)
            if (result is AppResult.Success)
                inputState.update { oldState ->
                    // A rate the person typed, or a newer currency choice, wins over a late reply.
                    if (oldState.entryCurrency != from || oldState.rateIsCustom) oldState
                    else oldState.copy(rate = result.value)
                }
            mutableState.update { oldState ->
                oldState.copy(
                    rateLoading = false,
                    rateError =
                        (result as? AppResult.Failure)?.error?.toExpensesTextResource(),
                )
            }
        }
    }

    override fun onAccountChanged(id: String) {
        if (!state.value.canChooseAccount || id == accountId) return
        mutableState.update { oldState -> oldState.copy(switchingAccount = true, error = null) }
        launch {
            val result = getExpenseFormUseCase(accountId = id, expenseId = null)
            if (result is AppResult.Success) {
                val data = result.value
                accountId = data.account.id
                // A foreign amount converts into the new account's currency instead.
                val entryCurrency = inputState.value.entryCurrency
                if (entryCurrency == data.account.currency)
                    inputState.update { oldState -> oldState.copy(entryCurrency = null, rate = null) }
                else if (entryCurrency != null) {
                    inputState.update { oldState -> oldState.copy(rate = null, rateIsCustom = false) }
                    loadRate(from = entryCurrency, to = data.account.currency)
                }
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

    override fun onSplitParticipantToggled(id: String) {
        if (state.value.receiptPending || state.value.busy) return
        inputState.update { oldState ->
            val split = oldState.split
            if (split.method != ExpenseSplitMethodDomain.EQUAL) return@update oldState
            val participants =
                if (id in split.participants) split.participants - id else split.participants + id
            // Someone always carries the cost.
            if (participants.isEmpty()) oldState
            else oldState.copy(split = split.copy(participants = participants))
        }
    }

    override fun onEditSplit() {
        if (state.value.busy || state.value.loading) return
        val result = safeCall { state.value.enteredAmount.accountMinor() }
        mutableState.update { oldState ->
            when (result) {
                is AppResult.Success ->
                    oldState.copy(redirectTo = RedirectTo.EditSplit(amountMinor = result.value))
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
                    val entered =
                        EnteredAmountDomain(
                            text = input.amount,
                            currency = input.entryCurrency ?: account.currency,
                            accountCurrency = account.currency,
                            rate = input.rate,
                        )
                    val amountMinor = entered.accountMinor()
                    val expense =
                        ExpenseDomain(
                            id = expenseId,
                            title = input.title,
                            amountMinor = amountMinor,
                            paidBy = input.paidBy,
                            category = input.category,
                            occurredOn = input.occurredOn ?: throw ExpenseError.InvalidPaymentDate(),
                            shares =
                                ExpenseSplitDomain.resolve(
                                    amountMinor = amountMinor,
                                    selection = input.split,
                                ),
                            recurring = input.recurring,
                            original = entered.original(),
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
                                    expense.original?.let {
                                        MoneyDomain(it.amountMinor, it.currency).inputText()
                                    }
                                        ?: MoneyDomain(expense.amountMinor, data.account.currency)
                                            .inputText(),
                                entryCurrency = expense.original?.currency,
                                rate = expense.original?.rate,
                                rateIsCustom = expense.original != null,
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
