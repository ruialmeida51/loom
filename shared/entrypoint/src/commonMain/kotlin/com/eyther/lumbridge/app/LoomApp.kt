package com.eyther.lumbridge.app

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.eyther.lumbridge.app.navigation.LauncherHost
import com.eyther.lumbridge.app.navigation.StartupConsentHost
import com.eyther.lumbridge.core.domain.media.PictureLoader
import com.eyther.lumbridge.core.presentation.components.picture.LocalPictureLoader
import com.eyther.lumbridge.core.presentation.language.ProvideAppLanguage
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.di.applicationModules
import com.eyther.lumbridge.features.settings.data.local.LanguagePreferences
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

@Composable
fun LoomApp() {
    val configuration = remember { koinConfiguration { modules(applicationModules()) } }
    KoinApplication(configuration = configuration) {
        val themeName by ThemePreferences.mode.collectAsState()
        val language by LanguagePreferences.language.collectAsState()
        // The phone's own per-app language setting can change while Loom is in the background.
        LifecycleResumeEffect(Unit) {
            LanguagePreferences.refresh()
            onPauseOrDispose {}
        }
        val theme = ThemeMode.entries.firstOrNull { it.name == themeName } ?: ThemeMode.SYSTEM
        // Avatars and household badges anywhere load stored pictures through this one loader.
        ProvideAppLanguage(language) {
            CompositionLocalProvider(LocalPictureLoader provides koinInject<PictureLoader>()) {
                LoomTheme(theme) { Surface { StartupConsentHost { LauncherHost() } } }
            }
        }
    }
}
