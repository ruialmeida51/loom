package com.eyther.lumbridge.app.navigation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.startup_reconnecting
import com.eyther.lumbridge.shared.entrypoint.generated.resources.startup_restoring
import org.jetbrains.compose.resources.stringResource

/** A neutral launch surface keeps login hidden until the SDK has resolved stored credentials. */
@Composable
internal fun SessionStartup(reconnecting: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().testTag("session_startup"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        LinearProgressIndicator(modifier = Modifier.width(160.dp))
        Text(
            text =
                stringResource(
                    if (reconnecting) Res.string.startup_reconnecting
                    else Res.string.startup_restoring,
                ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
