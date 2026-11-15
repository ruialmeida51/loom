package com.eyther.lumbridge.features.expenses.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseActionDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import kotlinx.coroutines.flow.Flow

interface RecurringExpenseDataSource {
    fun stream(accountId: String = ""): Flow<AppResult<List<RecurringExpenseDomain>>>

    suspend fun controlSchedule(
        rule: RecurringExpenseDomain,
        action: RecurringExpenseActionDomain,
    ): AppResult<RecurringExpenseDomain>
}
