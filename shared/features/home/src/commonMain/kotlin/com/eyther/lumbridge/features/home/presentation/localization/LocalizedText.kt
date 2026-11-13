package com.eyther.lumbridge.features.home.presentation.localization

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.presentation.localization.genericErrorMessage
import com.eyther.lumbridge.features.home.generated.resources.*

@Composable
internal fun localizedMessage(code: String?): String? =
    when {
        code == null -> null
        else -> genericErrorMessage()
    }
