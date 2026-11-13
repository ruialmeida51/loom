package com.eyther.lumbridge.features.expenses.domain.model

object ExpenseSplitDomain {
    fun equal(amountMinor: Long, memberIds: List<String>): Map<String, Long> {
        require(amountMinor in 1..MoneyDomain.MAX_MINOR_UNITS) {
            "expenses.message_enter_a_valid_amount"
        }
        require(
            memberIds.isNotEmpty() &&
                memberIds.size <= 100 &&
                memberIds.distinct().size == memberIds.size,
        ) {
            "expenses.message_choose_distinct_household_members"
        }
        val members = memberIds.sorted()
        val base = amountMinor / members.size
        val remainder = amountMinor % members.size
        return members
            .mapIndexed { index, id -> id to base + if (index < remainder) 1L else 0L }
            .toMap()
    }

    fun percentages(amountMinor: Long, percentages: Map<String, Int>): Map<String, Long> {
        require(amountMinor in 1..MoneyDomain.MAX_MINOR_UNITS) {
            "expenses.message_enter_a_valid_amount"
        }
        require(
            percentages.isNotEmpty() &&
                percentages.size <= 100 &&
                percentages.values.all { it in 0..100 } &&
                percentages.values.sum() == 100,
        ) {
            "expenses.message_percentages_must_add_up_to_100"
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
        require(amountMinor in 1..MoneyDomain.MAX_MINOR_UNITS) {
            "expenses.message_enter_a_valid_amount"
        }
        require(shares.isNotEmpty() && shares.size <= 100) {
            "expenses.message_choose_who_to_split_with"
        }
        require(shares.values.all { it in 0..amountMinor }) {
            "expenses.message_each_share_must_be_between_zero_and_the_total"
        }
        require(shares.values.sum() == amountMinor) {
            "expenses.message_the_shares_must_add_up_to_the_full_amount"
        }
    }
}
