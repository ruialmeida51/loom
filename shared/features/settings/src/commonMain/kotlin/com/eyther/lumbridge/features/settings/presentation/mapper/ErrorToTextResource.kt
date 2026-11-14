package com.eyther.lumbridge.features.settings.presentation.mapper

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.presentation.mapper.toTextResource
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.settings.domain.errors.SettingsError
import com.eyther.lumbridge.features.settings.generated.resources.Res
import com.eyther.lumbridge.features.settings.generated.resources.error_invalid_theme
import com.eyther.lumbridge.features.settings.generated.resources.error_permission_denied
import com.eyther.lumbridge.features.settings.generated.resources.error_provider_unavailable
import com.eyther.lumbridge.features.settings.generated.resources.error_registration_failed
import com.eyther.lumbridge.features.settings.generated.resources.error_unsupported_platform

fun AppError.toSettingsTextResource(): TextResource = when (this) {
    is SettingsError.InvalidTheme -> TextResource.Resource(Res.string.error_invalid_theme)
    is SettingsError.UnsupportedPlatform -> TextResource.Resource(Res.string.error_unsupported_platform)
    is SettingsError.ProviderUnavailable -> TextResource.Resource(Res.string.error_provider_unavailable)
    is SettingsError.PermissionDenied -> TextResource.Resource(Res.string.error_permission_denied)
    is SettingsError.RegistrationFailed -> TextResource.Resource(Res.string.error_registration_failed)
    else -> toTextResource()
}
