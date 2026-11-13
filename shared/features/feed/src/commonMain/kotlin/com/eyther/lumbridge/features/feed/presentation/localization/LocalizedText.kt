package com.eyther.lumbridge.features.feed.presentation.localization

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.presentation.localization.genericErrorMessage
import com.eyther.lumbridge.features.feed.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun localizedMessage(code: String?): String? =
    when {
        code == null -> null
        code.contains("feed.message_enter_a_secure_rss_or_atom_feed_url_beginning") ->
            stringResource(Res.string.message_enter_a_secure_rss_or_atom_feed_url_beginning)
        else -> genericErrorMessage()
    }
