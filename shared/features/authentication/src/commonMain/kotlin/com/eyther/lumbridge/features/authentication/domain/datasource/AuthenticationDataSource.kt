package com.eyther.lumbridge.features.authentication.domain.datasource

import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.features.authentication.domain.model.AuthSessionDomain
import kotlinx.coroutines.flow.StateFlow

interface AuthenticationDataSource {
    val session: StateFlow<AuthSessionDomain?>
    val configured: Boolean

    suspend fun signIn(email: String, password: String): AppResult<Unit>

    suspend fun register(email: String, password: String): AppResult<Unit>

    suspend fun signOut(): AppResult<Unit>

    fun startDemo()
}
