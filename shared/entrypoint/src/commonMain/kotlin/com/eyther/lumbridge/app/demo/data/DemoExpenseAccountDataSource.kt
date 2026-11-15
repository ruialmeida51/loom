package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
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
}
