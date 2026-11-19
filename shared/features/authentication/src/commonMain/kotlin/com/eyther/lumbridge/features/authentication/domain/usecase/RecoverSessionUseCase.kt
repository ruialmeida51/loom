package com.eyther.lumbridge.features.authentication.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource

/** Retries recovery; the session stream remains the authority for entering or leaving the app. */
class RecoverSessionUseCase(
    private val authenticationDataSource: AuthenticationDataSource,
) {
    suspend operator fun invoke(): AppResult<Unit> = authenticationDataSource.recoverSession()
}
