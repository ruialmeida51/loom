package com.eyther.lumbridge.features.expenses.domain.model

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError

object ExpenseSplitDomain {
    fun equal(amountMinor: Long, memberIds: List<String>): Map<String, Long> {
        requireDomain(amountMinor in 1..MoneyDomain.MAX_MINOR_UNITS) {
            ExpenseError.InvalidAmount()
        }
        requireDomain(
            memberIds.isNotEmpty() &&
                memberIds.size <= 100 &&
                memberIds.distinct().size == memberIds.size,
        ) {
            ExpenseError.DuplicateParticipants()
        }
        val members = memberIds.sorted()
        val base = amountMinor / members.size
        val remainder = amountMinor % members.size
        return members
            .mapIndexed { index, id -> id to base + if (index < remainder) 1L else 0L }
            .toMap()
    }

    fun percentages(amountMinor: Long, percentages: Map<String, Int>): Map<String, Long> {
        requireDomain(amountMinor in 1..MoneyDomain.MAX_MINOR_UNITS) {
            ExpenseError.InvalidAmount()
        }
        requireDomain(
            percentages.isNotEmpty() &&
                percentages.size <= 100 &&
                percentages.values.all { it in 0..100 } &&
                percentages.values.sum() == 100,
        ) {
            ExpenseError.InvalidPercentages()
        }
        val shares =
            percentages
                .mapValues { (_, percentage) -> amountMinor * percentage / 100 }
                .toMutableMap()
        val remainder = amountMinor - shares.values.sum()
        percentages.keys
            .sortedWith(
                compareByDescending<String> { amountMinor * percentages.getValue(it) % 100 }
                    .thenBy { it },
            )
            .take(remainder.toInt())
            .forEach { shares[it] = shares.getValue(it) + 1 }
        return shares
    }

    fun validate(amountMinor: Long, shares: Map<String, Long>) {
        requireDomain(amountMinor in 1..MoneyDomain.MAX_MINOR_UNITS) {
            ExpenseError.InvalidAmount()
        }
        requireDomain(shares.isNotEmpty() && shares.size <= 100) {
            ExpenseError.NoParticipants()
        }
        requireDomain(shares.values.all { it in 0..amountMinor }) {
            ExpenseError.InvalidShare()
        }
        requireDomain(shares.values.sum() == amountMinor) {
            ExpenseError.IncorrectSplitTotal()
        }
    }
}
