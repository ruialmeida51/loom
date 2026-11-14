package com.eyther.lumbridge.features.authentication.data.platform

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.data.network.validation.parseGoogleSignInCallback
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/** Retains a cold-start callback until the application authentication source is ready to consume it. */
object NativeAuthenticationBridge {

    private val pendingCallbacks = Channel<AppResult<String>>(capacity = Channel.CONFLATED)

    fun open(url: String) {
        val callback = parseGoogleSignInCallback(value = url) ?: return
        pendingCallbacks.trySend(callback)
    }

    fun stream(): Flow<AppResult<String>> = pendingCallbacks.receiveAsFlow()
}
