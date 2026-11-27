package com.eyther.lumbridge.core.data.local

import com.eyther.lumbridge.core.domain.AppLanguage
import java.util.Locale

/** Desktop preview and tests: the JVM default locale, restored when following the device again. */
actual object AppLanguageSetting {
    private var language = AppLanguage.SYSTEM
    private var systemLocale: Locale? = null

    actual fun get(): AppLanguage = language

    actual suspend fun set(language: AppLanguage) {
        val tag = language.tag
        if (tag == null) {
            systemLocale?.let(Locale::setDefault)
            systemLocale = null
        } else {
            if (systemLocale == null) systemLocale = Locale.getDefault()
            Locale.setDefault(Locale.forLanguageTag(tag))
        }
        this.language = language
    }
}
