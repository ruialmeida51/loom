package com.eyther.lumbridge.features.authentication.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.authentication.generated.resources.Res
import com.eyther.lumbridge.features.authentication.generated.resources.session_choose_household
import com.eyther.lumbridge.features.authentication.generated.resources.session_retry
import com.eyther.lumbridge.features.authentication.generated.resources.startup_reconnecting
import com.eyther.lumbridge.features.authentication.generated.resources.startup_restoring
import org.jetbrains.compose.resources.stringResource

// Bound recovery actions on wide windows and keep the neutral progress indicator compact.
private val StartupContentMaxWidth = 400.dp
private val StartupProgressWidth = 160.dp

/** A neutral launch surface keeps login hidden until the SDK has resolved stored credentials. */
@Composable
internal fun SessionStartup(
    reconnecting: Boolean,
    error: TextResource? = null,
    onRetry: () -> Unit = {},
    onChooseHousehold: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().testTag("session_startup"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LoomSpacing.content, Alignment.CenterVertically),
    ) {
        if (error == null) LinearProgressIndicator(modifier = Modifier.width(StartupProgressWidth))
        Text(
            text =
                stringResource(
                    if (reconnecting) Res.string.startup_reconnecting
                    else Res.string.startup_restoring,
                ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        error?.let { message ->
            Column(
                Modifier.widthIn(max = StartupContentMaxWidth).padding(LoomSpacing.content),
                verticalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
            ) {
                ErrorNotice(message = message)
                LoomButton(
                    text = stringResource(Res.string.session_retry),
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth().testTag("retry_session_household"),
                )
                LoomSecondaryButton(
                    text = stringResource(Res.string.session_choose_household),
                    onClick = onChooseHousehold,
                )
            }
        }
    }
}
