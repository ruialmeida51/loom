package com.eyther.lumbridge.features.expenses.data.network.mapper

import com.eyther.lumbridge.features.expenses.data.network.model.response.MemberAccountPositionResponse
import com.eyther.lumbridge.features.expenses.domain.model.MemberAccountPositionDomain

internal fun MemberAccountPositionResponse.toDomain(): MemberAccountPositionDomain =
    MemberAccountPositionDomain(
        accountId = accountId,
        accountName = accountName,
        isDefault = isDefault,
        currency = currency,
        balanceMinor = balanceMinor,
    )
