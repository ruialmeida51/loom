package com.eyther.lumbridge.core.presentation.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing

private val MinimumButtonHeight = 48.dp
private const val DisabledContentAlpha = 0.38f
private val ButtonProgressSize = 18.dp
private val ButtonBorderWidth = 1.dp

/** Shared Plannot button geometry; the hidden label preserves size while progress is displayed. */
@Composable
internal fun DefaultLoomButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    outlineColor: Color? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    glyph: Glyph? = null,
    trailingGlyph: Glyph? = null,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = MinimumButtonHeight),
        enabled = enabled && !loading,
        shape = CircleShape,
        border = outlineColor?.let { BorderStroke(width = ButtonBorderWidth, color = it) },
        colors =
            ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor =
                    if (loading) containerColor else MaterialTheme.colorScheme.surfaceVariant,
                // A disabled button sits on surfaceVariant, so its label takes that surface's
                // readable colour; the button's own label colour can vanish on it in dark mode.
                disabledContentColor =
                    if (loading) contentColor.copy(alpha = DisabledContentAlpha)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                modifier = Modifier.alpha(if (loading) 0f else 1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small),
            ) {
                if (leadingContent != null) leadingContent()
                else glyph?.let { LoomIcon(glyph = it, decorative = true) }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                )
                trailingGlyph?.let { LoomIcon(glyph = it, decorative = true) }
            }
            if (loading)
                CircularProgressIndicator(
                    modifier = Modifier.size(ButtonProgressSize),
                    strokeWidth = ButtonBorderWidth,
                    color = contentColor,
                )
        }
    }
}
