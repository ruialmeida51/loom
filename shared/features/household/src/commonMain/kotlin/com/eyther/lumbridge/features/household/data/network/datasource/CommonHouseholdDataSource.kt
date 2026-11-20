package com.eyther.lumbridge.features.household.data.network.datasource

import com.eyther.lumbridge.core.data.local.ReferenceSnapshot
import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.asApiResult
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource
import com.eyther.lumbridge.features.household.data.network.mapper.requireJoined
import com.eyther.lumbridge.features.household.data.network.mapper.toDomain
import com.eyther.lumbridge.features.household.data.network.mapper.toHouseholdError
import com.eyther.lumbridge.features.household.data.network.model.request.CreateHouseholdRequest
import com.eyther.lumbridge.features.household.data.network.model.request.HouseholdRequest
import com.eyther.lumbridge.features.household.data.network.model.request.JoinHouseholdRequest
import com.eyther.lumbridge.features.household.data.network.model.response.HouseholdResponse
import com.eyther.lumbridge.features.household.data.network.model.response.JoinHouseholdResponse
import com.eyther.lumbridge.features.household.data.network.model.response.MemberResponse
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

@OptIn(ExperimentalCoroutinesApi::class)
class CommonHouseholdDataSource(
    private val access: SupabaseAccess,
    private val profileDataSource: ProfileDataSource,
) : HouseholdDataSource {
    private val members = ReferenceSnapshot<List<MemberDomain>>()

    override suspend fun getHouseholds(): AppResult<List<HouseholdDomain>> {
        return safeApiCall(
            mapError = Exception::toHouseholdError,
        ) {
            access.requireClient().from("households").select().decodeList<HouseholdResponse>().map {
                it.toDomain()
            }
        }
    }

    override fun streamHouseholds(): Flow<AppResult<List<HouseholdDomain>>> =
        access
            .watch {
                access
                    .requireClient()
                    .from("households")
                    .select()
                    .decodeList<HouseholdResponse>()
                    .map { it.toDomain() }
            }
            .asApiResult(mapError = Exception::toHouseholdError, retryTransientFailures = true)

    override fun streamMemberHistory(householdId: String): Flow<AppResult<List<MemberDomain>>> =
        access
            .watch {
                loadMemberHistory(householdId = householdId)
            }
            .asApiResult(mapError = Exception::toHouseholdError, retryTransientFailures = true)

    override suspend fun getMemberHistory(householdId: String): AppResult<List<MemberDomain>> =
        safeApiCall(mapError = Exception::toHouseholdError) {
            loadMemberHistory(householdId = householdId)
        }

    private suspend fun loadMemberHistory(householdId: String): List<MemberDomain> =
        access
            .requireClient()
            .postgrest
            .rpc("household_member_history", HouseholdRequest(householdId = householdId))
            .decodeList<MemberResponse>()
            .map { it.toDomain() }

    override suspend fun createHousehold(
        name: String,
        currency: String,
        timeZone: String,
    ): AppResult<HouseholdDomain> {
        return safeApiCall(
            mapError = Exception::toHouseholdError,
        ) {
            val request =
                CreateHouseholdRequest(
                    name = name,
                    currency = currency,
                    timeZone = timeZone,
                )
            val id =
                access.write { client ->
                    client.postgrest.rpc("create_household", request).decodeAs<String>()
                }

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

            access.write { client ->
                client
                    .postgrest
                    .rpc("redeem_household_invitation", request)
                    .decodeAs<JoinHouseholdResponse>()
                    .requireJoined()
            }
        }
    }

    override fun streamMembers(householdId: String): Flow<AppResult<List<MemberDomain>>> = flow {
        // SDK background callbacks temporarily remove the in-memory user. Resolve it
        // inside the cold, protected stream so opening a screen during resume cannot throw.
        val userId = access.userId()
        emitAll(
            profileDataSource.stream(userId = userId).flatMapLatest {
                access.watch {
                    loadMembers(householdId = householdId)
                }
            },
        )
    }
        .asApiResult(mapError = Exception::toHouseholdError, retryTransientFailures = true)

    override suspend fun getMembers(householdId: String): AppResult<List<MemberDomain>> =
        safeApiCall(mapError = Exception::toHouseholdError) {
            loadMembers(householdId = householdId)
        }

    override suspend fun getMemberReferences(householdId: String): AppResult<List<MemberDomain>> =
        safeApiCall(mapError = Exception::toHouseholdError) {
            members.get(key = "${access.userId()}:$householdId")
                ?: loadMembers(householdId = householdId)
        }

    private suspend fun loadMembers(householdId: String): List<MemberDomain> {
        val userId = access.userId()
        val request = HouseholdRequest(householdId = householdId)
        return access
            .requireClient()
            .postgrest
            .rpc("household_members_with_names", request)
            .decodeList<MemberResponse>()
            .map { it.toDomain() }
            .also { members.put(key = "$userId:$householdId", value = it) }
    }

    override suspend fun revokeInvitation(householdId: String): AppResult<Unit> =
        safeApiCall(mapError = Exception::toHouseholdError) {
            access.write { client ->
                client
                    .postgrest
                    .rpc("revoke_household_invitation", HouseholdRequest(householdId = householdId))
            }
            Unit
        }

    override suspend fun createInvitation(householdId: String): AppResult<String> {
        return safeApiCall(
            mapError = Exception::toHouseholdError,
        ) {
            val request = HouseholdRequest(householdId = householdId)

            access.write { client ->
                client
                    .postgrest
                    .rpc("create_short_household_invitation", request)
                    .decodeAs<String>()
            }
        }
    }
}
