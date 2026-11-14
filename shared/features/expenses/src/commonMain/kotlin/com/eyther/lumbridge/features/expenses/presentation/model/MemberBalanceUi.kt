package com.eyther.lumbridge.features.expenses.presentation.model

import com.eyther.lumbridge.features.household.domain.model.MemberDomain

data class MemberBalanceUi(
    val member: MemberDomain,
    val balance: BalanceSummaryUi,
)
