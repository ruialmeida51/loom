package com.eyther.lumbridge.core.data.local

import com.eyther.lumbridge.core.domain.AppLanguage

/**
 * The language picked for the app, kept where the platform keeps its per-app language, so the
 * phone's own settings and the app always agree.
 */
expect object AppLanguageSetting {
    fun get(): AppLanguage

    suspend fun set(language: AppLanguage)
}
