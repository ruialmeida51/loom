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

    /** Largest remainders allocate every minor unit, with stable member IDs breaking ties. */
    fun weighted(amountMinor: Long, weights: Map<String, Long>): Map<String, Long> {
        requireDomain(amountMinor in 1..MoneyDomain.MAX_MINOR_UNITS) {
            ExpenseError.InvalidAmount()
        }
        requireDomain(
            weights.size in 1..100 &&
                weights.values.all { it in 0..10_000 } &&
                weights.values.any { it > 0 },
        ) {
            ExpenseError.InvalidShare()
        }
        val totalWeight = weights.values.sum()
        val shares =
            weights.mapValues { (_, weight) -> amountMinor * weight / totalWeight }.toMutableMap()
        val remainder = amountMinor - shares.values.sum()
        weights.keys
            .sortedWith(
                compareByDescending<String> { amountMinor * weights.getValue(it) % totalWeight }
                    .thenBy { it },
            )
            .take(remainder.toInt())
            .forEach { id -> shares[id] = shares.getValue(id) + 1 }
        return shares
    }

    fun resolve(amountMinor: Long, selection: ExpenseSplitSelectionDomain): Map<String, Long> {
        requireDomain(selection.participants.isNotEmpty()) { ExpenseError.NoParticipants() }
        val selectedValues = selection.participants.associateWith { selection.values[it] ?: 0L }
        return when (selection.method) {
            ExpenseSplitMethodDomain.EQUAL ->
                equal(amountMinor = amountMinor, memberIds = selection.participants.toList())
            ExpenseSplitMethodDomain.SHARES ->
                weighted(amountMinor = amountMinor, weights = selectedValues)
            ExpenseSplitMethodDomain.AMOUNTS ->
                selectedValues.also { validate(amountMinor = amountMinor, shares = it) }
            ExpenseSplitMethodDomain.PERCENTAGES -> {
                requireDomain(selectedValues.values.all { it in 0..100 }) {
                    ExpenseError.InvalidPercentages()
                }
                percentages(
                    amountMinor = amountMinor,
                    percentages = selectedValues.mapValues { it.value.toInt() },
                )
            }
        }
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
