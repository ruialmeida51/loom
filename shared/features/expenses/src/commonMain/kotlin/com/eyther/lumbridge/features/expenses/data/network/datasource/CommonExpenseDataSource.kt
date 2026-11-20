package com.eyther.lumbridge.features.expenses.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.asApiResult
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.network.mapper.toDomain
import com.eyther.lumbridge.features.expenses.data.network.mapper.toExpenseError
import com.eyther.lumbridge.features.expenses.data.network.mapper.toRemoveRequest
import com.eyther.lumbridge.features.expenses.data.network.mapper.toRequest
import com.eyther.lumbridge.features.expenses.data.network.mapper.toSaveRequest
import com.eyther.lumbridge.features.expenses.data.network.model.request.DeleteExpenseRequest
import com.eyther.lumbridge.features.expenses.data.network.model.response.ExpenseResponse
import com.eyther.lumbridge.features.expenses.data.network.model.response.SettlementResponse
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow

class CommonExpenseDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : ExpenseDataSource {
    override fun streamExpenses(accountId: String): Flow<AppResult<List<ExpenseDomain>>> =
        access
            .watch {
                access
                    .requireClient()
                    .from("account_expenses_with_shares")
                    .select {
                        filter {
                            eq("household_id", context.id)
                            eq("account_id", accountId.ifBlank { context.id })
                        }
                    }
                    .decodeList<ExpenseResponse>()
                    .map { it.toDomain() }
            }
            .asApiResult(mapError = Exception::toExpenseError, retryTransientFailures = true)

    override fun streamSettlements(accountId: String): Flow<AppResult<List<SettlementDomain>>> =
        access
            .watch {
                access
                    .requireClient()
                    .from("settlements")
                    .select {
                        filter {
                            eq("household_id", context.id)
                            eq("account_id", accountId.ifBlank { context.id })
                        }
                    }
                    .decodeList<SettlementResponse>()
                    .map { it.toDomain() }
            }
            .asApiResult(mapError = Exception::toExpenseError, retryTransientFailures = true)

    override suspend fun getExpense(id: String, accountId: String): AppResult<ExpenseDomain> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access
                .requireClient()
                .from("account_expenses_with_shares")
                .select {
                    filter {
                        eq("household_id", context.id)
                        eq("account_id", accountId)
                        eq("id", id)
                    }
                }
                .decodeSingle<ExpenseResponse>()
                .toDomain()
        }

    override suspend fun getSettlement(id: String, accountId: String): AppResult<SettlementDomain> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access
                .requireClient()
                .from("settlements")
                .select {
                    filter {
                        eq("household_id", context.id)
                        eq("account_id", accountId)
                        eq("id", id)
                    }
                }
                .decodeSingle<SettlementResponse>()
                .toDomain()
        }

    override suspend fun saveExpense(expense: ExpenseDomain): AppResult<Unit> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access.write { client ->
                client
                    .postgrest
                    .rpc("save_account_expense", expense.toRequest(householdId = context.id))
            }
            Unit
        }

    override suspend fun deleteExpense(expense: ExpenseDomain): AppResult<Unit> =
        safeApiCall(mapError = Exception::toExpenseError) {
            val request =
                DeleteExpenseRequest(
                    householdId = context.id,
                    accountId = expense.accountId,
                    id = expense.id,
                    version = expense.version,
                )
            access.write { client ->
                client.postgrest.rpc("delete_account_expense", request)
            }
            Unit
        }

    override suspend fun recordSettlement(settlement: SettlementDomain): AppResult<Unit> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access.write { client ->
                client
                    .postgrest
                    .rpc("record_account_settlement", settlement.toRequest(householdId = context.id))
            }
            Unit
        }

    override suspend fun saveSettlement(settlement: SettlementDomain): AppResult<SettlementDomain> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access.write { client ->
                client
                    .postgrest
                    .rpc(
                        "save_account_settlement",
                        settlement.toSaveRequest(householdId = context.id),
                    ) {
                        single()
                    }
                    .decodeAs<SettlementResponse>()
                    .toDomain()
            }
        }

    override suspend fun removeSettlement(settlement: SettlementDomain): AppResult<Unit> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access.write { client ->
                client
                    .postgrest
                    .rpc(
                        "delete_account_settlement",
                        settlement.toRemoveRequest(householdId = context.id),
                    )
            }
            Unit
        }
}
