package com.eyther.lumbridge.features.tasks.data.fixtures

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersStreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** The household's people for task screens; only the member stream is used. */
internal class MemberFixtureDataSource(
    members: List<MemberDomain> =
        listOf(MemberDomain("you", "You", "owner"), MemberDomain("alex", "Alex", "member")),
) : HouseholdDataSource {
    val members = MutableStateFlow(members)

    val useCase = GetMembersStreamUseCase(householdDataSource = this)

    override fun streamMembers(householdId: String): Flow<AppResult<List<MemberDomain>>> =
        members.map { AppResult.Success(it) }

    override suspend fun getMembers(householdId: String): AppResult<List<MemberDomain>> =
        AppResult.Success(members.value)

    override suspend fun getHouseholds(): AppResult<List<HouseholdDomain>> = error("Not used")

    override suspend fun createHousehold(
        name: String,
        currency: String,
        timeZone: String,
    ): AppResult<HouseholdDomain> = error("Not used")

    override suspend fun joinHousehold(token: String): AppResult<Unit> = error("Not used")

    override suspend fun revokeInvitation(householdId: String): AppResult<Unit> = error("Not used")

    override suspend fun createInvitation(householdId: String): AppResult<String> =
        error("Not used")
}
