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
            "expenses.message_choose_two_different_people_and_a_positive_amount"
        }
        require(context.userId == settlement.from || context.userId == settlement.to) {
            "expenses.message_you_can_record_repayments_you_send_or_receive"
        }
        LocalDate.parse(settlement.occurredOn)
        val memberIds = members(context.id).getOrThrow().map { it.id }
        require(settlement.from in memberIds && settlement.to in memberIds) {
            "expenses.message_choose_current_household_members"
        }
        source.settle(settlement).getOrThrow()
    }
}
