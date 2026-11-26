package com.eyther.lumbridge.features.expenses.data.network.mapper

import com.eyther.lumbridge.features.expenses.data.network.model.response.ExpenseAccountResponse
import com.eyther.lumbridge.features.expenses.data.network.model.response.ExpenseGuestResponse
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseGuestDomain

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
        guests = guests.map { it.toDomain() },
    )

internal fun ExpenseGuestResponse.toDomain(): ExpenseGuestDomain =
    ExpenseGuestDomain(id = id, name = name, removed = removed, version = version)
