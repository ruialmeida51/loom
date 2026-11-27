package com.eyther.lumbridge.core.domain

/**
 * The language the app is showing, as a BCP 47 tag such as "pt-PT" or "en-GB": the app's own
 * language where the platform has one (Android 13+, iOS per-app language), else the device's.
 * Read on each call, so a language change applies without restarting.
 */
expect fun deviceLanguageTag(): String
