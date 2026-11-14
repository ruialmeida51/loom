package com.eyther.lumbridge.core.data.local

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.KeychainSettings

@OptIn(ExperimentalSettingsApi::class)
actual object PlatformStorage {
    private val settings = KeychainSettings(service = "com.eyther.lumbridge")

    actual fun get(key: String): String? = settings.getStringOrNull(key)

    actual fun put(key: String, value: String?) {
        if (value == null) settings.remove(key) else settings.putString(key, value)
    }
}
