package com.eyther.lumbridge.features.household.data.network.datasource

import com.eyther.lumbridge.core.data.SupabaseAccess
import com.eyther.lumbridge.core.data.safeApiCall
import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.features.household.data.demo.DemoHousehold
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
private data class HouseholdDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("currency") val currency: String,
    @SerialName("time_zone") val timeZone: String,
)

@Serializable
private data class MemberDto(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val name: String,
    @SerialName("role") val role: String,
)

class CommonHouseholdDataSource(private val access: SupabaseAccess) : HouseholdDataSource {
    override suspend fun list() = safeApiCall {
        access.requireClient().from("households").select().decodeList<HouseholdDto>().map {
            HouseholdDomain(it.id, it.name, it.currency, it.timeZone)
        }
    }

    override suspend fun create(name: String, currency: String, timeZone: String) = safeApiCall {
        val id =
            access
                .requireClient()
                .postgrest
                .rpc(
                    "create_household",
                    buildJsonObject {
                        put("p_name", name)
                        put("p_currency", currency)
                        put("p_time_zone", timeZone)
                    },
                )
                .decodeAs<String>()
        HouseholdDomain(id, name, currency, timeZone)
    }

    override suspend fun join(token: String) = safeApiCall {
        access
            .requireClient()
            .postgrest
            .rpc("join_household", buildJsonObject { put("p_token", token) })
        Unit
    }

    override suspend fun members(householdId: String): AppResult<List<MemberDomain>> =
        if (householdId == DemoHousehold.ID)
            AppResult.Success(
                listOf(
                    MemberDomain(DemoHousehold.USER, "You", "owner"),
                    MemberDomain(DemoHousehold.ALEX, "Alex", "member"),
                    MemberDomain(DemoHousehold.SAM, "Sam", "member"),
                ),
            )
        else
            safeApiCall {
                access
                    .requireClient()
                    .postgrest
                    .rpc(
                        "household_members_with_names",
                        buildJsonObject { put("p_household_id", householdId) },
                    )
                    .decodeList<MemberDto>()
                    .map { MemberDomain(it.userId, it.name, it.role) }
            }

    override suspend fun invite(householdId: String) = safeApiCall {
        require(householdId != DemoHousehold.ID) {
            "Invitations are available in your own household."
        }
        access
            .requireClient()
            .postgrest
            .rpc("create_household_invite", buildJsonObject { put("p_household_id", householdId) })
            .decodeAs<String>()
    }
}
