package com.eyther.lumbridge.features.settings.data.notifications

import com.eyther.lumbridge.features.settings.domain.model.NotificationPermissionDomain

expect object PushPlatform {

    suspend fun requestToken(): Pair<String, String>

    suspend fun getPermission(): NotificationPermissionDomain

    suspend fun openSettings()
}
