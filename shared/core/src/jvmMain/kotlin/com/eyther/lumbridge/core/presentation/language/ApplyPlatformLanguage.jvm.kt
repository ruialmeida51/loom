package com.eyther.lumbridge.core.presentation.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.Locale

// The JVM default before a language was picked, restored when the app follows the device again.
private var systemLocale: Locale? = null

@Composable
internal actual fun ApplyPlatformLanguage(tag: String?) {
    remember(tag) {
        if (tag == null) {
            systemLocale?.let(Locale::setDefault)
            systemLocale = null
        } else {
            if (systemLocale == null) systemLocale = Locale.getDefault()
            Locale.setDefault(Locale.forLanguageTag(tag))
        }
    }
}
