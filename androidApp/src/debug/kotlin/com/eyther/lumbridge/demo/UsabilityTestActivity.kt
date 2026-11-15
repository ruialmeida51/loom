package com.eyther.lumbridge.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.core.data.local.PlatformStorage

/** Debug-only native test host; release authentication and screens never select demo data. */
class UsabilityTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlatformStorage.initialize(applicationContext)
        enableEdgeToEdge()
        setContent { LoomDemoApp() }
    }
}
