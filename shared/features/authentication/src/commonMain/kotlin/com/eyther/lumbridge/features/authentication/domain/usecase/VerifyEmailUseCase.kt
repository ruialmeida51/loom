package com.eyther.lumbridge.features.authentication.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.errors.AuthenticationError

class VerifyEmailUseCase(
    private val authenticationDataSource: AuthenticationDataSource,
) {

    suspend operator fun invoke(email: String, code: String): AppResult<Unit> {
        val token = code.trim()
        if (email.isBlank()) {
            return AppResult.Failure(AuthenticationError.MissingEmail())
        }
        if (token.isEmpty() || token.any { it !in '0'..'9' }) {
            return AppResult.Failure(AuthenticationError.InvalidVerificationCode())
        }

        return authenticationDataSource.verifyEmail(email = email.trim(), code = token)
    }
}
