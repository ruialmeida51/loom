package com.eyther.lumbridge.features.authentication.domain.usecase

import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource

/**
 * Best-effort foreground repair. Offline failures stay silent while SDK/read-stream retries
 * continue; confirmed revocation still arrives through the session stream. Never resets household
 * selection.
 */
class ResumeSessionUseCase(
    private val authenticationDataSource: AuthenticationDataSource,
) {
    suspend operator fun invoke() {
        authenticationDataSource.recoverSession()
    }
}
