package com.eyther.lumbridge.features.authentication.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.domain.model.AuthSessionDomain
import kotlinx.coroutines.flow.StateFlow

interface AuthenticationDataSource {
    fun stream(): StateFlow<AuthSessionDomain?>
    val configured: Boolean

    suspend fun signIn(email: String, password: String): AppResult<Unit>

    suspend fun register(email: String, password: String): AppResult<Unit>

    suspend fun signOut(): AppResult<Unit>
}
