package com.eyther.lumbridge.features.settings.data.notifications

expect object PushPlatform {

    suspend fun requestToken(): Pair<String, String>
}
