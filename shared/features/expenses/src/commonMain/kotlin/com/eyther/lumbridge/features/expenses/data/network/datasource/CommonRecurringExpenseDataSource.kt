package com.eyther.lumbridge.features.expenses.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.asApiResult
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.network.mapper.toDomain
import com.eyther.lumbridge.features.expenses.data.network.mapper.toExpenseError
import com.eyther.lumbridge.features.expenses.data.network.model.request.ControlRecurringExpenseRequest
import com.eyther.lumbridge.features.expenses.data.network.model.response.RecurringExpenseResponse
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
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
