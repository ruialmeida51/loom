package com.eyther.lumbridge.demo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.core.data.local.PlatformStorage

/** Debug-only native test host; release authentication and screens never select demo data. */
// An AppCompatActivity handling locale changes, like MainActivity, so the language switch is real.
class UsabilityTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlatformStorage.initialize(applicationContext)
        enableEdgeToEdge()
        setContent { LoomDemoApp() }
    }
}
