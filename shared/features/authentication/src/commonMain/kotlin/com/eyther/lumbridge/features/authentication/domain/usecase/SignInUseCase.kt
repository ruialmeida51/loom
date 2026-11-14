package com.eyther.lumbridge.features.authentication.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.errors.AuthenticationError

class SignInUseCase(
    private val authenticationDataSource: AuthenticationDataSource,
) {

    suspend operator fun invoke(email: String, password: String): AppResult<Unit> {
        if (email.isBlank() || password.isBlank()) {
            return AppResult.Failure(error = AuthenticationError.MissingCredentials())
        }

        return authenticationDataSource.signIn(email = email.trim(), password = password)
    }
}
