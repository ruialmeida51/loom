package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource

class JoinHouseholdUseCase(private val source: HouseholdDataSource) {
    suspend operator fun invoke(token: String): AppResult<Unit> {
        if (token.trim().length < 20)
            return AppResult.Failure("household.message_enter_the_invitation_code_you_received")
        return source.join(token.trim())
    }
}
