package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.InvitationCodeDomain

class JoinHouseholdUseCase(
    private val householdDataSource: HouseholdDataSource,
) {

    suspend operator fun invoke(token: String): AppResult<Unit> = safeCall {
        householdDataSource
            .joinHousehold(token = InvitationCodeDomain.normalized(token))
            .getOrThrow()
    }
}
