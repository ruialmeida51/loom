package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.RecurringExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseActionDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain

class ControlRecurringExpenseUseCase(
    private val recurringExpenseDataSource: RecurringExpenseDataSource,
) {

    suspend operator fun invoke(
        rule: RecurringExpenseDomain,
        action: RecurringExpenseActionDomain,
    ): AppResult<RecurringExpenseDomain> {
        if (rule.cancelled && action != RecurringExpenseActionDomain.CANCEL)
            return AppResult.Failure(RecurringExpenseError.Cancelled())
        return recurringExpenseDataSource.controlSchedule(rule, action)
    }
}
