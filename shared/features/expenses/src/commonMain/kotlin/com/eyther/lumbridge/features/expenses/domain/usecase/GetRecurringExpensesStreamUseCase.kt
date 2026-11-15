package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.mapToResult
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseLedgerDomain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class GetRecurringExpensesStreamUseCase(
    private val recurringExpenseDataSource: RecurringExpenseDataSource,
    private val getSelectedExpenseAccountStreamUseCase: GetSelectedExpenseAccountStreamUseCase,
) {
    operator fun invoke(): Flow<AppResult<RecurringExpenseLedgerDomain>> =
        getSelectedExpenseAccountStreamUseCase().flatMapLatest { result ->
            when (result) {
                is AppResult.Failure -> flowOf(result)
                is AppResult.Success ->
                    recurringExpenseDataSource.stream(accountId = result.value.id).map { rules ->
                        rules.mapToResult {
                            RecurringExpenseLedgerDomain(account = result.value, rules = it)
                        }
                    }
            }
        }
}
