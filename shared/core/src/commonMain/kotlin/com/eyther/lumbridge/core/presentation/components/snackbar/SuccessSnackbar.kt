package com.eyther.lumbridge.core.presentation.components.snackbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomColors

/** Plain confirmations only need a glance. */
const val DEFAULT_ALERT_DURATION: Long = 2_500L

/** Long enough to reach Undo without the message lingering over the screen. */
const val DEFAULT_ACTION_ALERT_DURATION: Long = 5_000L

private val SnackbarMaximumWidth = 360.dp // Sized to its words, never a full-width banner.
private val SnackbarMinimumHeight = 44.dp
private val SnackbarActionMinimumSize = 44.dp // Touch target for the text-only action.
private val SnackbarIconSize = 16.dp
private val SnackbarProgressSize = 16.dp
private val SnackbarShape = RoundedCornerShape(14.dp)
private const val SnackbarDismissDragPx = 24f

/**
 * Compact transient feedback: one dark pill centred above the navigation or add button. Its
 * lifetime is owned by the ViewModel, not composition. [onDismiss] enables swipe-down dismissal.
 */
@Composable
fun SuccessSnackbar(
    message: TextResource?,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    actionEnabled: Boolean = true,
    actionLoading: Boolean = false,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        ) {
            val hasAction = actionText != null && onAction != null
            Surface(
                modifier =
                    Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        .widthIn(max = SnackbarMaximumWidth)
                        .heightIn(min = SnackbarMinimumHeight)
                        .then(
                            if (onDismiss == null) Modifier
                            else
                                Modifier.pointerInput(onDismiss) {
                                    detectVerticalDragGestures { _, dragAmount ->
                                        if (dragAmount > SnackbarDismissDragPx) onDismiss()
                                    }
                                },
                        )
                        .semantics { liveRegion = LiveRegionMode.Polite }
                        .testTag("success_snackbar"),
                shape = SnackbarShape,
                color = LoomColors.snackbar,
                contentColor = LoomColors.onSnackbar,
                shadowElevation = 6.dp,
            ) {
                Row(
                    modifier =
                        Modifier.padding(
                            start = 16.dp,
                            end = if (hasAction) 4.dp else 16.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!hasAction) {
                        LoomIcon(
                            glyph = Glyph.CHECK,
                            tint = LoomColors.snackbarAction,
                            decorative = true,
                            modifier = Modifier.size(SnackbarIconSize),
                        )
                        Box(Modifier.size(width = 10.dp, height = 1.dp))
                    }
                    Text(
                        text = message?.getString().orEmpty(),
                        modifier = Modifier.padding(vertical = 12.dp).weight(1f, fill = false),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (hasAction)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier =
                                Modifier.padding(start = 8.dp)
                                    .heightIn(min = SnackbarActionMinimumSize)
                                    .widthIn(min = SnackbarActionMinimumSize)
                                    .clickable(
                                        enabled = actionEnabled && !actionLoading,
                                        role = Role.Button,
                                        onClick = { onAction?.invoke() },
                                    )
                                    .padding(horizontal = 12.dp)
                                    .testTag("snackbar_action"),
                        ) {
                            if (actionLoading)
                                CircularProgressIndicator(
                                    modifier = Modifier.size(SnackbarProgressSize),
                                    strokeWidth = 2.dp,
                                    color = LoomColors.snackbarAction,
                                )
                            else
                                Text(
                                    text = actionText.orEmpty(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color =
                                        if (actionEnabled) LoomColors.snackbarAction
                                        else LoomColors.snackbarAction.copy(alpha = 0.5f),
                                )
                        }
                }
            }
        }
    }
}
