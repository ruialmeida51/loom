package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlinx.coroutines.flow.Flow

class GetMembersStreamUseCase(
    private val householdDataSource: HouseholdDataSource,
) {

    operator fun invoke(
        householdId: String,
        includeInactive: Boolean = false,
    ): Flow<AppResult<List<MemberDomain>>> =
        if (includeInactive) householdDataSource.streamMemberHistory(householdId = householdId)
        else householdDataSource.streamMembers(householdId = householdId)
}
