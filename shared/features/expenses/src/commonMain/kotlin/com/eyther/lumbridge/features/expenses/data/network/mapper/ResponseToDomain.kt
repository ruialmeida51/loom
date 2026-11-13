package com.eyther.lumbridge.features.expenses.data.network.mapper

import com.eyther.lumbridge.features.expenses.data.network.model.response.ExpenseResponse
import com.eyther.lumbridge.features.expenses.data.network.model.response.RecurringExpenseResponse
import com.eyther.lumbridge.features.expenses.data.network.model.response.SettlementResponse
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain

internal fun ExpenseResponse.toDomain(): ExpenseDomain = ExpenseDomain(
    id = id,
    title = title,
    amountMinor = amountMinor,
    paidBy = paidBy,
    category = category,
    occurredOn = occurredOn,
    shares = shares,
    recurring = recurring,
)

internal fun SettlementResponse.toDomain(): SettlementDomain = SettlementDomain(
    id = id,
    from = from,
    to = to,
    amountMinor = amountMinor,
    occurredOn = occurredOn,
)

internal fun RecurringExpenseResponse.toDomain(): RecurringExpenseDomain = RecurringExpenseDomain(
    id = id,
    title = title,
    amountMinor = amountMinor,
    paidBy = paidBy,
    category = category,
    nextOn = nextOn,
    anchorDay = anchorDay,
    active = active,
    cancelled = cancelledAt != null,
    createdBy = createdBy,
    version = version,
    shares = shares,
)
