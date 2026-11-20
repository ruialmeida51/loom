package com.eyther.lumbridge.features.shopping.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.preview_more
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.pluralStringResource

/**
 * Read-only preview chips; any chip opens the selected checklist in the owning Host. [moreCount]
 * summarises the items that did not fit.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShoppingPreview(
    items: ImmutableList<ShoppingItemDomain>,
    onOpen: () -> Unit,
    moreCount: Int = 0,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { item ->
            PreviewChip(
                text = item.name,
                onClick = onOpen,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurface,
                elevated = true,
            )
        }
        if (moreCount > 0)
            PreviewChip(
                text = pluralStringResource(Res.plurals.preview_more, moreCount, moreCount),
                onClick = onOpen,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                elevated = false,
            )
    }
}

@Composable
private fun PreviewChip(
    text: String,
    onClick: () -> Unit,
    color: Color,
    contentColor: Color,
    elevated: Boolean,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = color,
        contentColor = contentColor,
        shadowElevation = if (elevated && LoomColors.cardShadow) 1.dp else 0.dp,
        border = if (elevated) BorderStroke(1.dp, LoomColors.cardBorder) else null,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}
