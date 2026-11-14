package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain

class GetHouseholdsUseCase(
    private val householdDataSource: HouseholdDataSource,
) {

    suspend operator fun invoke(): AppResult<List<HouseholdDomain>> = householdDataSource.getHouseholds()
}
