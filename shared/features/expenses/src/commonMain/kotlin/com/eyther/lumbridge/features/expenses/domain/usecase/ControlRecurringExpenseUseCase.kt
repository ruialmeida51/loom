package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.*

class ControlRecurringExpenseUseCase(private val source: RecurringExpenseDataSource) {
    suspend operator fun invoke(
        rule: RecurringExpenseDomain,
        action: RecurringExpenseActionDomain,
    ): AppResult<RecurringExpenseDomain> {
        if (rule.cancelled && action != RecurringExpenseActionDomain.CANCEL)
            return AppResult.Failure("expenses.message_a_cancelled_schedule_cannot_be_resumed")
        return source.control(rule, action)
    }
}
