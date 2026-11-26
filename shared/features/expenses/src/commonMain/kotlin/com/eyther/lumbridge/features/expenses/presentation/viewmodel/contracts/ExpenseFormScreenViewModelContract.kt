package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import kotlinx.coroutines.flow.StateFlow

abstract class ExpenseFormScreenViewModelContract : LoomViewModel() {
    data class Params(
        val accountId: String,
        val expenseId: String? = null,
        /** Opens a new expense with "Repeat every month" already on. */
        val recurring: Boolean = false,
    )

    @Stable
    sealed interface RedirectTo {
        @Stable data object Saved : RedirectTo

        @Stable data class EditSplit(val amountMinor: Long) : RedirectTo
    }

    abstract val state: StateFlow<ExpenseFormState>

    abstract fun onReceiptPicked(bytes: ByteArray)

    abstract fun onReceiptPickFailed(error: ReceiptError)

    abstract fun onReceiptDiscarded()

    abstract fun onReceiptRetry()

    abstract fun onKeepExpense()

    abstract fun onTitleChanged(value: String)

    abstract fun onAmountChanged(value: String)

    abstract fun onPayerChanged(value: String)

    abstract fun onDateChanged(value: String)

    abstract fun onCategoryChanged(value: String)

    abstract fun onRecurringChanged(value: Boolean)

    /** Moves a new expense to another account; its split resets to that account's people. */
    abstract fun onAccountChanged(id: String)

    abstract fun onEditSplit()

    /** Adds or removes someone from an equal split, straight from the form. */
    abstract fun onSplitParticipantToggled(id: String)

    abstract fun onSplitConfirmed(selection: ExpenseSplitSelectionDomain)

    abstract fun onSave()

    abstract fun onRetry()

    abstract fun onDismissError()

    abstract fun onNavigationHandled()
}
