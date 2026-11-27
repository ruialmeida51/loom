package com.eyther.lumbridge.features.settings.data.local

import com.eyther.lumbridge.core.data.local.PlatformStorage
import com.eyther.lumbridge.core.domain.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Device-wide language choice, read by the composition root before the first screen. */
object LanguagePreferences {
    private val mutable = MutableStateFlow(AppLanguage.fromName(PlatformStorage.get("language")))
    val language = mutable.asStateFlow()

    fun set(value: AppLanguage) {
        PlatformStorage.put("language", value.name)
        mutable.value = value
    }
}
