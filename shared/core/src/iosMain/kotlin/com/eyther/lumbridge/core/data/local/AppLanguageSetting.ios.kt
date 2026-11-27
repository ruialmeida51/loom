package com.eyther.lumbridge.core.data.local

import com.eyther.lumbridge.core.domain.AppLanguage
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

private const val APPLE_LANGUAGES = "AppleLanguages"

/**
 * The app's own AppleLanguages, the list iOS's per-app language (Settings > Loom > Language)
 * writes too. Read from the app's domain only: the global list is the device's languages, which
 * means "follow the device" here. iOS launches the app in this language next time, system texts
 * included; `ProvideAppLanguage` switches the running app.
 */
actual object AppLanguageSetting {
    actual fun get(): AppLanguage {
        val bundleId = NSBundle.mainBundle.bundleIdentifier ?: return AppLanguage.SYSTEM
        val own = NSUserDefaults.standardUserDefaults.persistentDomainForName(bundleId)
        val languages = own?.get(APPLE_LANGUAGES) as? List<*>
        return AppLanguage.fromTag(languages?.firstOrNull() as? String)
    }

    actual suspend fun set(language: AppLanguage) {
        val defaults = NSUserDefaults.standardUserDefaults
        val tag = language.tag
        if (tag == null) defaults.removeObjectForKey(APPLE_LANGUAGES)
        else defaults.setObject(listOf(tag), forKey = APPLE_LANGUAGES)
    }
}
