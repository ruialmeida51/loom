package com.eyther.lumbridge.features.expenses.data.network.mapper

import com.eyther.lumbridge.features.expenses.data.network.model.response.ExpenseReceiptResponse
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain

internal fun ExpenseReceiptResponse.toDomain(): ExpenseReceiptDomain =
    ExpenseReceiptDomain(
        expenseId = expenseId,
        accountId = accountId,
        objectPath = objectPath,
        version = version,
    )
