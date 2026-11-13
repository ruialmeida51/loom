package com.eyther.lumbridge.features.household.domain.datasource

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

interface HouseholdDataSource {
    suspend fun list(): AppResult<List<HouseholdDomain>>

    suspend fun create(name: String, currency: String, timeZone: String): AppResult<HouseholdDomain>

    suspend fun join(token: String): AppResult<Unit>

    suspend fun members(householdId: String): AppResult<List<MemberDomain>>

    suspend fun invite(householdId: String): AppResult<String>
}
