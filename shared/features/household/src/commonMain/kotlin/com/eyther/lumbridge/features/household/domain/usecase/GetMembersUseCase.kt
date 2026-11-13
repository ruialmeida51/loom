package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource

class GetMembersUseCase(private val source: HouseholdDataSource) {
    suspend operator fun invoke(id: String) = source.members(id)
}
