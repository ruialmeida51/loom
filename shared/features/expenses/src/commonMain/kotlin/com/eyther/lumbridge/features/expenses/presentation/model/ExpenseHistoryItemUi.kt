package com.eyther.lumbridge.features.expenses.presentation.model

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import kotlinx.datetime.LocalDate

@Stable
sealed interface ExpenseHistoryItemUi {
    val key: String
    val date: String
    val calendarDate: LocalDate
    val amount: String

    @Stable
    data class Purchase(
        val expense: ExpenseDomain,
        val payerName: String?,
        override val amount: String,
    ) : ExpenseHistoryItemUi {
        override val key: String = "expense-${expense.id}"
        override val date: String = expense.occurredOn
        override val calendarDate: LocalDate = LocalDate.parse(date)
    }

    @Stable
    data class Repayment(
        val settlement: SettlementDomain,
        val fromName: String?,
        val toName: String?,
        override val amount: String,
    ) : ExpenseHistoryItemUi {
        override val key: String = "settlement-${settlement.id}"
        override val date: String = settlement.occurredOn
        override val calendarDate: LocalDate = LocalDate.parse(date)
    }
}
