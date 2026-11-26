package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseAccountError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountIconDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseGuestDomain
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class DemoExpenseAccountDataSource(private val store: DemoHouseholdStore) :
    ExpenseAccountDataSource {
    private val selected = store.selectedExpenseAccount
    private val accounts = store.expenseAccounts

    override fun stream() = accounts.map { AppResult.Success(it) }

    override fun streamSelection() = selected.asStateFlow()

    override suspend fun getAccount(id: String) = safeCall { accounts.value.first { it.id == id } }

    override suspend fun selectAccount(id: String): AppResult<Unit> = safeCall {
        selected.value = id
    }

    override suspend fun saveAccount(account: ExpenseAccountDomain) = safeCall {
        val saved = account.copy(version = account.version + 1, createdBy = store.context.userId)
        accounts.update { oldAccounts -> oldAccounts.filterNot { it.id == saved.id } + saved }
        saved
    }

    override suspend fun deleteAccount(account: ExpenseAccountDomain): AppResult<Unit> = safeCall {
        requireDomain(account.canManage(userId = store.context.userId, isOwner = true)) {
            ExpenseAccountError.PermissionDenied()
        }
        store.expenses.update { items -> items.filterNot { it.accountId == account.id } }
        store.settlements.update { items -> items.filterNot { it.accountId == account.id } }
        store.recurring.update { items -> items.filterNot { it.accountId == account.id } }
        accounts.update { items -> items.filterNot { it.id == account.id } }
        if (selected.value == account.id) selected.value = store.context.id
    }

    override suspend fun saveGuest(accountId: String, guest: ExpenseGuestDomain) = safeCall {
        val account = accounts.value.first { it.id == accountId }
        requireDomain(account.acceptsGuests) { ExpenseAccountError.GuestsNotOnEveryday() }
        val saved = guest.copy(version = guest.version + 1)
        val guests =
            if (account.guests.any { it.id == guest.id })
                account.guests.map { if (it.id == guest.id) saved else it }
            else account.guests + saved
        replace(account.copy(guests = guests))
    }

    override suspend fun removeGuest(accountId: String, guest: ExpenseGuestDomain) = safeCall {
        val account = accounts.value.first { it.id == accountId }
        val spent =
            store.expenses.value
                .filter { it.accountId == accountId }
                .sumOf { expense ->
                    (if (expense.paidBy == guest.id) expense.amountMinor else 0L) -
                        (expense.shares[guest.id] ?: 0L)
                }
        val settled =
            store.settlements.value
                .filter { it.accountId == accountId }
                .sumOf { settlement ->
                    when (guest.id) {
                        settlement.from -> settlement.amountMinor
                        settlement.to -> -settlement.amountMinor
                        else -> 0L
                    }
                }
        requireDomain(spent + settled == 0L) { ExpenseAccountError.GuestHasBalance() }
        replace(
            account.copy(
                guests =
                    account.guests.map {
                        if (it.id == guest.id) it.copy(removed = true, version = it.version + 1)
                        else it
                    },
            ),
        )
    }

    override suspend fun setIcon(accountId: String, icon: ExpenseAccountIconDomain?) = safeCall {
        replace(accounts.value.first { it.id == accountId }.copy(icon = icon))
    }

    private fun replace(account: ExpenseAccountDomain): ExpenseAccountDomain {
        accounts.update { items -> items.map { if (it.id == account.id) account else it } }
        return account
    }
}
