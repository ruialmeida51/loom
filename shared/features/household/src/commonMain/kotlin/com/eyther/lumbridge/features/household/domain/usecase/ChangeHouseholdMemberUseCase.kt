package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdManagementDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdMemberActionDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

class ChangeHouseholdMemberUseCase(
    private val householdManagementDataSource: HouseholdManagementDataSource,
) {
    suspend operator fun invoke(
        member: MemberDomain,
        action: HouseholdMemberActionDomain,
        requestId: String,
    ): AppResult<Unit> =
        householdManagementDataSource.changeMember(
            member = member,
            action = action,
            requestId = requestId,
        )
}
