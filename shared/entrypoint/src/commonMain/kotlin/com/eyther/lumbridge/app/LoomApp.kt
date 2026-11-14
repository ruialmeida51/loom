package com.eyther.lumbridge.app

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.app.di.applicationModule
import com.eyther.lumbridge.app.navigation.LauncherHost
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import org.koin.compose.KoinApplication

@Composable
fun LoomApp() {
    KoinApplication(application = { modules(applicationModule) }) {
        val themeName by ThemePreferences.mode.collectAsState()
        val theme = ThemeMode.valueOf(themeName)
        LoomTheme(theme) { Surface { LauncherHost() } }
    }
}
