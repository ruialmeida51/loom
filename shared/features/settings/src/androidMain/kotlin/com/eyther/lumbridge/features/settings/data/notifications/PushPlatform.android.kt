package com.eyther.lumbridge.features.settings.data.notifications

import com.eyther.lumbridge.core.data.PlatformStorage
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

actual object PushPlatform {
    var permissionRequester: (((Boolean) -> Unit) -> Unit)? = null

    actual suspend fun requestToken(): Pair<String, String> =
        withContext(Dispatchers.Main.immediate) {
            require(FirebaseApp.getApps(PlatformStorage.applicationContext).isNotEmpty()) {
                "Notifications aren’t available in this build yet."
            }
            val granted = suspendCancellableCoroutine { continuation ->
                val requester = permissionRequester
                if (requester == null) continuation.resume(false)
                else requester { if (continuation.isActive) continuation.resume(it) }
            }
            require(granted) {
                "Allow notifications in your phone’s settings to receive reminders."
            }
            val token =
                suspendCancellableCoroutine<String> { continuation ->
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (continuation.isActive) {
                            if (task.isSuccessful) continuation.resume(task.result)
                            else
                                continuation.resumeWithException(
                                    task.exception
                                        ?: IllegalStateException(
                                            "Couldn’t register notifications. Please try again.",
                                        ),
                                )
                        }
                    }
                }
            NativePushBridge.setToken(token, "fcm")
            token to "fcm"
        }
}
