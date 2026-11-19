package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdManagementDataSource

class DeleteHouseholdUseCase(
    private val householdManagementDataSource: HouseholdManagementDataSource,
) {
    suspend operator fun invoke(name: String, requestId: String): AppResult<Unit> =
        householdManagementDataSource.deleteHousehold(name = name, requestId = requestId)
}
