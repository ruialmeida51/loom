package com.eyther.lumbridge.features.expenses.domain.model

data class RecurringExpenseLedgerDomain(
    val account: ExpenseAccountDomain,
    val rules: List<RecurringExpenseDomain>,
)
