package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.theme.LoomColors

private val PillHeight = 40.dp
private val PillGlyphSize = 18.dp
private val PillMaximumWidth = 220.dp

/**
 * The one pill used to pick among peers: shopping lists, Money views and similar. The selected
 * pill is solid ink; the others sit on the card surface.
 */
@Composable
fun PillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingGlyph: Glyph? = null,
    trailingGlyph: Glyph? = null,
    role: Role = Role.Tab,
) {
    Surface(
        shape = CircleShape,
        color =
            if (selected) MaterialTheme.colorScheme.inverseSurface
            else MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor =
            if (selected) MaterialTheme.colorScheme.inverseOnSurface
            else MaterialTheme.colorScheme.onSurface,
        shadowElevation = if (!selected && LoomColors.cardShadow) 1.dp else 0.dp,
        border = if (selected) null else BorderStroke(1.dp, LoomColors.cardBorder),
        modifier =
            modifier
                .heightIn(min = PillHeight)
                .widthIn(max = PillMaximumWidth)
                .clip(CircleShape)
                .selectable(selected = selected, role = role, onClick = onClick),
    ) {
        Row(
            modifier =
                Modifier.padding(
                    start = if (leadingGlyph != null) 12.dp else 16.dp,
                    end = if (trailingGlyph != null) 10.dp else 16.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        ) {
            leadingGlyph?.let {
                LoomIcon(
                    glyph = it,
                    tint = LocalContentColor.current,
                    decorative = true,
                    modifier = Modifier.size(PillGlyphSize),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            trailingGlyph?.let {
                LoomIcon(
                    glyph = it,
                    tint = LocalContentColor.current,
                    decorative = true,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

/** The dashed "+" at the end of a pill row that creates another peer (a list, an account). */
@Composable
fun AddPill(description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        modifier =
            modifier.size(PillHeight).semantics {
                contentDescription = description
                role = Role.Button
            },
    ) {
        LoomIcon(
            glyph = Glyph.PLUS,
            tint = LocalContentColor.current,
            decorative = true,
            modifier = Modifier.padding(11.dp),
        )
    }
}

/**
 * Lets a horizontally scrolling row run to the screen edges inside padded content: the row is
 * widened by [bleed] on each side, and should pad its own content by the same amount.
 */
fun Modifier.bleedHorizontally(bleed: Dp): Modifier = layout { measurable, constraints ->
    val extra = (bleed * 2).roundToPx()
    val placeable =
        measurable.measure(
            constraints.copy(
                minWidth = constraints.minWidth + extra,
                maxWidth =
                    if (constraints.hasBoundedWidth) constraints.maxWidth + extra
                    else constraints.maxWidth,
            ),
        )
    layout(placeable.width - extra, placeable.height) { placeable.place(-bleed.roundToPx(), 0) }
}
