package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource

/** Sets the household picture from picked image bytes, or removes it when [bytes] is null. */
class SetHouseholdPictureUseCase(private val householdDataSource: HouseholdDataSource) {
    suspend operator fun invoke(householdId: String, bytes: ByteArray?): AppResult<Unit> =
        householdDataSource.setPicture(householdId = householdId, bytes = bytes)
}
