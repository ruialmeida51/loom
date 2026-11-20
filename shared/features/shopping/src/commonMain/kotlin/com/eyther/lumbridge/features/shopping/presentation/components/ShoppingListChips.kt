package com.eyther.lumbridge.features.shopping.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.ui_new_list
import com.eyther.lumbridge.features.shopping.generated.resources.ui_shopping_lists
import com.eyther.lumbridge.features.shopping.presentation.mapper.toGlyph
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

private val ChipHeight = 40.dp
private val ChipGlyphSize = 18.dp
private val ChipMaximumWidth = 200.dp

/**
 * One chip per active list. Tapping another list switches to it; tapping the current one opens the
 * lists manager (rename, icon, archive), which its caret signals. The star opens favourites.
 */
@Composable
fun ShoppingListChips(
    lists: ImmutableList<ShoppingListDomain>,
    selectedListId: String,
    favoritesEnabled: Boolean,
    onSelect: (String) -> Unit,
    onManage: () -> Unit,
    onCreate: () -> Unit,
    onFavorites: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            lists.forEach { list ->
                val selected = list.id == selectedListId
                ListChip(
                    name = list.name,
                    glyph = list.icon.toGlyph(),
                    selected = selected,
                    onClick = { if (selected) onManage() else onSelect(list.id) },
                    modifier =
                        if (selected) Modifier.testTag("manage_lists")
                        else Modifier.testTag("list_chip_${list.name}"),
                )
            }
            val newList = stringResource(Res.string.ui_new_list)
            Surface(
                onClick = onCreate,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                modifier =
                    Modifier.size(ChipHeight).testTag("shopping_new_list").semantics {
                        contentDescription = newList
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
        Surface(
            onClick = onFavorites,
            enabled = favoritesEnabled,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.primary,
            shadowElevation = if (LoomColors.cardShadow) 1.dp else 0.dp,
            border = BorderStroke(1.dp, LoomColors.cardBorder),
            modifier = Modifier.size(44.dp).testTag("shopping_favorites"),
        ) {
            LoomIcon(
                glyph = Glyph.STAR,
                tint = LocalContentColor.current,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}

@Composable
private fun ListChip(
    name: String,
    glyph: Glyph,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val manage = stringResource(Res.string.ui_shopping_lists)
    Surface(
        onClick = onClick,
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
            modifier.heightIn(min = ChipHeight).widthIn(max = ChipMaximumWidth).semantics {
                this.selected = selected
                role = Role.Tab
                if (selected) contentDescription = "$name, $manage"
            },
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = if (selected) 10.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            LoomIcon(
                glyph = glyph,
                tint = LocalContentColor.current,
                decorative = true,
                modifier = Modifier.size(ChipGlyphSize),
            )
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (selected)
                LoomIcon(
                    glyph = Glyph.CHEVRON_DOWN,
                    tint = LocalContentColor.current,
                    decorative = true,
                    modifier = Modifier.size(16.dp),
                )
        }
    }
}
