package com.eyther.lumbridge.core.presentation.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.eyther.lumbridge.core.domain.AppLanguage
import com.eyther.lumbridge.core.domain.appLanguageOverride

/**
 * Shows [content] in [language], switching in place when it changes.
 *
 * Compose resources pick strings by the platform language but only look at it again when the
 * density changes, so each language gets its own density object: every string re-resolves while
 * navigation and screen state stay where they are.
 */
@Composable
fun ProvideAppLanguage(language: AppLanguage, content: @Composable () -> Unit) {
    appLanguageOverride = language.tag
    ApplyPlatformLanguage(tag = language.tag)
    val density = LocalDensity.current
    val languageDensity = remember(density, language) { object : Density by density {} }
    CompositionLocalProvider(LocalDensity provides languageDensity, content = content)
}

/** Makes [tag] (null for the device's language) the language the platform reports. */
@Composable internal expect fun ApplyPlatformLanguage(tag: String?)
