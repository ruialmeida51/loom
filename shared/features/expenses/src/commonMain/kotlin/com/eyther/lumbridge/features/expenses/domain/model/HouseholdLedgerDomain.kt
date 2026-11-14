package com.eyther.lumbridge.features.expenses.domain.model

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError

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
            requireDomain(
                settlement.from != settlement.to &&
                    settlement.amountMinor in 1..MoneyDomain.MAX_MINOR_UNITS,
            ) {
                ExpenseError.InvalidSettlement()
            }
            add(settlement.from, settlement.amountMinor)
            add(settlement.to, -settlement.amountMinor)
        }
        return balances.entries.sortedBy { it.key }.associate { it.key to it.value }
    }

    fun suggestions(balances: Map<String, Long>): List<SuggestedSettlementDomain> {
        requireDomain(balances.values.sum() == 0L) {
            ExpenseError.UnbalancedLedger()
        }
        val ordered = balances.entries.sortedBy { it.key }
        val debtors = ordered.filter { it.value < 0 }
        val creditors =
            ordered.filter { it.value > 0 }.associate { it.key to it.value }.toMutableMap()
        val result = mutableListOf<SuggestedSettlementDomain>()
        for ((debtor, originalDebt) in debtors) {
            var debt = -originalDebt
            for (creditor in creditors.keys.toList()) {
                if (debt == 0L) break
                val credit = creditors.getValue(creditor)
                val amount = minOf(debt, credit)
                if (amount > 0) {
                    result += SuggestedSettlementDomain(from = debtor, to = creditor, amountMinor = amount)
                    debt -= amount
                    creditors[creditor] = credit - amount
                }
            }
        }
        return result
    }
}
