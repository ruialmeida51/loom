package com.eyther.lumbridge.features.home.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.presentation.model.BalanceSummaryUi
import com.eyther.lumbridge.features.home.domain.model.HomeOverviewDomain
import com.eyther.lumbridge.features.home.presentation.model.HomeReminderUi
import com.eyther.lumbridge.features.home.presentation.model.HomeShoppingUi

data class HomeState(
    val overview: HomeOverviewDomain? = null,
    val currency: String = "EUR",
    val timeZone: String = "UTC",
    val error: TextResource? = null,
) {

    val loading: Boolean = overview == null && error == null
    val shoppingCount: Int = overview?.shopping?.size ?: 0
    val shopping: List<HomeShoppingUi> =
        overview?.shopping.orEmpty().take(3).map { item ->
            HomeShoppingUi(id = item.id, name = item.name, quantity = item.quantity)
        }
    val reminders: List<HomeReminderUi> =
        overview?.reminders.orEmpty().take(2).map { reminder ->
            HomeReminderUi(
                id = reminder.id,
                title = reminder.title,
                due = reminder.dueLabel(timeZone),
            )
        }

    val balance: BalanceSummaryUi =
        BalanceSummaryUi(
            money = MoneyDomain(minorUnits = overview?.balanceMinor ?: 0L, currency = currency),
        )
}
