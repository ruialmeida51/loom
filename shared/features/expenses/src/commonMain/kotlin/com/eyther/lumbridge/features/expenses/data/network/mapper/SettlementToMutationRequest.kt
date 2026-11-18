package com.eyther.lumbridge.features.expenses.data.network.mapper

import com.eyther.lumbridge.features.expenses.data.network.model.request.RemoveSettlementRequest
import com.eyther.lumbridge.features.expenses.data.network.model.request.SaveSettlementRequest
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain

internal fun SettlementDomain.toSaveRequest(householdId: String): SaveSettlementRequest =
    SaveSettlementRequest(
        householdId = householdId,
        accountId = accountId,
        id = id,
        from = from,
        to = to,
        amountMinor = amountMinor,
        occurredOn = occurredOn,
        version = version,
    )

internal fun SettlementDomain.toRemoveRequest(householdId: String): RemoveSettlementRequest =
    RemoveSettlementRequest(
        householdId = householdId,
        accountId = accountId,
        id = id,
        version = version,
    )
