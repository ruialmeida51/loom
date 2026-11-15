package com.eyther.lumbridge.features.insights.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseLedgerStreamUseCase
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.insights.domain.model.MonthlyStatementDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetMonthlyStatementStreamUseCase(
    private val getExpenseLedgerStreamUseCase: GetExpenseLedgerStreamUseCase,
    private val context: HouseholdContextDomain,
) {

    operator fun invoke(month: Flow<String>): Flow<AppResult<MonthlyStatementDomain>> =
        combine(
            getExpenseLedgerStreamUseCase(),
            month,
        ) { ledgerResult, selectedMonth ->
            safeCall {
                val ledger = ledgerResult.getOrThrow()

                MonthlyStatementDomain.build(
                        month = selectedMonth,
                        userId = context.userId,
                        expenses = ledger.expenses,
                        settlements = ledger.settlements,
                    )
                    .copy(accountId = ledger.account.id, currency = ledger.account.currency)
            }
        }
}
