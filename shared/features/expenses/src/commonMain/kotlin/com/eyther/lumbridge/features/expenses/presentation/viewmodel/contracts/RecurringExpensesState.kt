package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.presentation.model.RecurringExpenseUi

data class RecurringExpensesState(
    val userId: String = "",
    val cancellation: RecurringExpenseDomain? = null,
    val account: ExpenseAccountDomain? = null,
    val rules: List<RecurringExpenseDomain> = emptyList(),
    val payerNames: Map<String, String> = emptyMap(),
    val role: String? = null,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val loadError: TextResource? = null,
) {

    val showEmpty: Boolean = account != null && rules.isEmpty() && loadError == null
    val currency: String = account?.currency ?: "EUR"
    val displayError: TextResource? = error ?: loadError

    val isOwner: Boolean = role == "owner"
    val rows: List<RecurringExpenseUi> = rules.map { rule ->
        RecurringExpenseUi(
            rule = rule,
            userId = userId,
            role = role,
            payerName = payerNames[rule.paidBy],
        )
    }
}
