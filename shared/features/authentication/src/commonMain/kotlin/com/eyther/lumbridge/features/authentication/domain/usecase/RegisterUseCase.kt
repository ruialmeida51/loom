package com.eyther.lumbridge.features.authentication.domain.usecase

import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource

class RegisterUseCase(private val source: AuthenticationDataSource) {
    suspend operator fun invoke(email: String, password: String): AppResult<Unit> {
        if (!email.contains('@') || password.length < 12)
            return AppResult.Failure(
                "Use a valid email and a password with at least 12 characters.",
            )
        return source.register(email.trim(), password)
    }
}
