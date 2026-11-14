package com.eyther.lumbridge.features.settings.data.local

import com.eyther.lumbridge.core.data.local.PlatformStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Device-wide theme state shared by the composition root and household preference sources. */
object ThemePreferences {
    private val mutable =
        MutableStateFlow(
            PlatformStorage.get("theme")?.takeIf { it in setOf("SYSTEM", "LIGHT", "DARK") }
                ?: "SYSTEM",
        )
    val mode = mutable.asStateFlow()

    fun set(value: String) {
        PlatformStorage.put("theme", value)
        mutable.value = value
    }
}
