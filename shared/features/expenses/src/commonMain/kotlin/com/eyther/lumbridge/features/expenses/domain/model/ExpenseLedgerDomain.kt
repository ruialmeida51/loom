package com.eyther.lumbridge.features.expenses.domain.model

data class ExpenseLedgerDomain(
    val account: ExpenseAccountDomain,
    val expenses: List<ExpenseDomain>,
    val settlements: List<SettlementDomain>,
) {

    val balances: Map<String, Long> =
        HouseholdLedgerDomain.balances(
            expenses = expenses,
            settlements = settlements,
        )
}
