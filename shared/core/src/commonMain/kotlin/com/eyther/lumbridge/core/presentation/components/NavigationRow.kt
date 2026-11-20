package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing

private val MinimumRowHeight = 56.dp // Compact single-line rows retain a full touch target.
private val MaximumSummaryWidth = 180.dp // Keep the row title readable on a compact phone.

private val IconTileSize = 36.dp
private val IconInTileSize = 20.dp
private val IconTileShape = RoundedCornerShape(12.dp)
private val TrailingIconSize = 18.dp
private const val EmphasisTileAlpha = 0.12f

/**
 * Compact navigation/action row; emphasisColor can identify a destructive action without relying on
 * colour alone. [trailingContent] keeps a popup's anchor beside its caret, or shows action
 * progress.
 */
@Composable
fun NavigationRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    value: String? = null,
    glyph: Glyph? = null,
    trailingGlyph: Glyph = Glyph.CHEVRON_RIGHT,
    emphasisColor: Color = Color.Unspecified,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val titleColor =
        if (emphasisColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface
        else emphasisColor
    val iconColor =
        if (emphasisColor == Color.Unspecified) MaterialTheme.colorScheme.primary else emphasisColor
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .heightIn(min = MinimumRowHeight)
                .padding(horizontal = LoomSpacing.content, vertical = LoomSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
    ) {
        glyph?.let {
            // Icons sit in a soft tile so a list of rows scans by shape and colour.
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier.size(IconTileSize)
                        .clip(IconTileShape)
                        .background(
                            if (emphasisColor == Color.Unspecified)
                                MaterialTheme.colorScheme.primaryContainer
                            else emphasisColor.copy(alpha = EmphasisTileAlpha),
                        ),
            ) {
                LoomIcon(
                    glyph = it,
                    tint =
                        if (emphasisColor == Color.Unspecified)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else iconColor,
                    decorative = true,
                    modifier = Modifier.size(IconInTileSize),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = titleColor)
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        value?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.widthIn(max = MaximumSummaryWidth),
            )
        }
        if (trailingContent != null) trailingContent()
        else
            LoomIcon(
                glyph = trailingGlyph,
                tint = MaterialTheme.colorScheme.outline,
                decorative = true,
                modifier = Modifier.size(TrailingIconSize),
            )
    }
}
