package com.eyther.lumbridge.features.expenses.data.network.mapper

import com.eyther.lumbridge.features.expenses.data.network.model.response.ExpenseAccountResponse
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain

internal fun ExpenseAccountResponse.toDomain(): ExpenseAccountDomain =
    ExpenseAccountDomain(
        id = id,
        name = name,
        currency = currency,
        memberIds = memberIds.toSet(),
        isDefault = isDefault,
        archived = archived,
        version = version,
        createdBy = createdBy,
    )
