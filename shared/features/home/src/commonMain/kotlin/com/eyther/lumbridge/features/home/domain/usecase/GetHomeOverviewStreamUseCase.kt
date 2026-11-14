package com.eyther.lumbridge.features.home.domain.usecase

import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseLedgerStreamUseCase
import com.eyther.lumbridge.features.home.domain.model.HomeOverviewDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.reminders.domain.usecase.GetRemindersStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetHomeOverviewStreamUseCase(
    private val getShoppingStreamUseCase: GetShoppingStreamUseCase,
    private val getRemindersStreamUseCase: GetRemindersStreamUseCase,
    private val getExpenseLedgerStreamUseCase: GetExpenseLedgerStreamUseCase,
    private val context: HouseholdContextDomain,
) {

    operator fun invoke(): Flow<AppResult<HomeOverviewDomain>> = combine(
        getShoppingStreamUseCase(),
        getRemindersStreamUseCase(),
        getExpenseLedgerStreamUseCase(),
    ) { shoppingResult, remindersResult, ledgerResult ->
        safeCall {
            val shopping = shoppingResult.getOrThrow()
            val reminders = remindersResult.getOrThrow()
            val ledger = ledgerResult.getOrThrow()
            val month = todayIso(context.timeZone).take(7)

            HomeOverviewDomain(
                shopping = shopping.activeItems.filterNot { it.checked },
                reminders = reminders.filterNot { it.completed }.sortedBy { it.dueAt },
                balanceMinor = ledger.balances[context.userId] ?: 0,
                monthSpendingMinor = ledger.expenses.filter { it.occurredOn.startsWith(month) }.sumOf { it.amountMinor },
            )
        }
    }
}
