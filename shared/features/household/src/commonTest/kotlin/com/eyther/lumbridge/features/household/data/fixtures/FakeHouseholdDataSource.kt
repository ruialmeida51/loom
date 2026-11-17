package com.eyther.lumbridge.features.household.data.fixtures

import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class FakeHouseholdDataSource : HouseholdDataSource {
    var members: List<MemberDomain> = emptyList()
    var invitationCode: String = "test-invitation"
    var revocationCalls = 0
    var revocationGate: CompletableDeferred<AppResult<Unit>>? = null
    var revocationResult: AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun getHouseholds(): AppResult<List<HouseholdDomain>> =
        AppResult.Success(emptyList())

    override suspend fun createHousehold(
        name: String,
        currency: String,
        timeZone: String,
    ): AppResult<HouseholdDomain> = AppResult.Failure(CommonError.Unknown())

    override suspend fun joinHousehold(token: String): AppResult<Unit> = AppResult.Success(Unit)

    override fun streamMembers(householdId: String): Flow<AppResult<List<MemberDomain>>> = flow {
        emit(getMembers(householdId = householdId))
    }

    override suspend fun getMembers(householdId: String): AppResult<List<MemberDomain>> =
        AppResult.Success(members)

    override suspend fun revokeInvitation(householdId: String): AppResult<Unit> {
        revocationCalls++
        return revocationGate?.await() ?: revocationResult
    }

    override suspend fun createInvitation(householdId: String): AppResult<String> =
        AppResult.Success(invitationCode)
}
