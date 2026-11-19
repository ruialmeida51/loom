package com.eyther.lumbridge.features.household.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.model.HouseholdInvitationDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdManagementDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdMemberActionDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlinx.coroutines.flow.Flow

interface HouseholdManagementDataSource {
    fun stream(): Flow<AppResult<HouseholdManagementDomain>>

    suspend fun replaceInvitation(
        previous: HouseholdInvitationDomain?,
        requestId: String,
    ): AppResult<HouseholdInvitationDomain>

    suspend fun revokeInvitation(
        invitation: HouseholdInvitationDomain,
        requestId: String,
    ): AppResult<Unit>

    suspend fun changeMember(
        member: MemberDomain,
        action: HouseholdMemberActionDomain,
        requestId: String,
    ): AppResult<Unit>

    suspend fun deleteHousehold(name: String, requestId: String): AppResult<Unit>
}
