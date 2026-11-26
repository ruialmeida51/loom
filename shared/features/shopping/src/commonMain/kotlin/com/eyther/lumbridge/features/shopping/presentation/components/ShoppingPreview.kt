package com.eyther.lumbridge.features.shopping.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.IconTile
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.preview_more
import com.eyther.lumbridge.features.shopping.generated.resources.preview_to_pick_up
import com.eyther.lumbridge.features.shopping.presentation.mapper.toGlyph
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.pluralStringResource

private val BulletSize = 18.dp

/**
 * Home's view of the selected list as one card: its icon, name and count, then the first few
 * items as quiet checklist lines. Any tap opens the list in the owning Host.
 */
@Composable
fun ShoppingPreview(
    list: ShoppingListDomain?,
    items: ImmutableList<ShoppingItemDomain>,
    totalCount: Int,
    moreCount: Int,
    onOpen: () -> Unit,
) {
    Surface(
        onClick = onOpen,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = if (LoomColors.cardShadow) 1.dp else 0.dp,
        border = BorderStroke(1.dp, LoomColors.cardBorder),
        modifier = Modifier.fillMaxWidth().testTag("home_shopping_card"),
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = LoomSpacing.content, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconTile(glyph = list?.icon?.toGlyph() ?: Glyph.SHOPPING)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    list?.let {
                        Text(
                            it.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        pluralStringResource(Res.plurals.preview_to_pick_up, totalCount, totalCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                LoomIcon(
                    glyph = Glyph.CHEVRON_RIGHT,
                    tint = MaterialTheme.colorScheme.outline,
                    decorative = true,
                    modifier = Modifier.size(18.dp),
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(
                modifier = Modifier.padding(horizontal = LoomSpacing.content, vertical = 8.dp),
            ) {
                items.forEach { item -> PreviewLine(item = item) }
                if (moreCount > 0)
                    Text(
                        pluralStringResource(Res.plurals.preview_more, moreCount, moreCount),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = BulletSize + 12.dp, top = 4.dp, bottom = 6.dp),
                    )
            }
        }
    }
}

@Composable
private fun PreviewLine(item: ShoppingItemDomain) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // A quiet ring echoes the list's checkboxes without pretending to be one.
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.size(BulletSize),
        ) {}
        Text(
            item.displayName(),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (item.quantity.isNotBlank())
            Text(
                item.quantity,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
    }
}
