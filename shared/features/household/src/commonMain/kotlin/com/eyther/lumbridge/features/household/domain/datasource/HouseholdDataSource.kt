package com.eyther.lumbridge.features.household.domain.datasource

import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlinx.coroutines.flow.Flow

interface HouseholdDataSource {
    suspend fun getHouseholds(): AppResult<List<HouseholdDomain>>

    suspend fun createHousehold(
        name: String,
        currency: String,
        timeZone: String,
    ): AppResult<HouseholdDomain>

    suspend fun joinHousehold(token: String): AppResult<Unit>

    fun streamMembers(householdId: String): Flow<AppResult<List<MemberDomain>>>

    suspend fun getMembers(householdId: String): AppResult<List<MemberDomain>>

    /** Recent identities may seed capture; explicit member reads and retries remain fresh. */
    suspend fun getMemberReferences(householdId: String): AppResult<List<MemberDomain>> =
        getMembers(householdId = householdId)

    fun streamHouseholds(): Flow<AppResult<List<HouseholdDomain>>> =
        kotlinx.coroutines.flow.flow { emit(getHouseholds()) }

    fun streamMemberHistory(householdId: String): Flow<AppResult<List<MemberDomain>>> =
        streamMembers(householdId = householdId)

    suspend fun getMemberHistory(householdId: String): AppResult<List<MemberDomain>> =
        getMembers(householdId = householdId)

    /** Replaces the household picture with [bytes], or removes it when null. */
    suspend fun setPicture(householdId: String, bytes: ByteArray?): AppResult<Unit> =
        AppResult.Failure(CommonError.ServiceUnavailable())

    suspend fun revokeInvitation(householdId: String): AppResult<Unit>

    suspend fun createInvitation(householdId: String): AppResult<String>
}
