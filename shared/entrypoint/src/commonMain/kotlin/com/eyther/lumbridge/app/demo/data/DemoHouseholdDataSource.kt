package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain

class DemoHouseholdDataSource(private val store: DemoHouseholdStore) : HouseholdDataSource {

    override suspend fun getHouseholds() =
        AppResult.Success(
            DemoHouseholdStore.households.map {
                HouseholdDomain(it.id, it.name, it.currency, it.timeZone)
            },
        )

    override suspend fun createHousehold(name: String, currency: String, timeZone: String) =
        AppResult.Success(HouseholdDomain(newId(), name, currency, timeZone))

    override suspend fun joinHousehold(token: String) = AppResult.Success(Unit)

    override suspend fun getMembers(householdId: String) = AppResult.Success(store.members)

    override suspend fun createInvitation(householdId: String) = AppResult.Success("PREVIEW-INVITATION")
}
