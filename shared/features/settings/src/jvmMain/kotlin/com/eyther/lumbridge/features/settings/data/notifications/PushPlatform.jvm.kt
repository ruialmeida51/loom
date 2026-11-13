package com.eyther.lumbridge.features.settings.data.notifications

import com.eyther.lumbridge.features.settings.domain.errors.SettingsError

actual object PushPlatform {
    actual suspend fun requestToken(): Pair<String, String> =
        throw SettingsError.UnsupportedPlatform()
}
