package com.eyther.lumbridge.features.household.domain

import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdSelectionDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RestoreHouseholdUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest

class RestoreHouseholdTest {

    private class Selection : HouseholdSelectionDataSource {

        val values = mutableMapOf<String, String>()

        override suspend fun getLastSelected(userId: String) = AppResult.Success(values[userId])

        override suspend fun select(userId: String, householdId: String?): AppResult<Unit> {
            if (householdId == null) values.remove(userId) else values[userId] = householdId
            return AppResult.Success(Unit)
        }
    }

    private class Households(val visible: List<HouseholdDomain>) : HouseholdDataSource {

        override suspend fun getHouseholds() = AppResult.Success(visible)

        override suspend fun createHousehold(name: String, currency: String, timeZone: String) =
            AppResult.Failure(CommonError.Unknown())

        override suspend fun joinHousehold(token: String) = AppResult.Success(Unit)

        override fun streamMembers(householdId: String): Flow<AppResult<List<MemberDomain>>> =
            flow {
                emit(getMembers(householdId = householdId))
            }

        override suspend fun getMembers(householdId: String) =
            AppResult.Success(emptyList<MemberDomain>())

        override suspend fun revokeInvitation(householdId: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun createInvitation(householdId: String) =
            AppResult.Failure(CommonError.Unknown())
    }

    @Test
    fun removedMembershipClearsTheLastSelection() = runTest {
        val selection = Selection().apply { select("alice", "old-home") }
        val result =
            RestoreHouseholdUseCase(selection, GetHouseholdsUseCase(Households(emptyList())))(
                    "alice",
                )
                .getOrThrow()
        assertNull(result)
        assertNull(selection.getLastSelected("alice").getOrThrow())
    }

    @Test
    fun selectionsArePerAccountAndRestoredFromCurrentData() = runTest {
        val selection =
            Selection().apply {
                select("alice", "home")
                select("bob", "other")
            }
        val household = HouseholdDomain("home", "Renamed household", "EUR", "UTC")
        val restore =
            RestoreHouseholdUseCase(
                selection,
                GetHouseholdsUseCase(Households(listOf(household))),
            )
        assertEquals(household, restore("alice").getOrThrow())
        assertEquals("other", selection.getLastSelected("bob").getOrThrow())
    }
}
