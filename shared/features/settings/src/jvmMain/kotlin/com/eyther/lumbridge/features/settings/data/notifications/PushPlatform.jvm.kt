package com.eyther.lumbridge.features.settings.data.notifications

actual object PushPlatform {
    actual suspend fun requestToken(): Pair<String, String> =
        error("Push notifications are available in the Android and iOS apps.")
}
