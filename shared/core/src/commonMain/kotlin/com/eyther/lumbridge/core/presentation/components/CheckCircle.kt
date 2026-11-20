package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon

private val CheckCircleTouchSize = 48.dp
private val CheckCircleSize = 22.dp
private val CheckCircleGlyphSize = 14.dp
private val CheckCircleBorderWidth = 2.dp

/**
 * The round checklist control used by shopping and tasks. With [onCheckedChange] null the parent
 * owns the toggle; [muted] (the default then) draws the faint ring of rows that cannot be checked
 * yet, so a parent-toggled row passes `muted = false`.
 * [square] draws a rounded square for selection mode, so selecting never looks like checking.
 */
@Composable
fun CheckCircle(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    square: Boolean = false,
    muted: Boolean = onCheckedChange == null,
) {
    val shape = if (square) RoundedCornerShape(6.dp) else CircleShape
    val colors = MaterialTheme.colorScheme
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .size(CheckCircleTouchSize)
                .then(
                    if (onCheckedChange == null) Modifier
                    else
                        Modifier.toggleable(
                            value = checked,
                            enabled = enabled,
                            role = Role.Checkbox,
                            onValueChange = onCheckedChange,
                        ),
                ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier.size(CheckCircleSize)
                    .alpha(if (enabled || onCheckedChange == null) 1f else 0.5f)
                    .clip(shape)
                    .then(
                        if (checked) Modifier.background(colors.primary)
                        else
                            Modifier.border(
                                BorderStroke(
                                    CheckCircleBorderWidth,
                                    if (muted) colors.outlineVariant else colors.outline,
                                ),
                                shape,
                            ),
                    ),
        ) {
            if (checked)
                LoomIcon(
                    glyph = Glyph.CHECK,
                    tint = colors.onPrimary,
                    decorative = true,
                    modifier = Modifier.size(CheckCircleGlyphSize),
                )
        }
    }
}
