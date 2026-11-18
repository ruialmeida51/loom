package com.eyther.lumbridge.features.settings.data.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.eyther.lumbridge.core.data.local.PlatformStorage
import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.features.settings.domain.errors.SettingsError
import com.eyther.lumbridge.features.settings.domain.model.NotificationPermissionDomain
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

actual object PushPlatform {
    private var permissionOwner: Any? = null
    private var permissionRequester: (((Boolean) -> Unit) -> Unit)? = null

    fun attachPermissionRequester(owner: Any, requester: ((Boolean) -> Unit) -> Unit) {
        permissionOwner = owner
        permissionRequester = requester
    }

    fun detachPermissionRequester(owner: Any) {
        if (permissionOwner !== owner) return
        permissionOwner = null
        permissionRequester = null
    }

    actual suspend fun getPermission(): NotificationPermissionDomain {
        val context = PlatformStorage.applicationContext
        if (FirebaseApp.getApps(context).isEmpty()) return NotificationPermissionDomain.UNAVAILABLE
        val permissionGranted =
            Build.VERSION.SDK_INT < 33 ||
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        val notificationsEnabled =
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .areNotificationsEnabled()
        return when {
            permissionGranted && notificationsEnabled -> NotificationPermissionDomain.GRANTED
            permissionGranted && !notificationsEnabled ||
                PlatformStorage.get("notification-os-requested") == "true" ||
                Build.VERSION.SDK_INT < 33 -> NotificationPermissionDomain.DENIED
            else -> NotificationPermissionDomain.NOT_REQUESTED
        }
    }

    actual suspend fun openSettings(): Unit =
        withContext(Dispatchers.Main.immediate) {
            val context = PlatformStorage.applicationContext
            context.startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }

    actual suspend fun requestToken(): Pair<String, String> =
        withContext(Dispatchers.Main.immediate) {
            requireDomain(FirebaseApp.getApps(PlatformStorage.applicationContext).isNotEmpty()) {
                SettingsError.ProviderUnavailable()
            }
            val requester = permissionRequester ?: throw SettingsError.ProviderUnavailable()
            PlatformStorage.put("notification-os-requested", "true")
            val granted = suspendCancellableCoroutine { continuation ->
                requester { if (continuation.isActive) continuation.resume(it) }
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
