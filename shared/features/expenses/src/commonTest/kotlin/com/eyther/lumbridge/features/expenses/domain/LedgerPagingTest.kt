package com.eyther.lumbridge.features.expenses.domain

import com.eyther.lumbridge.features.expenses.data.fixtures.LedgerFixtureDataSource
import com.eyther.lumbridge.features.expenses.data.fixtures.LedgerFixtureDataSource.Companion.expense
import com.eyther.lumbridge.features.expenses.data.fixtures.LedgerFixtureDataSource.Companion.settlement
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerCursorDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerPageDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerRecordDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LedgerPagingTest {
    @Test
    fun aPageMergesBothTablesNewestFirstAndBreaksTiesById() {
        val page =
            LedgerPageDomain.merge(
                expenses = listOf(expense("e-b", secondsAgo = 5), expense("e-old", secondsAgo = 30)),
                settlements =
                    listOf(settlement("s-a", secondsAgo = 5), settlement("s-new", secondsAgo = 1)),
                limit = 3,
            )
        assertEquals(listOf("s-new", "e-b", "s-a"), page.records.map { it.id })
        assertEquals(page.records.last().cursor, page.nextCursor, "The leftover proves more exist")
        assertEquals(listOf("e-b"), page.expenses.map { it.id })
        assertEquals(listOf("s-new", "s-a"), page.settlements.map { it.id })
    }

    @Test
    fun anExactlyFullLastPageHasNoNextPage() {
        val page =
            LedgerPageDomain.merge(
                expenses = listOf(expense("one", secondsAgo = 1)),
                settlements = listOf(settlement("two", secondsAgo = 2)),
                limit = 2,
            )
        assertEquals(2, page.records.size)
        assertNull(page.nextCursor)
    }

    @Test
    fun pagingFromEachCursorVisitsEveryRecordOnceInDisplayOrder() {
        // Many records share an instant, across both tables, so only the id orders them.
        val expenses = (1..37).map { expense("e$it", secondsAgo = it / 4) }
        val settlements = (1..23).map { settlement("s$it", secondsAgo = it / 3) }
        val all =
            (expenses.map { LedgerRecordDomain.Purchase(expense = it) } +
                    settlements.map { LedgerRecordDomain.Repayment(settlement = it) })
                .sortedWith(LedgerRecordDomain.NEWEST_FIRST)
        var loaded = page(expenses = expenses, settlements = settlements, after = null)
        while (loaded.nextCursor != null) {
            loaded =
                loaded.append(
                    page(expenses = expenses, settlements = settlements, after = loaded.nextCursor),
                )
        }
        assertEquals(all.map { it.key }, loaded.records.map { it.key })
    }

    @Test
    fun appendingNeverShowsARecordTwice() {
        val first =
            LedgerPageDomain.merge(
                expenses = listOf(expense("a", secondsAgo = 1), expense("b", secondsAgo = 2)),
                settlements = emptyList(),
                limit = 1,
            )
        val overlapping =
            LedgerPageDomain.merge(
                expenses = listOf(expense("a", secondsAgo = 1), expense("c", secondsAgo = 3)),
                settlements = emptyList(),
                limit = 5,
            )
        val joined = first.append(older = overlapping)
        assertEquals(listOf("a", "c"), joined.records.map { it.id })
        assertNull(joined.nextCursor)
        assertNotNull(first.nextCursor)
    }

    @Test
    fun theLedgerListUsesTheSameOrderTheServerPagesIn() {
        val state =
            ExpensesState(
                userId = "you",
                account = LedgerFixtureDataSource.account,
                expenses = listOf(expense("b", secondsAgo = 5), expense("late", secondsAgo = 9)),
                settlements = listOf(settlement("a", secondsAgo = 5)),
            )
        assertEquals(
            listOf("settlement-a", "expense-b", "expense-late"),
            state.history.map { it.key },
        )
        val older = LedgerRecordDomain.Purchase(expense = expense("late", secondsAgo = 9))
        val newer = LedgerRecordDomain.Purchase(expense = expense("b", secondsAgo = 5))
        assertTrue(older.isAfter(cursor = newer.cursor))
        assertFalse(newer.isAfter(cursor = older.cursor))
    }

    /** What the server returns: the newest `PAGE + 1` rows of each table after [after]. */
    private fun page(
        expenses: List<ExpenseDomain>,
        settlements: List<SettlementDomain>,
        after: LedgerCursorDomain?,
    ): LedgerPageDomain =
        LedgerPageDomain.merge(
            expenses =
                expenses
                    .map { LedgerRecordDomain.Purchase(expense = it) }
                    .filter { after == null || it.isAfter(cursor = after) }
                    .sortedWith(LedgerRecordDomain.NEWEST_FIRST)
                    .take(PAGE + 1)
                    .map { it.expense },
            settlements =
                settlements
                    .map { LedgerRecordDomain.Repayment(settlement = it) }
                    .filter { after == null || it.isAfter(cursor = after) }
                    .sortedWith(LedgerRecordDomain.NEWEST_FIRST)
                    .take(PAGE + 1)
                    .map { it.settlement },
            limit = PAGE,
        )

    private companion object {
        const val PAGE = 7
    }
}
