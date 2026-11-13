package com.eyther.lumbridge.features.settings.domain.errors

import com.eyther.lumbridge.core.domain.errors.AppError

sealed class SettingsError(cause: Throwable? = null) : AppError(cause) {

    class InvalidTheme(cause: Throwable? = null) : SettingsError(cause)

    class UnsupportedPlatform(cause: Throwable? = null) : SettingsError(cause)

    class ProviderUnavailable(cause: Throwable? = null) : SettingsError(cause)

    class PermissionDenied(cause: Throwable? = null) : SettingsError(cause)

    class RegistrationFailed(cause: Throwable? = null) : SettingsError(cause)
}
