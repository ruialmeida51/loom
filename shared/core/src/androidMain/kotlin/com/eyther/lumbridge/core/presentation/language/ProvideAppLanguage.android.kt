package com.eyther.lumbridge.core.presentation.language

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.domain.AppLanguage

// AppCompat applies the language as a configuration change, which recomposes every string.
@Composable
actual fun ProvideAppLanguage(language: AppLanguage, content: @Composable () -> Unit) = content()
