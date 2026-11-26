package com.eyther.lumbridge.features.expenses.domain.model

/**
 * Ledger records, newest first, and where the next page starts. [nextCursor] is null once the
 * oldest record is loaded, including when the last page is exactly full.
 */
data class LedgerPageDomain(
    val records: List<LedgerRecordDomain> = emptyList(),
    val nextCursor: LedgerCursorDomain? = null,
) {
    val expenses: List<ExpenseDomain> =
        records.filterIsInstance<LedgerRecordDomain.Purchase>().map { it.expense }

    val settlements: List<SettlementDomain> =
        records.filterIsInstance<LedgerRecordDomain.Repayment>().map { it.settlement }

    /** Appends the [older] page loaded from [nextCursor]; a record never shows twice. */
    fun append(older: LedgerPageDomain): LedgerPageDomain =
        LedgerPageDomain(
            records = (records + older.records).distinctBy { it.key },
            nextCursor = older.nextCursor,
        )

    companion object {
        /** Records the ledger shows per page while scrolling. */
        const val PAGE_SIZE: Int = 50

        /** Records fetched per request when a reload restores a longer list. */
        const val RELOAD_CHUNK: Int = 200

        /**
         * Merges up to `limit + 1` newest candidates from each table, all after the same cursor,
         * into one page of [limit]. A leftover candidate proves another page exists; candidates
         * that do not fit are fetched again after the new page's cursor.
         */
        fun merge(
            expenses: List<ExpenseDomain>,
            settlements: List<SettlementDomain>,
            limit: Int,
        ): LedgerPageDomain {
            val candidates =
                (expenses.map { LedgerRecordDomain.Purchase(expense = it) } +
                        settlements.map { LedgerRecordDomain.Repayment(settlement = it) })
                    .sortedWith(LedgerRecordDomain.NEWEST_FIRST)
            val records = candidates.take(limit)
            return LedgerPageDomain(
                records = records,
                nextCursor = records.lastOrNull()?.cursor?.takeIf { candidates.size > limit },
            )
        }
    }
}
