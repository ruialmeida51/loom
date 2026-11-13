package com.eyther.lumbridge.features.home.domain.usecase

import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.features.expenses.domain.model.HouseholdLedgerDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.ObserveExpensesUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.ObserveSettlementsUseCase
import com.eyther.lumbridge.features.home.domain.model.HomeOverviewDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.reminders.domain.usecase.ObserveRemindersUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.ObserveShoppingUseCase
import kotlinx.coroutines.flow.*

class GetHomeOverviewUseCase(
    private val shopping: ObserveShoppingUseCase,
    private val reminders: ObserveRemindersUseCase,
    private val expenses: ObserveExpensesUseCase,
    private val settlements: ObserveSettlementsUseCase,
    private val context: HouseholdContextDomain,
) {
    operator fun invoke(): Flow<HomeOverviewDomain> =
        combine(shopping(), reminders(), expenses(), settlements()) {
            shopping,
            reminders,
            expenses,
            settlements ->
            HomeOverviewDomain(
                shopping.filterNot { it.checked },
                reminders.filterNot { it.completed }.sortedBy { it.dueAt },
                HouseholdLedgerDomain.balances(expenses, settlements)[context.userId] ?: 0,
                expenses
                    .filter { it.occurredOn.startsWith(todayIso(context.timeZone).take(7)) }
                    .sumOf { it.amountMinor },
            )
        }
}
