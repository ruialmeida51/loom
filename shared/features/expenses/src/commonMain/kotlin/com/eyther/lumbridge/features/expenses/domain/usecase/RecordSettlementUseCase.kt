package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseAccountError
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
import kotlinx.datetime.LocalDate

class RecordSettlementUseCase(
    private val expenseDataSource: ExpenseDataSource,
    private val expenseAccountDataSource: ExpenseAccountDataSource,
    private val members: GetMembersUseCase,
    private val context: HouseholdContextDomain,
) {

    suspend operator fun invoke(settlement: SettlementDomain): AppResult<Unit> = safeCall {
        requireDomain(
            settlement.amountMinor in 1..MoneyDomain.MAX_MINOR_UNITS &&
                settlement.from != settlement.to,
        ) {
            ExpenseError.InvalidSettlement()
        }
        requireDomain(context.userId == settlement.from || context.userId == settlement.to) {
            ExpenseError.SettlementNotPermitted()
        }
        try {
            LocalDate.parse(settlement.occurredOn)
        } catch (cause: IllegalArgumentException) {
            throw ExpenseError.InvalidPaymentDate(cause = cause)
        }
        val memberIds = members(context.id).getOrThrow().map { it.id }
        requireDomain(settlement.from in memberIds && settlement.to in memberIds) {
            ExpenseError.InvalidSettlementMembers()
        }
        val account = expenseAccountDataSource.getAccount(id = settlement.accountId).getOrThrow()
        requireDomain(settlement.from in account.memberIds && settlement.to in account.memberIds) {
            ExpenseAccountError.InvalidMembers()
        }
        expenseDataSource.recordSettlement(settlement).getOrThrow()
    }
}
