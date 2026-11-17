package com.eyther.lumbridge.features.feed.presentation.components.bottomsheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.feed.generated.resources.Res
import com.eyther.lumbridge.features.feed.generated.resources.ui_cancel
import com.eyther.lumbridge.features.feed.generated.resources.ui_feed_url_hint
import com.eyther.lumbridge.features.feed.generated.resources.ui_follow_a_feed_a533eb8
import com.eyther.lumbridge.features.feed.generated.resources.ui_follow_feed
import com.eyther.lumbridge.features.feed.generated.resources.ui_paste_an_rss_or_atom_feed_url_articles_will
import org.jetbrains.compose.resources.stringResource

@Composable
fun FollowFeedBottomSheet(
    url: String,
    busy: Boolean,
    formError: TextResource?,
    canFollow: Boolean,
    onUrl: (String) -> Unit,
    onShowFollow: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onFollow: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.ui_follow_a_feed_a533eb8),
        action = {
            LoomButton(
                text = stringResource(Res.string.ui_follow_feed),
                onClick = onFollow,
                loading = busy,
                enabled = canFollow,
            )
        },
        dismissAction = {
            OutlinedButton(onClick = { onShowFollow(false) }, enabled = !busy) {
                Text(stringResource(Res.string.ui_cancel))
            }
        },
    ) {
        Text(stringResource(Res.string.ui_paste_an_rss_or_atom_feed_url_articles_will))
        OutlinedTextField(
            url,
            onUrl,
            label = { Text(stringResource(Res.string.ui_feed_url_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy,
        )
        formError?.let { message ->
            ErrorNotice(message = message, onDismiss = onDismiss)
        }
    }
}
