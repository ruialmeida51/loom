package com.eyther.lumbridge.features.settings.data.notifications

import com.eyther.lumbridge.features.settings.domain.errors.SettingsError
import com.eyther.lumbridge.features.settings.domain.model.NotificationPermissionDomain

actual object PushPlatform {
    actual suspend fun getPermission(): NotificationPermissionDomain =
        NotificationPermissionDomain.UNAVAILABLE

    actual suspend fun openSettings(): Unit = throw SettingsError.UnsupportedPlatform()

    actual suspend fun requestToken(): Pair<String, String> =
        throw SettingsError.UnsupportedPlatform()
}
