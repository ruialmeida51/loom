package com.eyther.lumbridge.features.settings.data.local

import com.eyther.lumbridge.core.data.local.AppLanguageSetting
import com.eyther.lumbridge.core.domain.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Device-wide language choice, read by the composition root and the account screen. */
object LanguagePreferences {
    private val mutable = MutableStateFlow(AppLanguageSetting.get())
    val language = mutable.asStateFlow()

    suspend fun set(value: AppLanguage) {
        AppLanguageSetting.set(value)
        mutable.value = value
    }

    /** Picks up a change made in the phone's own settings while the app was in the background. */
    fun refresh() {
        mutable.value = AppLanguageSetting.get()
    }
}
