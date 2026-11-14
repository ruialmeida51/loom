package com.eyther.lumbridge.features.household.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.errors.HouseholdError

class UpdateMemberNicknameUseCase(private val householdDataSource: HouseholdDataSource) {

    suspend operator fun invoke(userId: String, nickname: String): AppResult<Unit> {
        val name = nickname.trim()
        if (name.length !in 1..80 || name.any { it.isISOControl() }) {
            return AppResult.Failure(HouseholdError.InvalidNickname())
        }
        return householdDataSource.updateNickname(userId = userId, nickname = name)
    }
}
