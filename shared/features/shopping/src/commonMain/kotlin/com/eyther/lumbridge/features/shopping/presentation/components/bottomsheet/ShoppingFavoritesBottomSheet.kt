package com.eyther.lumbridge.features.shopping.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoomDivider
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.favorites_empty
import com.eyther.lumbridge.features.shopping.generated.resources.favorites_on_list
import com.eyther.lumbridge.features.shopping.generated.resources.favorites_title
import com.eyther.lumbridge.features.shopping.generated.resources.ui_favorite_remove
import com.eyther.lumbridge.features.shopping.presentation.model.ShoppingFavoriteUi
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingFavoritesBottomSheet(
    listName: String,
    favorites: ImmutableList<ShoppingFavoriteUi>,
    busy: Boolean,
    error: TextResource?,
    onReuse: (ShoppingItemDomain) -> Unit,
    onRemoveFavorite: (ShoppingItemDomain) -> Unit,
    onDismissError: () -> Unit,
) {
    FormBottomSheet(title = stringResource(Res.string.favorites_title)) {
        Text(listName, style = MaterialTheme.typography.bodyMedium)
        if (favorites.isEmpty()) Text(stringResource(Res.string.favorites_empty))
        else
            GroupedSurface {
                favorites.forEachIndexed { index, favorite ->
                    key(favorite.item.id) {
                        if (index > 0)
                            LoomDivider()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            NavigationRow(
                                title = favorite.item.name,
                                glyph = Glyph.STAR,
                                description =
                                    if (favorite.alreadyOnList)
                                        stringResource(Res.string.favorites_on_list)
                                    else null,
                                trailingGlyph =
                                    if (favorite.alreadyOnList) Glyph.CHECK else Glyph.PLUS,
                                enabled = !busy && !favorite.alreadyOnList,
                                onClick = { onReuse(favorite.item) },
                                modifier =
                                    Modifier.weight(1f).testTag("favorite_${favorite.item.name}"),
                            )
                            ActionMenu(
                                items =
                                    listOf(
                                        ActionMenuItem(
                                            text =
                                                TextResource.Resource(
                                                    Res.string.ui_favorite_remove,
                                                ),
                                            onClick = { onRemoveFavorite(favorite.item) },
                                            enabled = !busy,
                                        ),
                                    ),
                            )
                        }
                    }
                }
            }
        error?.let { ErrorNotice(message = it, onDismiss = onDismissError) }
    }
}
