package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DemoHouseholdDataSource(private val store: DemoHouseholdStore) : HouseholdDataSource {

    override suspend fun getHouseholds() =
        AppResult.Success(
            DemoHouseholdStore.households.map {
                HouseholdDomain(it.id, it.name, it.currency, it.timeZone)
            },
        )

    // Household pictures change in this preview, so the list follows the store's picture.
    override fun streamHouseholds(): Flow<AppResult<List<HouseholdDomain>>> =
        store.picturePath.map { picture ->
            AppResult.Success(
                DemoHouseholdStore.households.map {
                    HouseholdDomain(
                        id = it.id,
                        name = it.name,
                        currency = it.currency,
                        timeZone = it.timeZone,
                        imagePath = picture.takeIf { _ -> it.id == store.context.id },
                    )
                },
            )
        }

    override suspend fun createHousehold(name: String, currency: String, timeZone: String) =
        AppResult.Success(HouseholdDomain(newId(), name, currency, timeZone))

    override suspend fun joinHousehold(token: String) = AppResult.Success(Unit)

    override fun streamMembers(householdId: String): Flow<AppResult<List<MemberDomain>>> =
        store.memberUpdates.map { AppResult.Success(it.filter { member -> member.active }) }

    override suspend fun getMembers(householdId: String) =
        AppResult.Success(store.members.filter { it.active })

    override fun streamMemberHistory(householdId: String): Flow<AppResult<List<MemberDomain>>> =
        store.memberUpdates.map { AppResult.Success(it) }

    override suspend fun getMemberHistory(householdId: String): AppResult<List<MemberDomain>> =
        AppResult.Success(store.members)

    override suspend fun setPicture(householdId: String, bytes: ByteArray?): AppResult<Unit> =
        safeCall {
            store.picturePath.value =
                DemoPictures.replace(
                    folder = "households/$householdId",
                    bytes = bytes,
                    previous = store.picturePath.value,
                )
        }

    override suspend fun revokeInvitation(householdId: String) = AppResult.Success(Unit)

    override suspend fun createInvitation(householdId: String) = AppResult.Success("ABC234")
}
