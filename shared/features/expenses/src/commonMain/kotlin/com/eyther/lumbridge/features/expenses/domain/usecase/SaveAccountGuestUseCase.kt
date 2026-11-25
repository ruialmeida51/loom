package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseAccountError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseGuestDomain

class SaveAccountGuestUseCase(private val expenseAccountDataSource: ExpenseAccountDataSource) {
    suspend operator fun invoke(
        account: ExpenseAccountDomain,
        guest: ExpenseGuestDomain,
    ): AppResult<ExpenseAccountDomain> = safeCall {
        requireDomain(account.acceptsGuests) { ExpenseAccountError.GuestsNotOnEveryday() }
        val named = guest.copy(name = guest.name.trim())
        requireDomain(named.hasValidName()) { ExpenseAccountError.InvalidGuestName() }
        if (named.version == 0L) {
            requireDomain(account.activeGuests.size < ExpenseGuestDomain.MAXIMUM_PER_ACCOUNT) {
                ExpenseAccountError.GuestLimit()
            }
        }
        expenseAccountDataSource.saveGuest(accountId = account.id, guest = named).getOrThrow()
    }
}
