package com.eyther.lumbridge.core.presentation.language

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

/**
 * The activity's configuration keeps the system (or per-app) language and Android resets the
 * process default to it on every configuration change, so the choice is reapplied whenever the
 * configuration changes. Applied during composition, before any string below is read.
 */
@Composable
internal actual fun ApplyPlatformLanguage(tag: String?) {
    val system = LocalConfiguration.current.locales[0]
    Locale.setDefault(tag?.let(Locale::forLanguageTag) ?: system)
}
