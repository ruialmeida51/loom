package com.eyther.lumbridge.features.authentication.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource

class SignInWithGoogleUseCase(
    private val authenticationDataSource: AuthenticationDataSource,
) {

    suspend operator fun invoke(): AppResult<Unit> = authenticationDataSource.signInWithGoogle()
}
