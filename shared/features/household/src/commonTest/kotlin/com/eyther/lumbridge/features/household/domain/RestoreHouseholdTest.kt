package com.eyther.lumbridge.features.household.domain

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.household.domain.datasource.*
import com.eyther.lumbridge.features.household.domain.model.*
import com.eyther.lumbridge.features.household.domain.usecase.*
import kotlin.test.*

class RestoreHouseholdTest {
    private class Selection : HouseholdSelectionDataSource {
        val values = mutableMapOf<String, String>()

        override fun lastSelected(userId: String) = values[userId]

        override fun select(userId: String, householdId: String?) {
            if (householdId == null) values.remove(userId) else values[userId] = householdId
        }
    }

    private class Households(val visible: List<HouseholdDomain>) : HouseholdDataSource {
        override suspend fun list() = AppResult.Success(visible)

        override suspend fun create(name: String, currency: String, timeZone: String) =
            AppResult.Failure("unused")

        override suspend fun join(token: String) = AppResult.Success(Unit)

        override suspend fun members(householdId: String) =
            AppResult.Success(emptyList<MemberDomain>())

        override suspend fun invite(householdId: String) = AppResult.Failure("unused")
    }

    @Test
    fun removedMembershipClearsTheLastSelection() =
        kotlinx.coroutines.test.runTest {
            val selection = Selection().apply { select("alice", "old-home") }
            val result =
                RestoreHouseholdUseCase(selection, GetHouseholdsUseCase(Households(emptyList())))(
                        "alice",
                    )
                    .getOrThrow()
            assertNull(result)
            assertNull(selection.lastSelected("alice"))
        }

    @Test
    fun selectionsArePerAccountAndRestoredFromCurrentData() =
        kotlinx.coroutines.test.runTest {
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
            assertEquals("other", selection.lastSelected("bob"))
        }
}
