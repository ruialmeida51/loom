package com.eyther.lumbridge.features.account.presentation.mapper

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.domain.AppLanguage
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.account.generated.resources.Res
import com.eyther.lumbridge.features.account.generated.resources.language_english
import com.eyther.lumbridge.features.account.generated.resources.language_portuguese
import com.eyther.lumbridge.features.account.generated.resources.language_system
import com.eyther.lumbridge.features.account.generated.resources.theme_dark
import com.eyther.lumbridge.features.account.generated.resources.theme_light
import com.eyther.lumbridge.features.account.generated.resources.theme_system
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun themeLabel(theme: ThemeMode): String =
    stringResource(
        when (theme) {
            ThemeMode.SYSTEM -> Res.string.theme_system
            ThemeMode.LIGHT -> Res.string.theme_light
            ThemeMode.DARK -> Res.string.theme_dark
        },
    )

// Each language is named in itself, so it can be found from any language.
@Composable
internal fun languageLabel(language: AppLanguage): String =
    stringResource(
        when (language) {
            AppLanguage.SYSTEM -> Res.string.language_system
            AppLanguage.ENGLISH -> Res.string.language_english
            AppLanguage.PORTUGUESE -> Res.string.language_portuguese
        },
    )
