package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdSelectionDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain

class RestoreHouseholdUseCase(
    private val selection: HouseholdSelectionDataSource,
    private val households: GetHouseholdsUseCase,
) {
    suspend operator fun invoke(userId: String): AppResult<HouseholdDomain?> = safeCall {
        val id = selection.lastSelected(userId) ?: return@safeCall null
        // Restore only a household still visible under the caller's current membership.
        val household = households().getOrThrow().firstOrNull { it.id == id }
        if (household == null) selection.select(userId, null)
        household
    }
}
