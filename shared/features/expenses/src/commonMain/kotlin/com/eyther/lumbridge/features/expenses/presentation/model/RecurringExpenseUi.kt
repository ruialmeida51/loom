package com.eyther.lumbridge.features.expenses.presentation.model

import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseActionDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import kotlinx.datetime.LocalDate

/** Rebuilt from schedule and membership facts so a role update also updates available actions. */
data class RecurringExpenseUi(
    val rule: RecurringExpenseDomain,
    val userId: String,
    val role: String?,
    val payerName: String?,
    val currency: String,
) {

    val canManage: Boolean = rule.canManage(userId = userId, isOwner = role == "owner")
    val canDelete: Boolean = rule.canDelete(userId = userId, isOwner = role == "owner")
    val amount: String = MoneyDomain(minorUnits = rule.amountMinor, currency = currency).formatted()
    val toggleAction: RecurringExpenseActionDomain =
        if (rule.active) {
            RecurringExpenseActionDomain.PAUSE
        } else {
            RecurringExpenseActionDomain.RESUME
        }
    val isPaused: Boolean = !rule.active && !rule.cancelled
    val nextOn: LocalDate? = runCatching { LocalDate.parse(rule.nextOn) }.getOrNull()
}
