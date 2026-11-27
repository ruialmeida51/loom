package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.deviceLanguageTag

/**
 * Sample content in the app's language: the preview and the Portuguese store listing show a
 * Portuguese household, not an English one in a Portuguese app. Read when the store is built.
 */
internal fun demoText(english: String, portuguese: String): String =
    if (deviceLanguageTag().startsWith("pt")) portuguese else english
