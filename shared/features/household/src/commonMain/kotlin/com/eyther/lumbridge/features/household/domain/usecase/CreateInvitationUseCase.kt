package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource

class CreateInvitationUseCase(private val source: HouseholdDataSource) {
    suspend operator fun invoke(id: String) = source.invite(id)
}
