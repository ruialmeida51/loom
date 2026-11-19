package com.eyther.lumbridge.features.expenses.data.network.mapper

import com.eyther.lumbridge.features.expenses.data.network.model.request.SaveRecurringExpenseRequest
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain

internal fun RecurringExpenseDomain.toSaveRequest(
    householdId: String,
): SaveRecurringExpenseRequest =
    SaveRecurringExpenseRequest(
        householdId = householdId,
        id = id,
        version = version,
        title = title,
        amountMinor = amountMinor,
        paidBy = paidBy,
        nextOn = nextOn,
        shares = shares,
        splitMethod = splitMethod.name.lowercase(),
        splitValues = splitValues,
    )
