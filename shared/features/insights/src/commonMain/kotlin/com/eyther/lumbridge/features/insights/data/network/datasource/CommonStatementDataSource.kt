package com.eyther.lumbridge.features.insights.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.insights.data.network.model.request.SaveStatementRequest
import com.eyther.lumbridge.features.insights.domain.datasource.StatementDataSource
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc

class CommonStatementDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : StatementDataSource {

    override suspend fun saveStatement(month: String, accountId: String): AppResult<Unit> {
        return safeApiCall {
            val request =
                SaveStatementRequest(
                    householdId = context.id,
                    accountId = accountId,
                    month = "$month-01",
                )

            access.write { client ->
                client.postgrest.rpc("save_account_statement", request)
            }
            Unit
        }
    }
}
