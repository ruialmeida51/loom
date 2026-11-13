package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource

class GetHouseholdsUseCase(private val source: HouseholdDataSource) {
    suspend operator fun invoke() = source.list()
}
