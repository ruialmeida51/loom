package com.eyther.lumbridge.app.household.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

internal class HouseholdSetupFixtureDataSource : HouseholdDataSource {

    private val households = mutableListOf<HouseholdDomain>()

    override suspend fun getHouseholds(): AppResult<List<HouseholdDomain>> = AppResult.Success(households.toList())

    override suspend fun createHousehold(name: String, currency: String, timeZone: String): AppResult<HouseholdDomain> {
        val household = HouseholdDomain(id = "created-home", name = name, currency = currency, timeZone = timeZone)
        households += household
        return AppResult.Success(household)
    }

    override suspend fun joinHousehold(token: String): AppResult<Unit> {
        households += HouseholdDomain(id = "joined-home", name = "Joined home", currency = "EUR", timeZone = "UTC")
        return AppResult.Success(Unit)
    }

    override suspend fun getMembers(householdId: String): AppResult<List<MemberDomain>> = AppResult.Success(emptyList())

    override suspend fun createInvitation(householdId: String): AppResult<String> = AppResult.Success("test-invitation")
}
