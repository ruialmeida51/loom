package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdSelectionDataSource

class RememberHouseholdUseCase(
    private val selectionDataSource: HouseholdSelectionDataSource,
) {

    suspend operator fun invoke(userId: String, householdId: String): AppResult<Unit> =
        selectionDataSource.select(userId = userId, householdId = householdId)
}
