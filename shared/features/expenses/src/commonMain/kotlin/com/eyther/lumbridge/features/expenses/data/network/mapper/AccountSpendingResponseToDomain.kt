package com.eyther.lumbridge.features.expenses.data.network.mapper

import com.eyther.lumbridge.features.expenses.data.network.model.response.AccountSpendingResponse
import com.eyther.lumbridge.features.expenses.domain.model.AccountSpendingDomain

internal fun List<AccountSpendingResponse>.toSpending(): AccountSpendingDomain =
    AccountSpendingDomain(
        paidByPerson = associate { it.userId to it.paidMinor },
        expenseCount = sumOf { it.expenseCount },
    )
