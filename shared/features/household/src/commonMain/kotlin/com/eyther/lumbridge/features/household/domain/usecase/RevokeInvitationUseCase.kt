package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource

class RevokeInvitationUseCase(private val householdDataSource: HouseholdDataSource) {
    suspend operator fun invoke(householdId: String): AppResult<Unit> =
        householdDataSource.revokeInvitation(householdId = householdId)
}
