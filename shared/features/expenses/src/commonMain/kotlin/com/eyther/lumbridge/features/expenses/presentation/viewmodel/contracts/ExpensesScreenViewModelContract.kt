package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.domain.model.SuggestedSettlementDomain
import com.eyther.lumbridge.features.expenses.presentation.model.ExpensesViewUi
import kotlinx.coroutines.flow.StateFlow

abstract class ExpensesScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<ExpensesState>

    abstract fun onViewChanged(view: ExpensesViewUi)

    abstract fun onAccountsVisibilityChanged(value: Boolean)

    abstract fun onAccountSelected(id: String)

    abstract fun onArchiveAccount(account: ExpenseAccountDomain)

    abstract fun onManageAccountsVisibilityChanged(value: Boolean)

    abstract fun onDeleteAccountRequested(account: ExpenseAccountDomain)

    abstract fun onCancelDeleteAccount()

    abstract fun onConfirmDeleteAccount()

    abstract fun onSuggestedRepayment(suggestion: SuggestedSettlementDomain)

    abstract fun onSettlementSheetVisibilityChanged(value: Boolean)

    abstract fun onFromMemberChanged(value: String)

    abstract fun onToMemberChanged(value: String)

    abstract fun onSettlementAmountChanged(value: String)

    abstract fun onRecordSettlement()

    abstract fun onEditSettlement(settlement: SettlementDomain)

    abstract fun onSettlementDateVisibilityChanged(value: Boolean)

    abstract fun onSettlementDateChanged(value: String)

    abstract fun onDeleteExpenseRequested(expense: ExpenseDomain)

    abstract fun onDeleteSettlementRequested(settlement: SettlementDomain)

    abstract fun onEntryDeleteCancelled()

    abstract fun onEntryDeleteConfirmed()

    abstract fun onContentChanged()

    abstract fun onRefresh()

    abstract fun onDismissError()
}
