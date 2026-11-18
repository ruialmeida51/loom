package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdSelectionDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class RestoreHouseholdUseCase(
    private val selectionDataSource: HouseholdSelectionDataSource,
    private val getHouseholdsUseCase: GetHouseholdsUseCase,
) {

    suspend operator fun invoke(userId: String): AppResult<HouseholdDomain?> = safeCall {
        val selectedId =
            selectionDataSource.getLastSelected(userId).getOrThrow() ?: return@safeCall null
        val households = getHouseholdsUseCase().getOrThrow()
        currentCoroutineContext().ensureActive()
        val household = households.firstOrNull { it.id == selectedId }

        if (household == null) {
            selectionDataSource.select(userId = userId, householdId = null).getOrThrow()
        }

        household
    }
}
