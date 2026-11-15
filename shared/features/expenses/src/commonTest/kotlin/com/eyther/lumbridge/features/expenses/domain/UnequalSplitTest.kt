package com.eyther.lumbridge.features.expenses.domain

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.ResolveExpenseSplitUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class UnequalSplitTest {
    @Test
    fun twoSharesAndOneShareAllocateTheLastCentDeterministically() {
        assertEquals(
            mapOf("alice" to 67L, "bob" to 33L),
            ExpenseSplitDomain.weighted(
                amountMinor = 100,
                weights = mapOf("alice" to 2, "bob" to 1),
            ),
        )
        assertEquals(
            mapOf("alice" to 1L, "bob" to 0L, "carol" to 0L),
            ExpenseSplitDomain.weighted(
                amountMinor = 1,
                weights = linkedMapOf("carol" to 1, "bob" to 1, "alice" to 1),
            ),
        )
    }

    @Test
    fun participantInputOrderDoesNotChangeRounding() {
        val weights = mapOf("alice" to 1L, "bob" to 1L, "carol" to 1L)
        val expected = ExpenseSplitDomain.weighted(amountMinor = 100, weights = weights)
        assertEquals(
            expected,
            ExpenseSplitDomain.weighted(
                amountMinor = 100,
                weights = weights.toList().reversed().toMap(),
            ),
        )
        assertEquals(100L, expected.values.sum())
    }

    @Test
    fun maximumSupportedAmountsAndSharesDoNotOverflow() {
        val shares =
            ExpenseSplitDomain.weighted(
                amountMinor = MoneyDomain.MAX_MINOR_UNITS,
                weights = (1..100).associate { "person-$it" to 10_000L },
            )
        assertEquals(MoneyDomain.MAX_MINOR_UNITS, shares.values.sum())
        assertEquals(100, shares.size)
    }

    @Test
    fun invalidShareWeightsAreDomainFailures() {
        for (weights in
            listOf(
                emptyMap(),
                mapOf("alice" to 0L),
                mapOf("alice" to -1L),
                mapOf("alice" to 10_001L),
            )) {
            assertFailsWith<ExpenseError.InvalidShare> {
                ExpenseSplitDomain.weighted(amountMinor = 100, weights = weights)
            }
        }
    }

    @Test
    fun exactAmountsUseTheAccountsCurrencyAndMustMatchTheTotal() {
        val resolve = ResolveExpenseSplitUseCase()
        val selected = setOf("alice", "bob")
        val result =
            assertIs<AppResult.Success<*>>(
                resolve(
                    amountMinor = 1234,
                    currency = "GBP",
                    method = ExpenseSplitMethodDomain.AMOUNTS,
                    participants = selected,
                    custom = mapOf("alice" to "10,00", "bob" to "2.34"),
                ),
            )
        val selection = assertIs<ExpenseSplitSelectionDomain>(result.value)
        assertEquals(
            mapOf("alice" to 1000L, "bob" to 234L),
            ExpenseSplitDomain.resolve(amountMinor = 1234, selection = selection),
        )
        val failure =
            assertIs<AppResult.Failure>(
                resolve(
                    amountMinor = 1234,
                    currency = "GBP",
                    method = ExpenseSplitMethodDomain.AMOUNTS,
                    participants = selected,
                    custom = mapOf("alice" to "10.00", "bob" to "3.00"),
                ),
            )
        assertIs<ExpenseError.IncorrectSplitTotal>(failure.error)
    }

    @Test
    fun percentageInputsRemainTypedAndPreserveEveryCent() {
        val result =
            ResolveExpenseSplitUseCase()(
                amountMinor = 101,
                currency = "EUR",
                method = ExpenseSplitMethodDomain.PERCENTAGES,
                participants = setOf("alice", "bob"),
                custom = mapOf("alice" to "60", "bob" to "40"),
            )
        val selection = assertIs<AppResult.Success<ExpenseSplitSelectionDomain>>(result).value
        assertEquals(
            mapOf("alice" to 61L, "bob" to 40L),
            ExpenseSplitDomain.resolve(amountMinor = 101, selection = selection),
        )
        val invalid =
            ResolveExpenseSplitUseCase()(
                amountMinor = 101,
                currency = "EUR",
                method = ExpenseSplitMethodDomain.PERCENTAGES,
                participants = setOf("alice", "bob"),
                custom = mapOf("alice" to "60", "bob" to "60"),
            )
        assertIs<ExpenseError.InvalidPercentages>(assertIs<AppResult.Failure>(invalid).error)
    }
}
