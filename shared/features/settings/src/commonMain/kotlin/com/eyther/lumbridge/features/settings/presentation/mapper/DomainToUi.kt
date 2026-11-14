package com.eyther.lumbridge.features.settings.presentation.mapper

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.features.settings.generated.resources.Res
import com.eyther.lumbridge.features.settings.generated.resources.token_dark
import com.eyther.lumbridge.features.settings.generated.resources.token_light
import com.eyther.lumbridge.features.settings.generated.resources.token_member
import com.eyther.lumbridge.features.settings.generated.resources.token_owner
import com.eyther.lumbridge.features.settings.generated.resources.token_system
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun settingsOptionLabel(value: String): String =
    when (value) {
        "SYSTEM" -> stringResource(Res.string.token_system)
        "LIGHT" -> stringResource(Res.string.token_light)
        "DARK" -> stringResource(Res.string.token_dark)
        "owner" -> stringResource(Res.string.token_owner)
        "member" -> stringResource(Res.string.token_member)
        else -> value
    }
