package com.eyther.lumbridge.features.expenses.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseGuestDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ExpenseAccountDataSource {
    fun stream(): Flow<AppResult<List<ExpenseAccountDomain>>>

    fun streamSelection(): StateFlow<String>

    suspend fun getAccount(id: String): AppResult<ExpenseAccountDomain>

    /** Initial expense capture may reuse recent reference data; editing still uses getAccount. */
    suspend fun getReferenceAccount(id: String): AppResult<ExpenseAccountDomain> =
        getAccount(id = id)

    suspend fun selectAccount(id: String): AppResult<Unit>

    suspend fun saveAccount(account: ExpenseAccountDomain): AppResult<ExpenseAccountDomain>

    suspend fun deleteAccount(account: ExpenseAccountDomain): AppResult<Unit>

    /** Creates the guest when its version is 0, otherwise renames it; returns the saved account. */
    suspend fun saveGuest(
        accountId: String,
        guest: ExpenseGuestDomain,
    ): AppResult<ExpenseAccountDomain>

    /** Removes a guest whose account balance is zero; returns the saved account. */
    suspend fun removeGuest(
        accountId: String,
        guest: ExpenseGuestDomain,
    ): AppResult<ExpenseAccountDomain>
}
