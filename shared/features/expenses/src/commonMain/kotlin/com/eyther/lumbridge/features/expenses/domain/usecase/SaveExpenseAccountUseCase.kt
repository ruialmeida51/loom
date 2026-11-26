package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseAccountError
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseGuestDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdCurrencyDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase

class SaveExpenseAccountUseCase(
    private val expenseAccountDataSource: ExpenseAccountDataSource,
    private val getMembersUseCase: GetMembersUseCase,
    private val context: HouseholdContextDomain,
    private val saveAccountGuestUseCase: SaveAccountGuestUseCase,
) {
    /** Saves [account], then adds [newGuests] to it, returning the account with its guests. */
    suspend operator fun invoke(
        account: ExpenseAccountDomain,
        newGuests: List<ExpenseGuestDomain> = emptyList(),
    ): AppResult<ExpenseAccountDomain> =
        safeCall {
            requireDomain(newGuests.all { it.hasValidName() }) {
                ExpenseAccountError.InvalidGuestName()
            }
            requireDomain(account.name.trim().length in 1..80) { ExpenseAccountError.InvalidName() }
            requireDomain(account.currency in HouseholdCurrencyDomain.supportedCodes) {
                ExpenseError.UnsupportedCurrency()
            }
            if (account.version == 0L) {
                val members = getMembersUseCase(context.id).getOrThrow().map { it.id }.toSet()
                requireDomain(
                    context.userId in account.memberIds &&
                        account.memberIds.isNotEmpty() &&
                        members.containsAll(account.memberIds),
                ) {
                    ExpenseAccountError.InvalidMembers()
                }
            }
            val saved =
                expenseAccountDataSource
                    .saveAccount(account = account.copy(name = account.name.trim()))
                    .getOrThrow()
            val withIcon =
                if (saved.icon == account.icon) saved
                else
                    expenseAccountDataSource
                        .setIcon(accountId = saved.id, icon = account.icon)
                        .getOrThrow()
            newGuests.fold(withIcon) { current, guest ->
                saveAccountGuestUseCase(account = current, guest = guest).getOrThrow()
            }
        }
}
