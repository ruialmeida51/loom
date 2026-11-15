package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

data class ExpenseFormState(
    val userId: String,
    val account: ExpenseAccountDomain? = null,
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
    val shares: Map<String, Long>? =
        (safeCall {
                ExpenseSplitDomain.resolve(
                    amountMinor = MoneyDomain.parse(text = amount, currency = currency).minorUnits,
                    selection = split,
                )
            }
                as? AppResult.Success)
            ?.value
    val memberNames: Map<String, String> = members.associate { it.id to it.name }
}
