package com.eyther.lumbridge.features.account.domain.datasource

import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.domain.model.ProfileDomain
import kotlinx.coroutines.flow.Flow

interface ProfileDataSource {
    fun stream(userId: String): Flow<AppResult<ProfileDomain>>

    suspend fun updateNickname(userId: String, nickname: String): AppResult<ProfileDomain>

    /** Replaces the profile picture with [bytes], or removes it when null. */
    suspend fun setPicture(userId: String, bytes: ByteArray?): AppResult<Unit> =
        AppResult.Failure(CommonError.ServiceUnavailable())
}
