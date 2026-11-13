package com.eyther.lumbridge.features.authentication.domain.usecase

import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource

class RegisterUseCase(private val source: AuthenticationDataSource) {
    suspend operator fun invoke(email: String, password: String): AppResult<Unit> {
        if (!email.contains('@') || password.length < 12)
            return AppResult.Failure(
                "authentication.message_use_a_valid_email_and_a_password_with_at",
            )
        return source.register(email.trim(), password)
    }
}
