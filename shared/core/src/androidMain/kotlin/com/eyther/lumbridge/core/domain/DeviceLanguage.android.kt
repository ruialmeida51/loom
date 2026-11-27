package com.eyther.lumbridge.core.domain

import java.util.Locale

// Android applies the per-app language to the process default locale.
internal actual fun platformLanguageTag(): String = Locale.getDefault().toLanguageTag()
