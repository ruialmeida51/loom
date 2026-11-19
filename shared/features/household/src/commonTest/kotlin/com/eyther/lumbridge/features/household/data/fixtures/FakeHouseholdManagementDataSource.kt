package com.eyther.lumbridge.features.household.data.fixtures

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdManagementDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdInvitationDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdManagementDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdMemberActionDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeHouseholdManagementDataSource : HouseholdManagementDataSource {
    val invitation =
        HouseholdInvitationDomain(
            id = "invite",
            version = 2,
            code = "ABC234",
            expiresAt = "2030-01-01T18:00:00Z",
            reusable = true,
        )
    val owner = MemberDomain(id = "you", name = "Rui", role = "owner")
    val member = MemberDomain(id = "alex", name = "Alex", role = "member")
    val snapshot =
        MutableStateFlow<AppResult<HouseholdManagementDomain>>(
            AppResult.Success(
                HouseholdManagementDomain(members = listOf(owner, member), invitation = invitation),
            ),
        )
    val revocations = mutableListOf<Pair<HouseholdInvitationDomain, String>>()
    val memberChanges = mutableListOf<Triple<MemberDomain, HouseholdMemberActionDomain, String>>()
    val deletions = mutableListOf<Pair<String, String>>()
    var result: AppResult<Unit> = AppResult.Success(Unit)
    var gate: CompletableDeferred<AppResult<Unit>>? = null

    override fun stream(): Flow<AppResult<HouseholdManagementDomain>> = snapshot

    override suspend fun replaceInvitation(
        previous: HouseholdInvitationDomain?,
        requestId: String,
    ): AppResult<HouseholdInvitationDomain> = AppResult.Success(invitation)

    override suspend fun revokeInvitation(
        invitation: HouseholdInvitationDomain,
        requestId: String,
    ): AppResult<Unit> {
        revocations += invitation to requestId
        val outcome = gate?.await() ?: result
        if (outcome is AppResult.Success)
            snapshot.value =
                AppResult.Success(
                    HouseholdManagementDomain(members = listOf(owner, member), invitation = null),
                )
        return outcome
    }

    override suspend fun changeMember(
        member: MemberDomain,
        action: HouseholdMemberActionDomain,
        requestId: String,
    ): AppResult<Unit> {
        memberChanges += Triple(member, action, requestId)
        return gate?.await() ?: result
    }

    override suspend fun deleteHousehold(name: String, requestId: String): AppResult<Unit> {
        deletions += name to requestId
        return gate?.await() ?: result
    }
}
