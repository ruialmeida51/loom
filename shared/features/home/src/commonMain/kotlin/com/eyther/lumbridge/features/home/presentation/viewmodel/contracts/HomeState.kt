package com.eyther.lumbridge.features.home.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.presentation.model.BalanceSummaryUi
import com.eyther.lumbridge.features.home.domain.model.HomeOverviewDomain

data class HomeState(
    val overview: HomeOverviewDomain? = null,
    val currency: String = "EUR",
    val error: TextResource? = null,
) {

    val balance: BalanceSummaryUi = BalanceSummaryUi(
        money = MoneyDomain(minorUnits = overview?.balanceMinor ?: 0L, currency = currency),
    )
}
