package com.eyther.lumbridge.features.expenses.domain.model

import kotlin.time.Instant

/** One row of an account's ledger: an expense or a repayment, in the order people entered them. */
sealed interface LedgerRecordDomain {
    val id: String

    /** Identifies the record across both tables, which may in principle share an id. */
    val key: String

    /** When the record was entered; a record the server has not stamped sorts last. */
    val createdAt: Instant

    val cursor: LedgerCursorDomain
        get() = LedgerCursorDomain(createdAt = createdAt, id = id)

    /** Whether this record comes after [cursor] in [NEWEST_FIRST] order, on a later page. */
    fun isAfter(cursor: LedgerCursorDomain): Boolean =
        createdAt < cursor.createdAt || (createdAt == cursor.createdAt && id > cursor.id)

    data class Purchase(val expense: ExpenseDomain) : LedgerRecordDomain {
        override val id: String = expense.id
        override val key: String = "expense-$id"
        override val createdAt: Instant = enteredAt(expense.createdAt)
    }

    data class Repayment(val settlement: SettlementDomain) : LedgerRecordDomain {
        override val id: String = settlement.id
        override val key: String = "settlement-$id"
        override val createdAt: Instant = enteredAt(settlement.createdAt)
    }

    companion object {
        /** Newest entry first, whatever date it carries. The server pages in this same order. */
        val NEWEST_FIRST: Comparator<LedgerRecordDomain> =
            compareByDescending<LedgerRecordDomain> { it.createdAt }.thenBy { it.id }

        private fun enteredAt(value: String): Instant =
            Instant.parseOrNull(value) ?: Instant.DISTANT_PAST
    }
}
