package com.eyther.lumbridge.features.household.data.local

import com.eyther.lumbridge.core.data.local.PlatformStorage
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdSelectionDataSource

class CommonHouseholdSelectionDataSource : HouseholdSelectionDataSource {

    override suspend fun getLastSelected(userId: String): AppResult<String?> = safeApiCall(
        mapError = { CommonError.LocalStorage(cause = it) },
    ) {
        PlatformStorage.get("household-selection-$userId")
    }

    override suspend fun select(userId: String, householdId: String?): AppResult<Unit> = safeApiCall(
        mapError = { CommonError.LocalStorage(cause = it) },
    ) {
        PlatformStorage.put("household-selection-$userId", householdId)
    }
}
