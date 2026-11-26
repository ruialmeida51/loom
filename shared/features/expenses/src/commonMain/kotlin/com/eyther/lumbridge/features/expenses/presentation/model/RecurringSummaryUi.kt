package com.eyther.lumbridge.features.expenses.presentation.model

import androidx.compose.runtime.Immutable
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseStatusDomain
import kotlinx.datetime.LocalDate

/** How many bills repeat in an account and the one due next, for the Money tab shortcut. */
@Immutable
data class RecurringSummaryUi(
    val activeCount: Int,
    val pausedCount: Int,
    val nextTitle: String?,
    val nextAmount: String?,
    val nextOn: LocalDate?,
) {
    companion object {
        fun from(rules: List<RecurringExpenseDomain>, currency: String): RecurringSummaryUi {
            val active = rules.filter { it.status == RecurringExpenseStatusDomain.ACTIVE }
            val next = active.minByOrNull { it.nextOn }
            return RecurringSummaryUi(
                activeCount = active.size,
                pausedCount = rules.count { it.status == RecurringExpenseStatusDomain.PAUSED },
                nextTitle = next?.title,
                nextAmount =
                    next?.let { MoneyDomain(minorUnits = it.amountMinor, currency = currency) }
                        ?.formatted(),
                nextOn = next?.nextOn?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            )
        }
    }
}
