package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdManagementDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdManagementDomain
import kotlinx.coroutines.flow.Flow

class GetHouseholdManagementStreamUseCase(
    private val householdManagementDataSource: HouseholdManagementDataSource,
) {
    operator fun invoke(): Flow<AppResult<HouseholdManagementDomain>> =
        householdManagementDataSource.stream()
}
