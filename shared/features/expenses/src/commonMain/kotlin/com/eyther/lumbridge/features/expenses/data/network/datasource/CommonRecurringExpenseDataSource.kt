package com.eyther.lumbridge.features.expenses.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.asApiResult
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.network.mapper.toDomain
import com.eyther.lumbridge.features.expenses.data.network.mapper.toExpenseError
import com.eyther.lumbridge.features.expenses.data.network.mapper.toSaveRequest
import com.eyther.lumbridge.features.expenses.data.network.model.request.ControlRecurringExpenseRequest
import com.eyther.lumbridge.features.expenses.data.network.model.request.DeleteRecurringExpenseRequest
import com.eyther.lumbridge.features.expenses.data.network.model.response.RecurringExpenseResponse
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.RecurringExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseActionDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow

class CommonRecurringExpenseDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : RecurringExpenseDataSource {

    override fun stream(accountId: String): Flow<AppResult<List<RecurringExpenseDomain>>> {
        return access
            .watch("recurring_expenses", context.id) {
                access
                    .requireClient()
                    .from("recurring_expenses")
                    .select {
                        filter {
                            eq("household_id", context.id)
                            eq("account_id", accountId.ifBlank { context.id })
                        }
                    }
                    .decodeList<RecurringExpenseResponse>()
                    .map { it.toDomain() }
            }
            .asApiResult(mapError = Exception::toExpenseError, retryTransientFailures = true)
    }

    override suspend fun getRule(id: String, accountId: String): AppResult<RecurringExpenseDomain> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access
                .requireClient()
                .from("recurring_expenses")
                .select {
                    filter {
                        eq("household_id", context.id)
                        eq("account_id", accountId)
                        eq("id", id)
                    }
                }
                .decodeList<RecurringExpenseResponse>()
                .firstOrNull()
                ?.toDomain() ?: throw RecurringExpenseError.NotFound()
        }

    override suspend fun save(rule: RecurringExpenseDomain): AppResult<RecurringExpenseDomain> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access
                .requireClient()
                .postgrest
                .rpc("save_recurring_expense", rule.toSaveRequest(householdId = context.id))
                .decodeAs<RecurringExpenseResponse>()
                .toDomain()
        }

    override suspend fun delete(rule: RecurringExpenseDomain): AppResult<Unit> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access
                .requireClient()
                .postgrest
                .rpc(
                    "delete_recurring_expense",
                    DeleteRecurringExpenseRequest(
                        householdId = context.id,
                        id = rule.id,
                        version = rule.version,
                    ),
                )
            Unit
        }

    override suspend fun controlSchedule(
        rule: RecurringExpenseDomain,
        action: RecurringExpenseActionDomain,
    ): AppResult<RecurringExpenseDomain> {
        return safeApiCall(
            mapError = Exception::toExpenseError,
        ) {
            val request =
                ControlRecurringExpenseRequest(
                    householdId = context.id,
                    id = rule.id,
                    action = action.value,
                    version = rule.version,
                )

            access
                .requireClient()
                .postgrest
                .rpc("control_recurring_expense", request)
                .decodeAs<RecurringExpenseResponse>()
                .toDomain()
        }
    }
}
