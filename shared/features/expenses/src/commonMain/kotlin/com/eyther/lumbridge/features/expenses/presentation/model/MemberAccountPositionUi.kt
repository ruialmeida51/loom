package com.eyther.lumbridge.features.expenses.presentation.model

import com.eyther.lumbridge.features.expenses.domain.model.MemberAccountPositionDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain

data class MemberAccountPositionUi(val position: MemberAccountPositionDomain) {
    val balance =
        BalanceSummaryUi(
            money = MoneyDomain(minorUnits = position.balanceMinor, currency = position.currency),
        )
}
