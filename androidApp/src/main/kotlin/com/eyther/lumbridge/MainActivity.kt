package com.eyther.lumbridge

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.eyther.lumbridge.app.LoomApp
import com.eyther.lumbridge.app.navigation.push.PendingPush
import com.eyther.lumbridge.core.data.local.PlatformStorage
import com.eyther.lumbridge.features.settings.data.notifications.PushPlatform

class MainActivity : ComponentActivity() {
    private var permissionCallback: ((Boolean) -> Unit)? = null
    private val notificationsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
            permissionCallback?.invoke(allowed)
            permissionCallback = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlatformStorage.initialize(applicationContext)
        PushPlatform.permissionRequester = { callback ->
            if (
                Build.VERSION.SDK_INT < 33 ||
                    checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED
            )
                callback(true)
            else {
                permissionCallback = callback
                notificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        handlePush(intent)
        enableEdgeToEdge()
        setContent { LoomApp() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePush(intent)
    }

    private fun handlePush(intent: Intent) {
        PendingPush.open(
            intent.getStringExtra("household_id").orEmpty(),
            intent.getStringExtra("route").orEmpty(),
        )
    }

    override fun onDestroy() {
        PushPlatform.permissionRequester = null
        super.onDestroy()
    }
}
