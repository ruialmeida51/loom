package com.eyther.lumbridge.features.home.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.usecase.GetAccountBalancesStreamUseCase
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlinx.coroutines.flow.Flow

/**
 * Balances of the household's everyday account (whose id is the household's), which Home's Add
 * expense also writes to, independent of the account selected on Money. The server adds them up
 * over the account's whole history.
 */
class GetHomeBalanceStreamUseCase(
    private val getAccountBalancesStreamUseCase: GetAccountBalancesStreamUseCase,
    private val context: HouseholdContextDomain,
) {
    operator fun invoke(): Flow<AppResult<Map<String, Long>>> =
        getAccountBalancesStreamUseCase(accountId = context.id)
}
