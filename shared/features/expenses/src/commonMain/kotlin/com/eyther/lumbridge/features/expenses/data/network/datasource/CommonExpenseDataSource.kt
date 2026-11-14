package com.eyther.lumbridge.features.expenses.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.asApiResult
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.network.mapper.toDomain
import com.eyther.lumbridge.features.expenses.data.network.mapper.toExpenseError
import com.eyther.lumbridge.features.expenses.data.network.mapper.toRequest
import com.eyther.lumbridge.features.expenses.data.network.model.response.ExpenseResponse
import com.eyther.lumbridge.features.expenses.data.network.model.response.SettlementResponse
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.household.data.network.selectHouseholdRows
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow

class CommonExpenseDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : ExpenseDataSource {

    override fun streamExpenses(): Flow<AppResult<List<ExpenseDomain>>> {
        return access.watch("expenses", context.id) {
            access.selectHouseholdRows<ExpenseResponse>("expenses_with_shares", context.id).map { it.toDomain() }
        }.asApiResult(mapError = Exception::toExpenseError)
    }

    override fun streamSettlements(): Flow<AppResult<List<SettlementDomain>>> {
        return access.watch("settlements", context.id) {
            access.selectHouseholdRows<SettlementResponse>("settlements", context.id).map { it.toDomain() }
        }.asApiResult(mapError = Exception::toExpenseError)
    }

    override suspend fun addExpense(expense: ExpenseDomain): AppResult<Unit> {
        return safeApiCall(
            mapError = Exception::toExpenseError,
        ) {
            val request = expense.toRequest(householdId = context.id)

            access.requireClient().postgrest.rpc("create_expense", request)
            Unit
        }
    }

    override suspend fun recordSettlement(settlement: SettlementDomain): AppResult<Unit> {
        return safeApiCall(
            mapError = Exception::toExpenseError,
        ) {
            val request = settlement.toRequest(householdId = context.id)

            access.requireClient().postgrest.rpc("record_settlement", request)
            Unit
        }
    }
}
