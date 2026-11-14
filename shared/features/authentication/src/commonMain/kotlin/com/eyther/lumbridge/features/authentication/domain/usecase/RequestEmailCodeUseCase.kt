package com.eyther.lumbridge.features.authentication.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.errors.AuthenticationError
import com.eyther.lumbridge.features.authentication.domain.model.EmailCodePurposeDomain

/** Keeps retries tied to the original signup or sign-in challenge. */
class RequestEmailCodeUseCase(
    private val authenticationDataSource: AuthenticationDataSource,
) {

    suspend operator fun invoke(email: String, purpose: EmailCodePurposeDomain): AppResult<Unit> {
        if (email.isBlank()) {
            return AppResult.Failure(AuthenticationError.MissingEmail())
        }

        return when (purpose) {
            EmailCodePurposeDomain.REGISTRATION -> authenticationDataSource.resendVerificationEmail(email = email.trim())
            EmailCodePurposeDomain.SIGN_IN -> authenticationDataSource.sendSignInCode(email = email.trim())
        }
    }
}
