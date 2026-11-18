package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.errors.SettlementError
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import kotlinx.datetime.LocalDate

class SaveSettlementUseCase(private val expenseDataSource: ExpenseDataSource) {
    suspend operator fun invoke(settlement: SettlementDomain): AppResult<SettlementDomain> =
        safeCall {
            requireDomain(settlement.version > 0) { SettlementError.Changed() }
            requireDomain(
                settlement.from != settlement.to &&
                    settlement.amountMinor in 1..MoneyDomain.MAX_MINOR_UNITS,
            ) {
                ExpenseError.InvalidSettlement()
            }
            try {
                LocalDate.parse(settlement.occurredOn)
            } catch (cause: IllegalArgumentException) {
                throw ExpenseError.InvalidPaymentDate(cause = cause)
            }
            expenseDataSource.saveSettlement(settlement = settlement).getOrThrow()
        }
}
