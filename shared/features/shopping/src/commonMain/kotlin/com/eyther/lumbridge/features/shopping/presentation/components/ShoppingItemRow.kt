package com.eyther.lumbridge.features.shopping.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.ui_edit
import com.eyther.lumbridge.features.shopping.generated.resources.ui_favorite_add
import com.eyther.lumbridge.features.shopping.generated.resources.ui_favorite_remove
import com.eyther.lumbridge.features.shopping.generated.resources.ui_item_options
import com.eyther.lumbridge.features.shopping.generated.resources.ui_move_to
import com.eyther.lumbridge.features.shopping.generated.resources.ui_remove
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingItemRow(
    item: ShoppingItemDomain,
    moveTargets: ImmutableList<ShoppingListDomain>,
    busy: Boolean,
    onToggle: (ShoppingItemDomain) -> Unit,
    onEdit: (ShoppingItemDomain) -> Unit,
    onRemove: (ShoppingItemDomain) -> Unit,
    onFavorite: (ShoppingItemDomain) -> Unit,
    onMove: (ShoppingItemDomain, String) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = item.checked,
                onCheckedChange = { onToggle(item) },
                enabled = !busy,
                modifier = Modifier.testTag("check_${item.name}"),
            )
            Column(
                modifier =
                    Modifier.weight(1f)
                        .clickable(enabled = !busy, onClick = { onEdit(item) })
                        .testTag("edit_${item.name}"),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration =
                        if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
                )
                if (item.quantity.isNotBlank())
                    Text(
                        text = item.quantity,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
            }
            if (item.favorite) Text(text = "★", color = MaterialTheme.colorScheme.primary)
            ActionMenu(
                contentDescription = stringResource(Res.string.ui_item_options, item.name),
                modifier = Modifier.testTag("actions_${item.name}"),
                items =
                    buildList {
                        add(
                            ActionMenuItem(
                                text = TextResource.Resource(Res.string.ui_edit),
                                onClick = { onEdit(item) },
                                enabled = !busy,
                            ),
                        )
                        add(
                            ActionMenuItem(
                                text =
                                    TextResource.Resource(
                                        if (item.favorite) Res.string.ui_favorite_remove
                                        else Res.string.ui_favorite_add,
                                    ),
                                onClick = { onFavorite(item) },
                                enabled = !busy,
                            ),
                        )
                        moveTargets.forEach { target ->
                            add(
                                ActionMenuItem(
                                    text =
                                        TextResource.Resource(
                                            Res.string.ui_move_to,
                                            args = listOf(target.name),
                                        ),
                                    onClick = { onMove(item, target.id) },
                                    enabled = !busy,
                                ),
                            )
                        }
                        add(
                            ActionMenuItem(
                                text = TextResource.Resource(Res.string.ui_remove),
                                onClick = { onRemove(item) },
                                enabled = !busy,
                            ),
                        )
                    },
            )
        }
    }
}
