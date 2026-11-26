package com.eyther.lumbridge.features.expenses.domain.model

import com.eyther.lumbridge.core.domain.errors.AppError

/**
 * The Money overview of one account: the loaded part of its history and everyone's balance,
 * which the server adds up over the whole history rather than over the loaded pages.
 */
data class AccountLedgerDomain(
    val account: ExpenseAccountDomain,
    val history: LedgerPageDomain,
    val balances: Map<String, Long>,
    /** An older page is on its way. */
    val loadingMore: Boolean = false,
    /** The last older page failed to load; the pages already loaded are still [history]. */
    val loadMoreError: AppError? = null,
)
