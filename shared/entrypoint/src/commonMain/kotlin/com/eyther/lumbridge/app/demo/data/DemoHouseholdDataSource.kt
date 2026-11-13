package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain

class DemoHouseholdDataSource(private val store: DemoHouseholdStore) : HouseholdDataSource {
    override suspend fun list() =
        AppResult.Success(
            DemoHouseholdStore.households.map {
                HouseholdDomain(it.id, it.name, it.currency, it.timeZone)
            },
        )

    override suspend fun create(name: String, currency: String, timeZone: String) =
        AppResult.Success(HouseholdDomain(newId(), name, currency, timeZone))

    override suspend fun join(token: String) = AppResult.Success(Unit)

    override suspend fun members(householdId: String) = AppResult.Success(store.members)

    override suspend fun invite(householdId: String) = AppResult.Success("PREVIEW-INVITATION")
}
