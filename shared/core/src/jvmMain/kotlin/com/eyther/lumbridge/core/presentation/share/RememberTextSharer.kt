package com.eyther.lumbridge.core.presentation.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable actual fun rememberTextSharer(): (String) -> Boolean = remember { { false } }
