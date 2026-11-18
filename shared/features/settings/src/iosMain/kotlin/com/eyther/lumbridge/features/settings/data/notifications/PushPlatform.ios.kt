package com.eyther.lumbridge.features.settings.data.notifications

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.features.settings.domain.errors.SettingsError
import com.eyther.lumbridge.features.settings.domain.model.NotificationPermissionDomain
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.registerForRemoteNotifications
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

actual object PushPlatform {
    actual suspend fun getPermission(): NotificationPermissionDomain =
        suspendCancellableCoroutine { continuation ->
            UNUserNotificationCenter.currentNotificationCenter()
                .getNotificationSettingsWithCompletionHandler { settings ->
                    val permission =
                        when (settings?.authorizationStatus) {
                            UNAuthorizationStatusNotDetermined ->
                                NotificationPermissionDomain.NOT_REQUESTED
                            UNAuthorizationStatusDenied -> NotificationPermissionDomain.DENIED
                            null -> NotificationPermissionDomain.UNAVAILABLE
                            else -> NotificationPermissionDomain.GRANTED
                        }
                    if (continuation.isActive) continuation.resume(permission)
                }
        }

    actual suspend fun openSettings(): Unit =
        withContext(Dispatchers.Main.immediate) {
            NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.let { url ->
                UIApplication.sharedApplication.openURL(
                    url,
                    options = emptyMap<Any?, Any>(),
                    completionHandler = null,
                )
            }
        }

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
            requireDomain(granted) {
                SettingsError.PermissionDenied()
            }
            dispatch_async(dispatch_get_main_queue()) {
                UIApplication.sharedApplication.registerForRemoteNotifications()
            }
            NativePushBridge.awaitToken()
        }
}
