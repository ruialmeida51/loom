package com.eyther.lumbridge.core.domain

/** A language the app can be shown in; [SYSTEM] follows the device. */
enum class AppLanguage(val tag: String?) {
    SYSTEM(null),
    ENGLISH("en-GB"),
    PORTUGUESE("pt-PT");

    companion object {
        /** The stored choice, falling back to the device for an unknown or missing value. */
        fun fromName(name: String?): AppLanguage = entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}
