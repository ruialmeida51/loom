package com.eyther.lumbridge.features.home.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.model.HouseholdLedgerDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpensesStreamUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetSettlementsStreamUseCase
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Balances of the household's everyday account (whose id is the household's), which Home's Add
 * expense also writes to, independent of the account selected on Money.
 */
class GetHomeBalanceStreamUseCase(
    private val getExpensesStreamUseCase: GetExpensesStreamUseCase,
    private val getSettlementsStreamUseCase: GetSettlementsStreamUseCase,
    private val context: HouseholdContextDomain,
) {
    operator fun invoke(): Flow<AppResult<Map<String, Long>>> =
        combine(
            getExpensesStreamUseCase(accountId = context.id),
            getSettlementsStreamUseCase(accountId = context.id),
        ) { expenses, settlements ->
            safeCall {
                HouseholdLedgerDomain.balances(
                    expenses = expenses.getOrThrow(),
                    settlements = settlements.getOrThrow(),
                )
            }
        }
}
