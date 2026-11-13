package com.eyther.lumbridge.features.expenses.domain.model

import com.eyther.lumbridge.core.domain.MoneyDomain

object HouseholdLedgerDomain {
    /**
     * A positive balance means the member is owed money. Settlements reduce the corresponding debt.
     */
    fun balances(
        expenses: List<ExpenseDomain>,
        settlements: List<SettlementDomain>,
    ): Map<String, Long> {
        val balances = mutableMapOf<String, Long>()
        fun add(member: String, amount: Long) {
            balances[member] = (balances[member] ?: 0) + amount
        }
        expenses.forEach { expense ->
            ExpenseSplitDomain.validate(expense.amountMinor, expense.shares)
            add(expense.paidBy, expense.amountMinor)
            expense.shares.forEach { (member, share) -> add(member, -share) }
        }
        settlements.forEach { settlement ->
            require(
                settlement.from != settlement.to &&
                    settlement.amountMinor in 1..MoneyDomain.MAX_MINOR_UNITS,
            )
            add(settlement.from, settlement.amountMinor)
            add(settlement.to, -settlement.amountMinor)
        }
        return balances.toSortedMap()
    }

    fun suggestions(balances: Map<String, Long>): List<SuggestedSettlementDomain> {
        require(balances.values.sum() == 0L) {
            "Balances must reconcile before suggesting repayments."
        }
        val debtors = balances.filterValues { it < 0 }.toSortedMap().toMutableMap()
        val creditors = balances.filterValues { it > 0 }.toSortedMap().toMutableMap()
        val result = mutableListOf<SuggestedSettlementDomain>()
        for ((debtor, originalDebt) in debtors) {
            var debt = -originalDebt
            for (creditor in creditors.keys.toList()) {
                if (debt == 0L) break
                val credit = creditors.getValue(creditor)
                val amount = minOf(debt, credit)
                if (amount > 0) {
                    result += SuggestedSettlementDomain(debtor, creditor, amount)
                    debt -= amount
                    creditors[creditor] = credit - amount
                }
            }
        }
        return result
    }
}
