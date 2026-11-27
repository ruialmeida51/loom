package com.eyther.lumbridge.core.domain

import java.util.Locale

// The JVM default locale (desktop preview and tests).
actual fun deviceLanguageTag(): String = Locale.getDefault().toLanguageTag()
