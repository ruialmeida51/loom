package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import kotlinx.coroutines.flow.Flow

class GetHouseholdsStreamUseCase(private val householdDataSource: HouseholdDataSource) {
    operator fun invoke(): Flow<AppResult<List<HouseholdDomain>>> =
        householdDataSource.streamHouseholds()
}
