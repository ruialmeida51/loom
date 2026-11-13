package com.eyther.lumbridge.app.presentation.localization

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.presentation.localization.genericErrorMessage
import com.eyther.lumbridge.shared.entrypoint.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun localizedMessage(code: String?): String? =
    when {
        code == null -> null
        else -> genericErrorMessage()
    }

@Composable
internal fun localizedToken(value: String): String =
    when (value) {
        "Home" -> stringResource(Res.string.token_home)
        "Feed" -> stringResource(Res.string.token_feed)
        "Shopping" -> stringResource(Res.string.token_shopping)
        "Money" -> stringResource(Res.string.token_money)
        else -> value
    }
