package com.eyther.lumbridge.features.settings.data.notifications

actual object PushPlatform {
    actual suspend fun requestToken(): Pair<String, String> =
        error("settings.message_push_notifications_are_available_in_the_android_and_ios")
}
