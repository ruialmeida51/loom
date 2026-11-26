package com.eyther.lumbridge.features.shopping.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.AddPill
import com.eyther.lumbridge.core.presentation.components.PillChip
import com.eyther.lumbridge.core.presentation.components.PillRowAction
import com.eyther.lumbridge.core.presentation.components.PillRowHeader
import com.eyther.lumbridge.core.presentation.components.bleedHorizontally
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.favorites_title
import com.eyther.lumbridge.features.shopping.generated.resources.lists_header
import com.eyther.lumbridge.features.shopping.generated.resources.ui_new_list
import com.eyther.lumbridge.features.shopping.generated.resources.ui_shopping_lists
import com.eyther.lumbridge.features.shopping.presentation.mapper.toGlyph
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

private val ChipHeight = 40.dp

/**
 * One pill per active list, scrolling edge to edge under a header that holds Favourites. Tapping
 * another list switches to it; tapping the current one opens the lists manager (rename, icon,
 * archive), which its caret signals. "+ New list" ends the row.
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
    val manage = stringResource(Res.string.ui_shopping_lists)
    Column(modifier = modifier) {
        PillRowHeader(title = stringResource(Res.string.lists_header)) {
            PillRowAction(
                label = stringResource(Res.string.favorites_title),
                glyph = Glyph.STAR,
                onClick = onFavorites,
                enabled = favoritesEnabled,
                modifier = Modifier.testTag("shopping_favorites"),
            )
        }
        Row(
            modifier =
                Modifier.bleedHorizontally(LoomSpacing.content)
                    .horizontalScroll(rememberScrollState())
                    .padding(
                        start = LoomSpacing.content,
                        end = LoomSpacing.content,
                        top = 6.dp,
                        bottom = 4.dp,
                    )
                    .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            lists.forEach { list ->
                val selected = list.id == selectedListId
                PillChip(
                    text = list.name,
                    selected = selected,
                    leadingGlyph = list.icon.toGlyph(),
                    trailingGlyph = if (selected) Glyph.CHEVRON_DOWN else null,
                    onClick = { if (selected) onManage() else onSelect(list.id) },
                    modifier =
                        if (selected)
                            Modifier.testTag("manage_lists").semantics {
                                contentDescription = "${list.name}, $manage"
                            }
                        else Modifier.testTag("list_chip_${list.name}"),
                )
            }
            AddPill(
                label = stringResource(Res.string.ui_new_list),
                onClick = onCreate,
                modifier = Modifier.testTag("shopping_new_list"),
            )
        }
    }
}
