package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.launchWith
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.presentation.components.snackbar.DEFAULT_ALERT_DURATION
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.DeleteExpenseUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseReceiptUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.PrepareReceiptPhotoUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RemoveExpenseReceiptUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveExpenseReceiptUseCase
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_deleted
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_saved
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseDetailsScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseDetailsState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ExpenseDetailsScreenViewModel(
    private val getExpenseReceiptUseCase: GetExpenseReceiptUseCase,
    private val prepareReceiptPhotoUseCase: PrepareReceiptPhotoUseCase,
    private val saveExpenseReceiptUseCase: SaveExpenseReceiptUseCase,
    private val removeExpenseReceiptUseCase: RemoveExpenseReceiptUseCase,
    private val params: ExpenseDetailsScreenViewModelContract.Params,
    private val getExpenseFormUseCase: GetExpenseFormUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    context: HouseholdContextDomain,
) : ExpenseDetailsScreenViewModelContract() {
    private var expenseToDelete: ExpenseDomain? = null
    private var reading: Boolean = false
    private var contentRefreshPending: Boolean = false
    private var receiptReadRevision: Long = 0
    private var receiptAtDraft: ExpenseReceiptDomain? = null
    private var receiptToDelete: ExpenseReceiptDomain? = null
    private var successJob: Job? = null
    private val mutableState =
        MutableStateFlow(ExpenseDetailsState(userId = context.userId, receiptLoading = true))
            .launchWith(viewModelScope) { state ->
                loadDetails(state = state)
            }
    override val state = mutableState.asStateFlow()

    override fun onReceiptPicked(bytes: ByteArray) {
        if (!state.value.canEdit || !state.value.receiptReady || state.value.receiptPreparing)
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
        if (state.value.busy) return
        receiptAtDraft = null
        mutableState.update { oldState -> oldState.copy(receiptPhoto = null, receiptError = null) }
    }

    override fun onReceiptRetry() {
        if (state.value.busy || state.value.receiptLoading) return
        mutableState.update { oldState ->
            oldState.copy(receiptLoading = true, receiptError = null)
        }
        launch { loadReceipt() }
    }

    override fun onReceiptSave() {
        val photo = state.value.receiptPhoto ?: return
        val receipt = receiptAtDraft ?: return
        if (!state.value.canEdit || state.value.receiptPreparing) return
        mutableState.update { oldState ->
            oldState.copy(busy = true, receiptError = null, receiptLoading = false)
        }
        receiptReadRevision++
        launch {
            val result = saveExpenseReceiptUseCase(receipt = receipt, photo = photo)
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(busy = false, receiptPhoto = null, receipt = result.value)
                    is AppResult.Failure ->
                        oldState.copy(
                            busy = false,
                            receiptError = result.error.toExpensesTextResource(),
                        )
                }
            }
            if (result is AppResult.Success)
                showReceiptSuccess(TextResource.Resource(Res.string.receipt_saved))
        }
    }

    override fun onReceiptDeleteRequested() {
        if (!state.value.canEdit || state.value.receipt?.objectPath == null) return
        receiptToDelete = state.value.receipt
        mutableState.update { oldState ->
            oldState.copy(showReceiptDeletion = true, receiptError = null)
        }
    }

    override fun onReceiptDeleteCancelled() {
        if (state.value.busy) return
        receiptToDelete = null
        mutableState.update { oldState ->
            oldState.copy(showReceiptDeletion = false, receiptError = null)
        }
    }

    override fun onReceiptDeleteConfirmed() {
        val receipt = receiptToDelete ?: return
        if (!state.value.showReceiptDeletion || !state.value.canEdit) return
        mutableState.update { oldState ->
            oldState.copy(busy = true, receiptError = null, receiptLoading = false)
        }
        receiptReadRevision++
        launch {
            val result = removeExpenseReceiptUseCase(receipt = receipt)
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            busy = false,
                            receipt = result.value,
                            showReceiptDeletion = false,
                        )
                    is AppResult.Failure ->
                        oldState.copy(
                            busy = false,
                            receiptError = result.error.toExpensesTextResource(),
                        )
                }
            }
            if (result is AppResult.Success)
                showReceiptSuccess(TextResource.Resource(Res.string.receipt_deleted))
        }
    }

    private fun showReceiptSuccess(message: TextResource) {
        successJob?.cancel()
        mutableState.update { oldState -> oldState.copy(successMessage = message) }
        successJob = launch {
            delay(DEFAULT_ALERT_DURATION)
            mutableState.update { oldState -> oldState.copy(successMessage = null) }
        }
    }

    private suspend fun loadReceipt() {
        val revision = ++receiptReadRevision
        val result =
            getExpenseReceiptUseCase(expenseId = params.expenseId, accountId = params.accountId)
        if (revision != receiptReadRevision) return
        mutableState.update { oldState ->
            when (result) {
                is AppResult.Success ->
                    oldState.copy(
                        receipt = result.value,
                        receiptReady = true,
                        receiptLoading = false,
                        receiptError =
                            if (oldState.receiptPhoto == null) null else oldState.receiptError,
                    )
                is AppResult.Failure ->
                    oldState.copy(
                        receiptLoading = false,
                        receiptError = result.error.toExpensesTextResource(),
                    )
            }
        }
    }

    override fun onRefresh() {
        if (
            reading ||
                state.value.loading ||
                state.value.refreshing ||
                state.value.busy ||
                state.value.showDeleteConfirmation ||
                state.value.showReceiptDeletion ||
                state.value.receiptPhoto != null ||
                state.value.receiptPreparing
        )
            return
        mutableState.update { oldState -> oldState.copy(refreshing = true, error = null) }
        launch { loadDetails(state = mutableState) }
    }

    override fun onContentChanged() {
        if (
            state.value.loading ||
                state.value.busy ||
                state.value.deleted ||
                state.value.receiptPhoto != null ||
                state.value.showDeleteConfirmation ||
                state.value.showReceiptDeletion
        )
            return
        if (reading || state.value.refreshing) {
            contentRefreshPending = true
            return
        }
        launch { loadDetails(state = mutableState) }
    }

    override fun onDeleteRequested() {
        if (!state.value.canEdit || state.value.showDeleteConfirmation) return
        expenseToDelete = state.value.expense ?: return
        mutableState.update { oldState ->
            oldState.copy(showDeleteConfirmation = true, deleteError = null)
        }
    }

    override fun onDeleteCancelled() {
        if (state.value.busy) return
        expenseToDelete = null
        mutableState.update { oldState ->
            oldState.copy(showDeleteConfirmation = false, deleteError = null)
        }
    }

    override fun onDeleteConfirmed() {
        if (!state.value.showDeleteConfirmation || state.value.busy) return
        val expense = expenseToDelete ?: return
        mutableState.update { oldState -> oldState.copy(busy = true, deleteError = null) }
        launch {
            val result = deleteExpenseUseCase(expense = expense)
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(busy = false, deleted = true, showDeleteConfirmation = false)
                    is AppResult.Failure ->
                        oldState.copy(
                            busy = false,
                            deleteError = result.error.toExpensesTextResource(),
                        )
                }
            }
        }
    }

    override fun onDismissError() {
        mutableState.update { oldState -> oldState.copy(error = null) }
    }

    private suspend fun loadDetails(state: MutableStateFlow<ExpenseDetailsState>) {
        reading = true
        try {
            when (
                val result =
                    delayAtLeast(
                        timeToWaitInMillis =
                            if (state.value.loading || state.value.refreshing) 300L else 0L,
                    ) {
                        getExpenseFormUseCase(
                            accountId = params.accountId,
                            expenseId = params.expenseId,
                            refreshReferences = true,
                        )
                    }
            ) {
                is AppResult.Success -> {
                    state.update { oldState ->
                        oldState.copy(
                            account = result.value.account,
                            expense = result.value.expense,
                            members = result.value.members,
                            loading = false,
                            refreshing = false,
                            error = null,
                        )
                    }
                    loadReceipt()
                }
                is AppResult.Failure ->
                    state.update { oldState ->
                        oldState.copy(
                            loading = false,
                            refreshing = false,
                            error = result.error.toExpensesTextResource(),
                        )
                    }
            }
        } finally {
            reading = false
            if (contentRefreshPending) {
                contentRefreshPending = false
                onContentChanged()
            }
        }
    }
}
