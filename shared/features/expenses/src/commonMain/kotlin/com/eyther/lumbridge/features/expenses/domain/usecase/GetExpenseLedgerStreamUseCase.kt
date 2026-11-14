package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseLedgerDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetExpenseLedgerStreamUseCase(
    private val getExpensesStreamUseCase: GetExpensesStreamUseCase,
    private val getSettlementsStreamUseCase: GetSettlementsStreamUseCase,
) {

    operator fun invoke(): Flow<AppResult<ExpenseLedgerDomain>> = combine(
        getExpensesStreamUseCase(),
        getSettlementsStreamUseCase(),
    ) { expensesResult, settlementsResult ->
        safeCall {
            ExpenseLedgerDomain(
                expenses = expensesResult.getOrThrow(),
                settlements = settlementsResult.getOrThrow(),
            )
        }
    }
}
