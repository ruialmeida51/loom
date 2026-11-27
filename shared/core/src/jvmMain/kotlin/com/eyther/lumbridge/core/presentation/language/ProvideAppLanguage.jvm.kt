package com.eyther.lumbridge.core.presentation.language

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.domain.AppLanguage

@Composable
actual fun ProvideAppLanguage(language: AppLanguage, content: @Composable () -> Unit) =
    SwitchLanguageInPlace(language = language, content = content)
