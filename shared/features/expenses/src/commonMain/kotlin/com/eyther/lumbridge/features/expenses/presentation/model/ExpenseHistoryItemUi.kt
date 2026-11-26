package com.eyther.lumbridge.features.expenses.presentation.model

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import kotlinx.datetime.LocalDate

@Stable
sealed interface ExpenseHistoryItemUi {
    val key: String

    /** When the record was entered, which orders the ledger. */
    val addedAt: String
    val calendarDate: LocalDate
    val amount: String

    @Stable
    data class Purchase(
        val expense: ExpenseDomain,
        val payerName: String?,
        val canManage: Boolean = false,
        override val amount: String,
        /** The payer's photo, beside their name in the row. */
        val payerAvatarPath: String? = null,
        /** What was paid in another currency, such as "£48.00"; null when paid in the account's. */
        val originalAmount: String? = null,
    ) : ExpenseHistoryItemUi {
        override val key: String = "expense-${expense.id}"
        override val addedAt: String = expense.createdAt
        override val calendarDate: LocalDate = expense.occurredOn
    }

    @Stable
    data class Repayment(
        val settlement: SettlementDomain,
        val fromName: String?,
        val toName: String?,
        val canManage: Boolean = false,
        val isUserSender: Boolean = false,
        val isUserRecipient: Boolean = false,
        override val amount: String,
    ) : ExpenseHistoryItemUi {
        override val key: String = "settlement-${settlement.id}"
        override val addedAt: String = settlement.createdAt
        override val calendarDate: LocalDate = settlement.occurredOn
    }
}
