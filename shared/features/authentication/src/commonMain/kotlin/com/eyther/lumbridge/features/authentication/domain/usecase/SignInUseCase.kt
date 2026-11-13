package com.eyther.lumbridge.features.authentication.domain.usecase

import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource

class SignInUseCase(private val source: AuthenticationDataSource) {
    suspend operator fun invoke(email: String, password: String): AppResult<Unit> {
        if (!email.contains('@') || password.isBlank())
            return AppResult.Failure("Enter your email and password.")
        return source.signIn(email.trim(), password)
    }
}
