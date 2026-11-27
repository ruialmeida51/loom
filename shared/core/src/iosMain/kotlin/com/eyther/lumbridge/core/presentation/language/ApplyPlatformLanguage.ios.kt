package com.eyther.lumbridge.core.presentation.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

private const val APPLE_LANGUAGES = "AppleLanguages"

/**
 * The app's own language list sits in front of the device's; removing it follows the device again.
 * iOS also launches the app in this language next time, system texts included.
 */
@Composable
internal actual fun ApplyPlatformLanguage(tag: String?) {
    remember(tag) {
        val defaults = NSUserDefaults.standardUserDefaults
        if (tag == null) defaults.removeObjectForKey(APPLE_LANGUAGES)
        else defaults.setObject(listOf(tag), forKey = APPLE_LANGUAGES)
    }
}
