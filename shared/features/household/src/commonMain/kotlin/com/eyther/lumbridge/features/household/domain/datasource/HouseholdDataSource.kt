package com.eyther.lumbridge.features.household.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

interface HouseholdDataSource {
    suspend fun getHouseholds(): AppResult<List<HouseholdDomain>>

    suspend fun createHousehold(name: String, currency: String, timeZone: String): AppResult<HouseholdDomain>

    suspend fun joinHousehold(token: String): AppResult<Unit>

    suspend fun getMembers(householdId: String): AppResult<List<MemberDomain>>

    suspend fun createInvitation(householdId: String): AppResult<String>
}
