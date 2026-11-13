package com.eyther.lumbridge.app

import androidx.compose.ui.window.ComposeUIViewController
import com.eyther.lumbridge.app.navigation.push.PendingPush
import com.eyther.lumbridge.features.settings.data.notifications.NativePushBridge

fun MainViewController() = ComposeUIViewController { LoomApp() }

fun registerApnsToken(token: String) {
    NativePushBridge.setToken(token, "apns")
}

fun openPush(householdId: String, route: String) {
    PendingPush.open(householdId, route)
}
