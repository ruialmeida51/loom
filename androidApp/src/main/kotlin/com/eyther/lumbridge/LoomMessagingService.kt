package com.eyther.lumbridge

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.eyther.lumbridge.core.data.NativePushBridge
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class LoomMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        NativePushBridge.setToken(token, "fcm")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (
            Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED
        )
            return
        val id =
            (message.data["notification_id"] ?: message.messageId ?: message.sentTime.toString())
                .hashCode()
        val intent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("household_id", message.data["household_id"])
                putExtra("route", message.data["route"])
            }
        val pending =
            PendingIntent.getActivity(
                this,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat.Builder(this, "loom_household")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(message.notification?.title ?: "A little nudge from Loom")
                .setContentText(
                    message.notification?.body ?: "There’s something new in your household.",
                )
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build()
        getSystemService(NotificationManager::class.java).notify(id, notification)
    }
}
