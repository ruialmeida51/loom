package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdManagementDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdInvitationDomain

class RevokeHouseholdInvitationUseCase(
    private val householdManagementDataSource: HouseholdManagementDataSource,
) {
    suspend operator fun invoke(
        invitation: HouseholdInvitationDomain,
        requestId: String,
    ): AppResult<Unit> =
        householdManagementDataSource.revokeInvitation(
            invitation = invitation,
            requestId = requestId,
        )
}
