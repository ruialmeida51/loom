package com.eyther.lumbridge

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.eyther.lumbridge.app.LoomApp
import com.eyther.lumbridge.app.navigation.push.PendingPush
import com.eyther.lumbridge.app.openAuthenticationCallback
import com.eyther.lumbridge.core.data.local.PlatformStorage
import com.eyther.lumbridge.features.settings.data.notifications.PushPlatform

// AppCompatActivity applies the per-app language picked in the app before Android 13.
class MainActivity : AppCompatActivity() {
    private var permissionCallback: ((Boolean) -> Unit)? = null
    private val notificationsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
            permissionCallback?.invoke(allowed)
            permissionCallback = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlatformStorage.initialize(applicationContext)
        PushPlatform.attachPermissionRequester(
            owner = this,
            canAskAgainAfterDenial = {
                Build.VERSION.SDK_INT >= 33 &&
                    shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
            },
        ) { callback ->
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
        intent.dataString?.let { url -> openAuthenticationCallback(url = url) }
        enableEdgeToEdge()
        setContent { LoomApp() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePush(intent)
        intent.dataString?.let { url -> openAuthenticationCallback(url = url) }
    }

    private fun handlePush(intent: Intent) {
        PendingPush.open(
            householdId = intent.getStringExtra("household_id").orEmpty(),
            route = intent.getStringExtra("route").orEmpty(),
            accountId = intent.getStringExtra("account_id"),
        )
    }

    override fun onDestroy() {
        PushPlatform.detachPermissionRequester(owner = this)
        permissionCallback?.invoke(false)
        permissionCallback = null
        super.onDestroy()
    }
}
