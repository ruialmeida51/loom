package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.core.domain.getOrThrow
import com.eyther.lumbridge.core.domain.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
import kotlinx.datetime.LocalDate

class RecordSettlementUseCase(
    private val source: ExpenseDataSource,
    private val members: GetMembersUseCase,
    private val context: HouseholdContextDomain,
) {
    suspend operator fun invoke(settlement: SettlementDomain): AppResult<Unit> = safeCall {
        require(
            settlement.amountMinor in 1..MoneyDomain.MAX_MINOR_UNITS &&
                settlement.from != settlement.to,
        ) {
            "Choose two different people and a positive amount."
        }
        require(context.userId == settlement.from || context.userId == settlement.to) {
            "You can record repayments you send or receive."
        }
        LocalDate.parse(settlement.occurredOn)
        val memberIds = members(context.id).getOrThrow().map { it.id }
        require(settlement.from in memberIds && settlement.to in memberIds) {
            "Choose current household members."
        }
        source.settle(settlement).getOrThrow()
    }
}
