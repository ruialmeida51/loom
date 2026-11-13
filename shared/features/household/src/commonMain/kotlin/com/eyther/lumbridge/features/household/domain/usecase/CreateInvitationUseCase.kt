package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource

class CreateInvitationUseCase(
    private val householdDataSource: HouseholdDataSource,
) {

    suspend operator fun invoke(id: String): AppResult<String> = householdDataSource.createInvitation(id)
}
