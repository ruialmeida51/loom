package com.eyther.lumbridge.features.expenses.presentation.model

import androidx.compose.runtime.Immutable
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

@Immutable
data class ExpenseDetailsUi(
    val title: String,
    val amount: String,
    val category: String,
    val occurredOn: LocalDate,
    val payerName: String?,
    val accountName: String,
    val isEveryday: Boolean,
    val recurring: Boolean,
    val splitMethod: ExpenseSplitMethodDomain,
    val ownShare: String?,
    val shares: ImmutableList<ExpenseShareUi>,
    /** "Paid £48.00 · 1 GBP = 1.1546 EUR" for an expense paid in another currency. */
    val originalAmount: String? = null,
    val originalRate: String? = null,
)
