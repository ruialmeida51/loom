package com.eyther.lumbridge.features.account.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource
import com.eyther.lumbridge.features.account.domain.errors.AccountError
import com.eyther.lumbridge.features.account.domain.model.ProfileDomain

class UpdateNicknameUseCase(private val profileDataSource: ProfileDataSource) {
    suspend operator fun invoke(userId: String, nickname: String): AppResult<ProfileDomain> {
        val name = nickname.trim()
        if (name.length !in 1..80 || name.any { it.isISOControl() }) {
            return AppResult.Failure(AccountError.InvalidNickname())
        }
        return profileDataSource.updateNickname(userId = userId, nickname = name)
    }
}
