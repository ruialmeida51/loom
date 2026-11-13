package com.eyther.lumbridge.features.household.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.data.network.mapper.toDomain
import com.eyther.lumbridge.features.household.data.network.mapper.toHouseholdError
import com.eyther.lumbridge.features.household.data.network.model.request.CreateHouseholdRequest
import com.eyther.lumbridge.features.household.data.network.model.request.HouseholdRequest
import com.eyther.lumbridge.features.household.data.network.model.request.JoinHouseholdRequest
import com.eyther.lumbridge.features.household.data.network.model.response.HouseholdResponse
import com.eyther.lumbridge.features.household.data.network.model.response.MemberResponse
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc

class CommonHouseholdDataSource(
    private val access: SupabaseAccess,
) : HouseholdDataSource {

    override suspend fun getHouseholds(): AppResult<List<HouseholdDomain>> {
        return safeApiCall(
            mapError = Exception::toHouseholdError,
        ) {
            access.requireClient().from("households").select()
                .decodeList<HouseholdResponse>()
                .map { it.toDomain() }
        }
    }

    override suspend fun createHousehold(
        name: String,
        currency: String,
        timeZone: String,
    ): AppResult<HouseholdDomain> {
        return safeApiCall(
            mapError = Exception::toHouseholdError,
        ) {
            val request = CreateHouseholdRequest(
                name = name,
                currency = currency,
                timeZone = timeZone,
            )
            val id = access.requireClient().postgrest.rpc("create_household", request).decodeAs<String>()

            HouseholdDomain(
                id = id,
                name = name,
                currency = currency,
                timeZone = timeZone,
            )
        }
    }

    override suspend fun joinHousehold(token: String): AppResult<Unit> {
        return safeApiCall(
            mapError = Exception::toHouseholdError,
        ) {
            val request = JoinHouseholdRequest(token = token)

            access.requireClient().postgrest.rpc("join_household", request)
            Unit
        }
    }

    override suspend fun getMembers(householdId: String): AppResult<List<MemberDomain>> {
        return safeApiCall(
            mapError = Exception::toHouseholdError,
        ) {
            val request = HouseholdRequest(householdId = householdId)

            access.requireClient().postgrest.rpc("household_members_with_names", request)
                .decodeList<MemberResponse>()
                .map { it.toDomain() }
        }
    }

    override suspend fun createInvitation(householdId: String): AppResult<String> {
        return safeApiCall(
            mapError = Exception::toHouseholdError,
        ) {
            val request = HouseholdRequest(householdId = householdId)

            access.requireClient().postgrest.rpc("create_household_invite", request).decodeAs<String>()
        }
    }
}
