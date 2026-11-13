package com.eyther.lumbridge.features.settings.data.notifications

import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.UIKit.UIApplication
import platform.UIKit.registerForRemoteNotifications
import platform.UserNotifications.*
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

actual object PushPlatform {
    actual suspend fun requestToken(): Pair<String, String> =
        withContext(Dispatchers.Main.immediate) {
            val granted =
                suspendCancellableCoroutine<Boolean> { continuation ->
                    UNUserNotificationCenter.currentNotificationCenter()
                        .requestAuthorizationWithOptions(
                            UNAuthorizationOptionAlert or
                                UNAuthorizationOptionBadge or
                                UNAuthorizationOptionSound,
                        ) { allowed, _ ->
                            if (continuation.isActive) continuation.resume(allowed)
                        }
                }
            require(granted) {
                "Allow notifications in your phone’s settings to receive reminders."
            }
            dispatch_async(dispatch_get_main_queue()) {
                UIApplication.sharedApplication.registerForRemoteNotifications()
            }
            NativePushBridge.awaitToken()
        }
}
