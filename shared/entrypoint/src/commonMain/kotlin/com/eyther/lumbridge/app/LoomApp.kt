package com.eyther.lumbridge.app

import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import com.eyther.lumbridge.app.di.applicationModule
import com.eyther.lumbridge.app.navigation.LauncherHost
import com.eyther.lumbridge.core.presentation.*
import org.koin.compose.KoinApplication

@Composable
fun LoomApp() {
    KoinApplication(application = { modules(applicationModule) }) {
        val themeName by com.eyther.lumbridge.core.data.ThemePreferences.mode.collectAsState()
        val theme = ThemeMode.valueOf(themeName)
        LoomTheme(theme) { Surface { LauncherHost() } }
    }
}
