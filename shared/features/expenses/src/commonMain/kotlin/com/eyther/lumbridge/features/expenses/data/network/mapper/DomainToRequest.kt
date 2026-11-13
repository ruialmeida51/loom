package com.eyther.lumbridge.features.expenses.data.network.mapper

import com.eyther.lumbridge.features.expenses.data.network.model.request.CreateExpenseRequest
import com.eyther.lumbridge.features.expenses.data.network.model.request.RecordSettlementRequest
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain

internal fun ExpenseDomain.toRequest(householdId: String): CreateExpenseRequest = CreateExpenseRequest(
    householdId = householdId,
    id = id,
    title = title,
    amountMinor = amountMinor,
    paidBy = paidBy,
    category = category,
    occurredOn = occurredOn,
    shares = shares,
    recurring = recurring,
)

internal fun SettlementDomain.toRequest(householdId: String): RecordSettlementRequest = RecordSettlementRequest(
    householdId = householdId,
    id = id,
    from = from,
    to = to,
    amountMinor = amountMinor,
    occurredOn = occurredOn,
)
