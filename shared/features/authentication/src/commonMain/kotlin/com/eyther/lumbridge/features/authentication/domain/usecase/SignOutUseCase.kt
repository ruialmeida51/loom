package com.eyther.lumbridge.features.authentication.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource

class SignOutUseCase(private val source: AuthenticationDataSource) {

    suspend operator fun invoke(): AppResult<Unit> = source.signOut()
}
