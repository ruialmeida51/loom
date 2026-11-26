package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseGuestDomain

/** The server refuses guests with a non-zero balance ([ExpenseAccountError.GuestHasBalance]). */
class RemoveAccountGuestUseCase(private val expenseAccountDataSource: ExpenseAccountDataSource) {
    suspend operator fun invoke(
        account: ExpenseAccountDomain,
        guest: ExpenseGuestDomain,
    ): AppResult<ExpenseAccountDomain> =
        expenseAccountDataSource.removeGuest(accountId = account.id, guest = guest)
}
