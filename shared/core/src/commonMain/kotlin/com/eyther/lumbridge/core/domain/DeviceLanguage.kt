package com.eyther.lumbridge.core.domain

/**
 * The language the app is showing, as a BCP 47 tag such as "pt-PT" or "en-GB": the language
 * picked in the app's settings, else the app's own language where the platform has one (Android
 * 13+, iOS per-app language), else the device's. Read on each call, so a language change applies
 * without restarting.
 */
fun deviceLanguageTag(): String = appLanguageOverride ?: platformLanguageTag()

/** The tag of the language picked in the app, set by `ProvideAppLanguage`; null follows the device. */
internal var appLanguageOverride: String? = null

internal expect fun platformLanguageTag(): String
