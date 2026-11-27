package com.eyther.lumbridge.core.domain

import java.util.Locale

// Android applies the per-app language to the process default locale.
actual fun deviceLanguageTag(): String = Locale.getDefault().toLanguageTag()
