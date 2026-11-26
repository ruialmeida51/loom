package com.eyther.lumbridge.features.expenses.data.network.mapper

import com.eyther.lumbridge.features.expenses.data.network.model.response.ExpenseResponse
import com.eyther.lumbridge.features.expenses.data.network.model.response.RecurringExpenseResponse
import com.eyther.lumbridge.features.expenses.data.network.model.response.SettlementResponse
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.OriginalAmountDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain

internal fun ExpenseResponse.toDomain(): ExpenseDomain =
    ExpenseDomain(
        id = id,
        accountId = accountId,
        title = title,
        amountMinor = amountMinor,
        paidBy = paidBy,
        category = category,
        occurredOn = occurredOn,
        shares = shares,
        recurring = recurring,
        version = version,
        createdBy = createdBy,
        splitMethod = splitMethod.toSplitMethod(),
        splitValues = splitValues,
        createdAt = createdAt,
        original =
            if (originalCurrency != null && originalAmountMinor != null && exchangeRate != null)
                OriginalAmountDomain(
                    currency = originalCurrency,
                    amountMinor = originalAmountMinor,
                    rate = OriginalAmountDomain.normalizeRate(exchangeRate.content) ?: "1",
                )
            else null,
    )

internal fun SettlementResponse.toDomain(): SettlementDomain =
    SettlementDomain(
        id = id,
        accountId = accountId,
        from = from,
        to = to,
        amountMinor = amountMinor,
        occurredOn = occurredOn,
        createdBy = createdBy,
        version = version,
        createdAt = createdAt,
    )

internal fun RecurringExpenseResponse.toDomain(): RecurringExpenseDomain =
    RecurringExpenseDomain(
        id = id,
        accountId = accountId,
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
        splitMethod = splitMethod.toSplitMethod(),
        splitValues = splitValues,
    )
