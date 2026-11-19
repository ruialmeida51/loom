package com.eyther.lumbridge.features.expenses.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.network.mapper.toDomain
import com.eyther.lumbridge.features.expenses.data.network.mapper.toExpenseError
import com.eyther.lumbridge.features.expenses.data.network.model.request.MemberAccountPositionRequest
import com.eyther.lumbridge.features.expenses.data.network.model.response.MemberAccountPositionResponse
import com.eyther.lumbridge.features.expenses.domain.datasource.MemberAccountPositionDataSource
import com.eyther.lumbridge.features.expenses.domain.model.MemberAccountPositionDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc

class CommonMemberAccountPositionDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : MemberAccountPositionDataSource {
    override suspend fun get(memberId: String): AppResult<List<MemberAccountPositionDomain>> =
        safeApiCall(mapError = Exception::toExpenseError) {
            access
                .requireClient()
                .postgrest
                .rpc(
                    "member_account_positions",
                    MemberAccountPositionRequest(householdId = context.id, memberId = memberId),
                )
                .decodeList<MemberAccountPositionResponse>()
                .map { it.toDomain() }
        }
}
