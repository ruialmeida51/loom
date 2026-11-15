package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseLedgerDomain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class GetExpenseLedgerStreamUseCase(
    private val getExpensesStreamUseCase: GetExpensesStreamUseCase,
    private val getSettlementsStreamUseCase: GetSettlementsStreamUseCase,
    private val getSelectedExpenseAccountStreamUseCase: GetSelectedExpenseAccountStreamUseCase,
) {
    operator fun invoke(): Flow<AppResult<ExpenseLedgerDomain>> =
        getSelectedExpenseAccountStreamUseCase().flatMapLatest { result ->
            when (result) {
                is AppResult.Failure -> flowOf(result)
                is AppResult.Success ->
                    combine(
                        getExpensesStreamUseCase(accountId = result.value.id),
                        getSettlementsStreamUseCase(accountId = result.value.id),
                    ) { expenses, settlements ->
                        safeCall {
                            ExpenseLedgerDomain(
                                account = result.value,
                                expenses = expenses.getOrThrow(),
                                settlements = settlements.getOrThrow(),
                            )
                        }
                    }
            }
        }
}
