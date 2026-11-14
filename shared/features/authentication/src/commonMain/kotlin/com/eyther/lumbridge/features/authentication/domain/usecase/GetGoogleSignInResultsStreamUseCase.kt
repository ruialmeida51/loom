package com.eyther.lumbridge.features.authentication.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import kotlinx.coroutines.flow.Flow

class GetGoogleSignInResultsStreamUseCase(
    private val authenticationDataSource: AuthenticationDataSource,
) {

    operator fun invoke(): Flow<AppResult<Unit>> = authenticationDataSource.streamGoogleSignInResults()
}
