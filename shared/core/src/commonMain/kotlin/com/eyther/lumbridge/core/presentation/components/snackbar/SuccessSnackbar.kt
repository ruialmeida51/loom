package com.eyther.lumbridge.core.presentation.components.snackbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomColors

const val DEFAULT_ALERT_DURATION: Long = 4_000L

/** Plannot-style transient feedback. Its lifetime is owned by the ViewModel, not composition. */
@Composable
fun SuccessSnackbar(message: TextResource?, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = message != null,
        modifier = modifier,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
    ) {
        Surface(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(12.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
                    .testTag("success_snackbar"),
            shape = RoundedCornerShape(16.dp),
            color = LoomColors.positiveContainer,
            contentColor = LoomColors.onPositiveContainer,
            shadowElevation = 4.dp,
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(message?.getString().orEmpty(), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
