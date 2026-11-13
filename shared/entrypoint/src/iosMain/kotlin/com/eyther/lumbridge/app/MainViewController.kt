package com.eyther.lumbridge.app

import androidx.compose.ui.window.ComposeUIViewController
import com.eyther.lumbridge.core.data.NativePushBridge

fun MainViewController() = ComposeUIViewController { LoomApp() }

fun registerApnsToken(token: String) {
    NativePushBridge.setToken(token, "apns")
}

fun openPush(householdId: String, route: String) {
    com.eyther.lumbridge.core.data.PendingPush.open(householdId, route)
}
