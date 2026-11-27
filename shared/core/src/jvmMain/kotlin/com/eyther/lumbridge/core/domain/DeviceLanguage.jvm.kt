package com.eyther.lumbridge.core.domain

import java.util.Locale

// The JVM default locale (desktop preview and tests).
internal actual fun platformLanguageTag(): String = Locale.getDefault().toLanguageTag()
