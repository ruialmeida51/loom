package com.eyther.lumbridge.app

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.eyther.lumbridge.app.navigation.LauncherHost
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.di.applicationModules
import com.eyther.lumbridge.features.analytics.domain.usecase.InitializeAnalyticsUseCase
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

@Composable
fun LoomApp() {
    val configuration = remember { koinConfiguration { modules(applicationModules()) } }
    KoinApplication(configuration = configuration) {
        val initializeAnalyticsUseCase: InitializeAnalyticsUseCase = koinInject()
        LaunchedEffect(initializeAnalyticsUseCase) { initializeAnalyticsUseCase() }
        val themeName by ThemePreferences.mode.collectAsState()
        val theme = ThemeMode.valueOf(themeName)
        LoomTheme(theme) { Surface { LauncherHost() } }
    }
}
