package com.eyther.lumbridge.core.presentation.language

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.domain.AppLanguage

// AppCompat recreates the activity in the new language, so every string is read afresh.
@Composable
actual fun ProvideAppLanguage(language: AppLanguage, content: @Composable () -> Unit) = content()
