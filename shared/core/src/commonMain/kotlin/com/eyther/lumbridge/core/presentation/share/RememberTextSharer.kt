package com.eyther.lumbridge.core.presentation.share

import androidx.compose.runtime.Composable

/** Opens native text sharing, returning false when this platform has no available share target. */
@Composable expect fun rememberTextSharer(): (String) -> Boolean
