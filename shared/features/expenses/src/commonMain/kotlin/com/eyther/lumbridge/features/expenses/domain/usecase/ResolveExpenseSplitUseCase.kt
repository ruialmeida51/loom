package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain

class ResolveExpenseSplitUseCase {
    operator fun invoke(
        amountMinor: Long,
        currency: String,
        method: ExpenseSplitMethodDomain,
        participants: Set<String>,
        custom: Map<String, String>,
    ): AppResult<ExpenseSplitSelectionDomain> = safeCall {
        val values =
            if (method == ExpenseSplitMethodDomain.EQUAL) emptyMap()
            else
                participants.associateWith { id ->
                    val value = custom[id].orEmpty()
                    if (method == ExpenseSplitMethodDomain.AMOUNTS)
                        MoneyDomain.parseShare(text = value, currency = currency).minorUnits
                    else value.trim().toLongOrNull() ?: throw ExpenseError.InvalidShare()
                }
        ExpenseSplitSelectionDomain(method = method, participants = participants, values = values)
            .also {
                ExpenseSplitDomain.resolve(amountMinor = amountMinor, selection = it)
            }
    }
}
