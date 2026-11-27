package com.eyther.lumbridge.core.domain

/** A language the app can be shown in; [SYSTEM] follows the device. */
enum class AppLanguage(val tag: String?) {
    SYSTEM(null),
    ENGLISH("en-GB"),
    PORTUGUESE("pt-PT");

    companion object {
        /**
         * The choice a stored BCP 47 tag stands for, matched by language ("pt" and "pt-BR" are
         * Portuguese) since the platform's own settings may store a different region; no tag, or a
         * language the app does not have, follows the device.
         */
        fun fromTag(tag: String?): AppLanguage {
            val language = tag?.substringBefore('-')?.substringBefore('_')?.lowercase()
            return entries.firstOrNull { choice ->
                choice.tag?.substringBefore('-') == language
            } ?: SYSTEM
        }
    }
}
