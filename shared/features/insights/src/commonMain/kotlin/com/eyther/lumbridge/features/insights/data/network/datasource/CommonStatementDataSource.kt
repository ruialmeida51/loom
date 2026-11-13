package com.eyther.lumbridge.features.insights.data.network.datasource

import com.eyther.lumbridge.core.data.SupabaseAccess
import com.eyther.lumbridge.core.data.safeApiCall
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.insights.domain.datasource.StatementDataSource
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.*

class CommonStatementDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : StatementDataSource {
    override suspend fun save(month: String) = safeApiCall {
        require(!context.demo) {
            "You can save monthly statements in your own household. The sample statement can be exported."
        }
        access
            .requireClient()
            .postgrest
            .rpc(
                "save_monthly_statement",
                buildJsonObject {
                    put("p_household_id", context.id)
                    put("p_month", "$month-01")
                },
            )
        Unit
    }
}
