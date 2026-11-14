package com.eyther.lumbridge.features.authentication.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.errors.AuthenticationError

class RegisterUseCase(private val source: AuthenticationDataSource) {

    suspend operator fun invoke(
        email: String,
        password: String,
        acceptedTerms: Boolean,
    ): AppResult<Unit> {
        if (!acceptedTerms) {
            return AppResult.Failure(error = AuthenticationError.TermsNotAccepted())
        }
        if (!email.contains('@') || password.length < 12) {
            return AppResult.Failure(error = AuthenticationError.InvalidRegistration())
        }

        return source.register(email = email.trim(), password = password)
    }
}
