package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

data class ExpenseFormState(
    val userId: String,
    val account: ExpenseAccountDomain? = null,
    val editing: Boolean = false,
    val expense: ExpenseDomain? = null,
    val members: List<MemberDomain> = emptyList(),
    val title: String = "",
    val amount: String = "",
    val occurredOn: String = "",
    val paidBy: String = "",
    val category: String = "Other",
    val recurring: Boolean = false,
    val split: ExpenseSplitSelectionDomain =
        ExpenseSplitSelectionDomain(ExpenseSplitMethodDomain.EQUAL, emptySet()),
    val showDetails: Boolean = false,
    val loading: Boolean = true,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val redirectTo: ExpenseFormScreenViewModelContract.RedirectTo? = null,
) {
    val currency: String = account?.currency ?: "EUR"
    val canSave: Boolean =
        !loading &&
            !busy &&
            title.isNotBlank() &&
            amount.isNotBlank() &&
            account?.canAddExpense(userId = userId) == true
}
