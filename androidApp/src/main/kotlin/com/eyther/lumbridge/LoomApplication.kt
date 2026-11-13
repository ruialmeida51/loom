package com.eyther.lumbridge

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.eyther.lumbridge.core.data.PlatformStorage

class LoomApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PlatformStorage.initialize(this)
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(
                NotificationChannel(
                    "loom_household",
                    "Your household",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
    }
}
