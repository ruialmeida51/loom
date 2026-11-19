package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdManagementDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdInvitationDomain

class ReplaceHouseholdInvitationUseCase(
    private val householdManagementDataSource: HouseholdManagementDataSource,
) {
    suspend operator fun invoke(
        previous: HouseholdInvitationDomain?,
        requestId: String,
    ): AppResult<HouseholdInvitationDomain> =
        householdManagementDataSource.replaceInvitation(previous = previous, requestId = requestId)
}
