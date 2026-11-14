package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.errors.HouseholdError

class JoinHouseholdUseCase(
    private val householdDataSource: HouseholdDataSource,
) {

    suspend operator fun invoke(token: String): AppResult<Unit> {
        if (token.trim().length < 20)
            return AppResult.Failure(HouseholdError.InvalidInvitation())
        return householdDataSource.joinHousehold(token.trim())
    }
}
