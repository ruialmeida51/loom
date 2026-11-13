package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.safeCall
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdSelectionDataSource

class RememberHouseholdUseCase(private val selection: HouseholdSelectionDataSource) {
    suspend operator fun invoke(userId: String, householdId: String) = safeCall {
        selection.select(userId, householdId)
    }
}
