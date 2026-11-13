package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

class GetMembersUseCase(
    private val householdDataSource: HouseholdDataSource,
) {

    suspend operator fun invoke(id: String): AppResult<List<MemberDomain>> = householdDataSource.getMembers(id)
}
