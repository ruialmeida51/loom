package com.eyther.lumbridge.features.account.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource

/** Sets the user's profile picture from picked image bytes, or removes it when [bytes] is null. */
class SetProfilePictureUseCase(private val profileDataSource: ProfileDataSource) {
    suspend operator fun invoke(userId: String, bytes: ByteArray?): AppResult<Unit> =
        profileDataSource.setPicture(userId = userId, bytes = bytes)
}
