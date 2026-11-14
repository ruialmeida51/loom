package com.eyther.lumbridge.features.household.domain.datasource

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

    /** Nicknames identify the same person in all of their households. RLS limits writes to self. */
    suspend fun updateNickname(userId: String, nickname: String): AppResult<Unit>

    suspend fun createInvitation(householdId: String): AppResult<String>
}
