package com.eyther.lumbridge.features.expenses.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ExpenseAccountDataSource {
    fun stream(): Flow<AppResult<List<ExpenseAccountDomain>>>

    fun streamSelection(): StateFlow<String>

    suspend fun getAccount(id: String): AppResult<ExpenseAccountDomain>

    suspend fun selectAccount(id: String): AppResult<Unit>

    suspend fun saveAccount(account: ExpenseAccountDomain): AppResult<ExpenseAccountDomain>

    suspend fun deleteAccount(account: ExpenseAccountDomain): AppResult<Unit>
}
