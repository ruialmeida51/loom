package com.eyther.lumbridge.features.expenses.domain.datasource

import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.features.expenses.domain.model.*
import kotlinx.coroutines.flow.Flow

interface RecurringExpenseDataSource {
    fun observe(): Flow<List<RecurringExpenseDomain>>

    suspend fun control(
        rule: RecurringExpenseDomain,
        action: RecurringExpenseActionDomain,
    ): AppResult<RecurringExpenseDomain>
}
