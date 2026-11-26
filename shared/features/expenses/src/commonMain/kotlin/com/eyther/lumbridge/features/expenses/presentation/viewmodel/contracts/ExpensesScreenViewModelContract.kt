package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.presentation.model.ExpensesViewUi
import kotlinx.coroutines.flow.StateFlow

abstract class ExpensesScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<ExpensesState>

    abstract fun onViewChanged(view: ExpensesViewUi)

    abstract fun onMemberBalancesVisibilityChanged(value: Boolean)

    abstract fun onAccountsVisibilityChanged(value: Boolean)

    abstract fun onAccountSelected(id: String)

    abstract fun onArchiveAccount(account: ExpenseAccountDomain)

    abstract fun onDeleteAccountRequested(account: ExpenseAccountDomain)

    abstract fun onCancelDeleteAccount()

    abstract fun onConfirmDeleteAccount()

    abstract fun onDeleteExpenseRequested(expense: ExpenseDomain)

    abstract fun onDeleteSettlementRequested(settlement: SettlementDomain)

    abstract fun onEntryDeleteCancelled()

    abstract fun onEntryDeleteConfirmed()

    /** The reader scrolled near the end of the loaded ledger. */
    abstract fun onLoadMoreHistory()

    abstract fun onContentChanged()

    abstract fun onRefresh()

    abstract fun onDismissError()
}
