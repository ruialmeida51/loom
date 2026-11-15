package com.eyther.lumbridge.features.account.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource
import com.eyther.lumbridge.features.account.domain.model.ProfileDomain
import kotlinx.coroutines.flow.Flow

class GetProfileStreamUseCase(private val profileDataSource: ProfileDataSource) {
    operator fun invoke(userId: String): Flow<AppResult<ProfileDomain>> =
        profileDataSource.stream(userId = userId)
}
