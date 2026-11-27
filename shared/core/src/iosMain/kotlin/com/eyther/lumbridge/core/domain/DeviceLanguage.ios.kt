package com.eyther.lumbridge.core.domain

import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

// The localisation iOS picked for this app from CFBundleLocalizations and the user's languages.
internal actual fun platformLanguageTag(): String =
    (NSBundle.mainBundle.preferredLocalizations.firstOrNull() as? String)
        ?: NSLocale.currentLocale.languageCode
