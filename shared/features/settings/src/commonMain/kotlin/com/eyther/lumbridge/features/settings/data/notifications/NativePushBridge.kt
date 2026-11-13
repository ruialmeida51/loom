package com.eyther.lumbridge.features.settings.data.notifications

import com.eyther.lumbridge.core.data.local.PlatformStorage
import com.eyther.lumbridge.features.settings.domain.errors.SettingsError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/** iOS AppDelegate and Android's messaging service deliver native tokens here. */
object NativePushBridge {
    private val token =
        MutableStateFlow<Pair<String, String>?>(
            PlatformStorage.get("native-push-token")
                ?.split('|', limit = 2)
                ?.takeIf { it.size == 2 }
                ?.let { it[1] to it[0] },
        )
    var onTokenChanged: (() -> Unit)? = null

    fun currentToken(): Pair<String, String>? = token.value

    fun setToken(value: String, platform: String) {
        token.value = value to platform
        PlatformStorage.put("native-push-token", "$platform|$value")
        onTokenChanged?.invoke()
    }

    suspend fun awaitToken(): Pair<String, String> =
        withTimeoutOrNull(20_000) { token.first { it != null }!! }
            ?: throw SettingsError.RegistrationFailed()
}
