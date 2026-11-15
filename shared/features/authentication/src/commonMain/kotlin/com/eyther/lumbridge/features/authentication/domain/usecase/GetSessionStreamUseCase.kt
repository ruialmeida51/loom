package com.eyther.lumbridge.features.authentication.domain.usecase

import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.model.AuthenticationStateDomain
import kotlinx.coroutines.flow.StateFlow

class GetSessionStreamUseCase(
    private val dataSource: AuthenticationDataSource,
) {

    operator fun invoke(): StateFlow<AuthenticationStateDomain> = dataSource.stream()
}
