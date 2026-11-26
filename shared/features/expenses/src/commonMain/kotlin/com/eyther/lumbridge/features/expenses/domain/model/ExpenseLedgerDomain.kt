package com.eyther.lumbridge.features.expenses.domain.model

/**
 * An account's full history, for monthly statements that need every record up to a month. The
 * Money overview pages through [LedgerPageDomain] and reads balances from the server instead.
 */
data class ExpenseLedgerDomain(
    val account: ExpenseAccountDomain,
    val expenses: List<ExpenseDomain>,
    val settlements: List<SettlementDomain>,
)
