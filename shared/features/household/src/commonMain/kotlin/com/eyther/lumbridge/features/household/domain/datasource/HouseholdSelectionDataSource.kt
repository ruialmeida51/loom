package com.eyther.lumbridge.features.household.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult

interface HouseholdSelectionDataSource {

    suspend fun getLastSelected(userId: String): AppResult<String?>

    suspend fun select(userId: String, householdId: String?): AppResult<Unit>
}
