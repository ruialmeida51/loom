package com.eyther.lumbridge.features.settings.data.notifications

import com.eyther.lumbridge.core.data.local.PlatformStorage
import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.features.settings.domain.errors.SettingsError
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
            requireDomain(FirebaseApp.getApps(PlatformStorage.applicationContext).isNotEmpty()) {
                SettingsError.ProviderUnavailable()
            }
            val granted = suspendCancellableCoroutine { continuation ->
                val requester = permissionRequester
                if (requester == null) continuation.resume(false)
                else requester { if (continuation.isActive) continuation.resume(it) }
            }
            requireDomain(granted) {
                SettingsError.PermissionDenied()
            }
            val token =
                suspendCancellableCoroutine<String> { continuation ->
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (continuation.isActive) {
                            if (task.isSuccessful) continuation.resume(task.result)
                            else
                                continuation.resumeWithException(
                                    SettingsError.RegistrationFailed(cause = task.exception),
                                )
                        }
                    }
                }
            NativePushBridge.setToken(token, "fcm")
            token to "fcm"
        }
}
