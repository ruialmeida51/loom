package com.eyther.lumbridge.features.expenses.data.network.mapper

import com.eyther.lumbridge.features.expenses.data.network.model.request.SaveExpenseAccountRequest
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain

internal fun ExpenseAccountDomain.toRequest(householdId: String): SaveExpenseAccountRequest =
    SaveExpenseAccountRequest(
        householdId = householdId,
        id = id,
        name = name,
        currency = currency,
        memberIds = memberIds,
        archived = archived,
        version = version,
    )
