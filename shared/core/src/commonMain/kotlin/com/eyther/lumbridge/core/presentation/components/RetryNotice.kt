package com.eyther.lumbridge.core.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.core.generated.resources.Res
import com.eyther.lumbridge.core.generated.resources.ui_try_again
import com.eyther.lumbridge.core.presentation.resources.TextResource
import org.jetbrains.compose.resources.stringResource

/**
 * A failed read: its message, dismissible when there is content to fall back to, then the retry.
 * Lays out in the caller's column so it follows that column's spacing.
 */
@Composable
fun RetryNotice(
    message: TextResource,
    onRetry: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    retryText: String = stringResource(Res.string.ui_try_again),
    retryModifier: Modifier = Modifier,
) {
    ErrorNotice(message = message, onDismiss = onDismiss)
    LoomSecondaryButton(text = retryText, onClick = onRetry, modifier = retryModifier)
}
