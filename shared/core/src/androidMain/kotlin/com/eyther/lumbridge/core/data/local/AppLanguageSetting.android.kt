package com.eyther.lumbridge.core.data.local

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.eyther.lumbridge.core.domain.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * AppCompat's per-app language: the system's own on Android 13+ (Settings > Apps > Loom >
 * Language shows the same choice), stored by AppCompat before that. Applying it recreates the
 * activity in the new language; navigation and screen state come back from saved state.
 */
actual object AppLanguageSetting {
    actual fun get(): AppLanguage =
        AppLanguage.fromTag(AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag())

    actual suspend fun set(language: AppLanguage) {
        val locales =
            language.tag?.let(LocaleListCompat::forLanguageTags)
                ?: LocaleListCompat.getEmptyLocaleList()
        // Before Android 13 AppCompat updates the open activities, which needs the main thread.
        withContext(Dispatchers.Main.immediate) { AppCompatDelegate.setApplicationLocales(locales) }
    }
}
