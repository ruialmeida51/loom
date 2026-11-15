package com.eyther.lumbridge.features.account.data.fixtures

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource
import com.eyther.lumbridge.features.account.domain.model.ProfileDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeProfileDataSource : ProfileDataSource {
    var savedNickname: Pair<String, String>? = null
    var failure: AppResult.Failure? = null
    val profile =
        MutableStateFlow<AppResult<ProfileDomain>>(
            AppResult.Success(ProfileDomain(id = "member", nickname = "Original")),
        )

    override fun stream(userId: String): Flow<AppResult<ProfileDomain>> = profile

    override suspend fun updateNickname(
        userId: String,
        nickname: String,
    ): AppResult<ProfileDomain> {
        savedNickname = userId to nickname
        return failure
            ?: AppResult.Success(ProfileDomain(id = userId, nickname = nickname)).also {
                profile.value = it
            }
    }
}
