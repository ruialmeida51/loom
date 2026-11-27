package com.eyther.lumbridge.core.domain

/**
 * The language the app is showing, as a BCP 47 tag such as "pt-PT" or "en-GB": the app's own
 * language (picked in the app or the phone's per-app setting), else the device's. Read on each
 * call, so a language change applies without restarting.
 */
fun deviceLanguageTag(): String = appLanguageOverride ?: platformLanguageTag()

/**
 * The language a platform that cannot switch a running app reports late (iOS keeps its launch
 * localisation), set while `ProvideAppLanguage` switches in place; null follows the platform.
 */
internal var appLanguageOverride: String? = null

internal expect fun platformLanguageTag(): String
