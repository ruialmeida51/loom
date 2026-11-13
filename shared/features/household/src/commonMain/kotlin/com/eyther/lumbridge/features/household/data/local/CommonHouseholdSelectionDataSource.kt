package com.eyther.lumbridge.features.household.data.local

import com.eyther.lumbridge.core.data.PlatformStorage
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdSelectionDataSource

class CommonHouseholdSelectionDataSource : HouseholdSelectionDataSource {
    override fun lastSelected(userId: String) = PlatformStorage.get("household-selection-$userId")

    override fun select(userId: String, householdId: String?) =
        PlatformStorage.put("household-selection-$userId", householdId)
}
