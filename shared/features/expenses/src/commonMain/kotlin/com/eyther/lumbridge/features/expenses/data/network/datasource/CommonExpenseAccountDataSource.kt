package com.eyther.lumbridge.features.expenses.data.network.datasource

import com.eyther.lumbridge.core.data.local.PlatformStorage
import com.eyther.lumbridge.core.data.local.ReferenceSnapshot
import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.asApiResult
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.network.mapper.toDomain
import com.eyther.lumbridge.features.expenses.data.network.mapper.toExpenseError
import com.eyther.lumbridge.features.expenses.data.network.mapper.toRequest
import com.eyther.lumbridge.features.expenses.data.network.model.request.DeleteExpenseAccountRequest
import com.eyther.lumbridge.features.expenses.data.network.model.response.ExpenseAccountResponse
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class CommonExpenseAccountDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : ExpenseAccountDataSource {
    private val selectionKey = "expense-account-${context.userId}-${context.id}"
    private val selectedId = MutableStateFlow(PlatformStorage.get(selectionKey) ?: context.id)
    private val changes = MutableStateFlow(0L)
    private val accounts = ReferenceSnapshot<List<ExpenseAccountDomain>>()

    override fun streamSelection(): StateFlow<String> = selectedId.asStateFlow()

    override fun stream(): Flow<AppResult<List<ExpenseAccountDomain>>> =
        changes
            .flatMapLatest {
                access.watch(table = "expense_accounts", scopeId = context.id) {
                    val cacheKey = "${access.userId()}:${context.id}"
                    access
                        .requireClient()
                        .from("expense_accounts_with_members")
                        .select { filter { eq("household_id", context.id) } }
                        .decodeList<ExpenseAccountResponse>()
                        .map { it.toDomain() }
                        .also { accounts.put(key = cacheKey, value = it) }
                }
            }
            .asApiResult(mapError = Exception::toExpenseError)

    override suspend fun getAccount(id: String): AppResult<ExpenseAccountDomain> =
        safeApiCall(mapError = Exception::toExpenseError) {
            readAccount(id = id)
        }

    override suspend fun getReferenceAccount(id: String): AppResult<ExpenseAccountDomain> =
        safeApiCall(mapError = Exception::toExpenseError) {
            val cacheKey = "${access.userId()}:${context.id}"
            accounts.get(key = cacheKey)?.firstOrNull { it.id == id } ?: readAccount(id = id)
        }

    private suspend fun readAccount(id: String): ExpenseAccountDomain {
        val cacheKey = "${access.userId()}:${context.id}"
        return loadAccount(id = id).also { accounts.put(key = cacheKey, value = listOf(it)) }
    }

    override suspend fun selectAccount(id: String): AppResult<Unit> = safeApiCall {
        PlatformStorage.put(selectionKey, id)
        selectedId.value = id
    }

    override suspend fun saveAccount(
        account: ExpenseAccountDomain,
    ): AppResult<ExpenseAccountDomain> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access
                .requireClient()
                .postgrest
                .rpc("save_expense_account", account.toRequest(householdId = context.id))
            val saved = loadAccount(id = account.id)
            accounts.clear()
            changes.update { oldVersion -> oldVersion + 1 }
            saved
        }

    private suspend fun loadAccount(id: String): ExpenseAccountDomain =
        access
            .requireClient()
            .from("expense_accounts_with_members")
            .select {
                filter {
                    eq("household_id", context.id)
                    eq("id", id)
                }
            }
            .decodeSingle<ExpenseAccountResponse>()
            .toDomain()

    override suspend fun deleteAccount(account: ExpenseAccountDomain): AppResult<Unit> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access
                .requireClient()
                .postgrest
                .rpc(
                    "delete_expense_account",
                    DeleteExpenseAccountRequest(
                        householdId = context.id,
                        id = account.id,
                        version = account.version,
                    ),
                )
            if (selectedId.value == account.id) {
                PlatformStorage.put(selectionKey, context.id)
                selectedId.value = context.id
            }
            accounts.clear()
            changes.update { oldVersion -> oldVersion + 1 }
            Unit
        }
}
