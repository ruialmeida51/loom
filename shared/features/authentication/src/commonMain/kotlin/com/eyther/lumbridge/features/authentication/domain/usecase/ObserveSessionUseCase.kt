package com.eyther.lumbridge.features.authentication.domain.usecase

import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource

class ObserveSessionUseCase(private val source: AuthenticationDataSource) {
    operator fun invoke() = source.session
}
