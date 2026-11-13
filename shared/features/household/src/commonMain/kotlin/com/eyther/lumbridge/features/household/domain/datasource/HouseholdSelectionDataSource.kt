package com.eyther.lumbridge.features.household.domain.datasource

interface HouseholdSelectionDataSource {
    fun lastSelected(userId: String): String?

    fun select(userId: String, householdId: String?)
}
