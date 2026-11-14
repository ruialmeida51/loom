package com.eyther.lumbridge.features.expenses.presentation.model

import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain

data class BalanceSummaryUi(val money: MoneyDomain) {

    val isOwed: Boolean = money.minorUnits >= 0
    val isSettled: Boolean = money.minorUnits == 0L
    val amount: String = money.formattedMagnitude()
}
