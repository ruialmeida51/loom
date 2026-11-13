package com.eyther.lumbridge.features.authentication.domain.usecase

import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.model.AuthSessionDomain
import kotlinx.coroutines.flow.StateFlow

class GetSessionStreamUseCase(
    private val dataSource: AuthenticationDataSource,
) {

    operator fun invoke(): StateFlow<AuthSessionDomain?> = dataSource.stream()
}
