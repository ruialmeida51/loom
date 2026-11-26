package com.eyther.lumbridge.features.expenses.domain.model

import kotlin.time.Instant

/**
 * Where the next ledger page starts: the last record shown, in [LedgerRecordDomain.NEWEST_FIRST]
 * order. The id breaks ties between records entered in the same instant, across both tables.
 */
data class LedgerCursorDomain(val createdAt: Instant, val id: String)
