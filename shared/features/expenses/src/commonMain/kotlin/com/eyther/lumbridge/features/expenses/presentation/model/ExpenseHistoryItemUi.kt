package com.eyther.lumbridge.features.expenses.presentation.model

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain

@Stable
sealed interface ExpenseHistoryItemUi {
    val key: String
    val date: String
    val amount: String

    @Stable
    data class Purchase(
        val expense: ExpenseDomain,
        val payerName: String?,
        val yourShare: String,
        override val amount: String,
    ) : ExpenseHistoryItemUi {
        override val key: String = "expense-${expense.id}"
        override val date: String = expense.occurredOn
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
    }
}
