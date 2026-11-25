package com.eyther.lumbridge.features.shopping.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.CheckCircle
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.ui_favorite_add
import com.eyther.lumbridge.features.shopping.generated.resources.ui_favorite_remove
import com.eyther.lumbridge.features.shopping.generated.resources.ui_item_options
import com.eyther.lumbridge.features.shopping.generated.resources.ui_move_to_list
import com.eyther.lumbridge.features.shopping.generated.resources.ui_remove
import org.jetbrains.compose.resources.stringResource

/**
 * A checklist row. Tapping the name edits it in place; grouping uses the swipe gesture and its
 * accessibility actions, so the overflow menu keeps only favourite, move and remove.
 */
@Composable
fun ShoppingItemRow(
    item: ShoppingItemDomain,
    busy: Boolean,
    canMove: Boolean,
    onToggle: (ShoppingItemDomain) -> Unit,
    onStartEditing: (ShoppingItemDomain) -> Unit,
    onRemove: (ShoppingItemDomain) -> Unit,
    onFavorite: (ShoppingItemDomain) -> Unit,
    onMove: (ShoppingItemDomain) -> Unit,
    selectionMode: Boolean = false,
    selected: Boolean = false,
    onSelect: () -> Unit = {},
    editing: Boolean = false,
    editingName: String = "",
    onEditingName: (String) -> Unit = {},
    onEditingNext: () -> Unit = {},
    onEditingFinished: (String) -> Unit = {},
    onEditingDeleteEmpty: () -> Unit = {},
    indented: Boolean = false,
    canNest: Boolean = false,
    canUnnest: Boolean = false,
    onNest: (ShoppingItemDomain) -> Unit = {},
    onUnnest: (ShoppingItemDomain) -> Unit = {},
) {
    ShoppingGroupingGesture(
        id = item.id,
        indented = indented,
        canNest = canNest,
        canUnnest = canUnnest,
        enabled = !busy && !selectionMode && !editing,
        onNest = { onNest(item) },
        onUnnest = { onUnnest(item) },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CheckCircle(
                checked = if (selectionMode) selected else item.checked,
                onCheckedChange = { if (selectionMode) onSelect() else onToggle(item) },
                enabled = !busy,
                square = selectionMode,
                modifier =
                    Modifier.size(48.dp).testTag("check_${item.name}").semantics {
                        contentDescription = item.name
                    },
            )
            if (editing)
                ShoppingLineInput(
                    value = editingName,
                    onValue = onEditingName,
                    onNext = onEditingNext,
                    onFocusLost = { onEditingFinished(item.id) },
                    onDeleteEmpty = onEditingDeleteEmpty,
                    textDecoration =
                        if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f).testTag("edit_input_${item.name}"),
                )
            else
                Column(
                    modifier =
                        Modifier.weight(1f)
                            .clickable(
                                enabled = !busy,
                                onClick = {
                                    if (selectionMode) onSelect() else onStartEditing(item)
                                },
                            )
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
            if (item.favorite)
                LoomIcon(
                    glyph = Glyph.STAR,
                    tint = MaterialTheme.colorScheme.primary,
                    decorative = true,
                    modifier = Modifier.size(16.dp),
                )
            if (!selectionMode)
                ActionMenu(
                    contentDescription = stringResource(Res.string.ui_item_options, item.name),
                    modifier = Modifier.testTag("actions_${item.name}"),
                    items =
                        buildList {
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
                            if (canMove)
                                add(
                                    ActionMenuItem(
                                        text = TextResource.Resource(Res.string.ui_move_to_list),
                                        onClick = { onMove(item) },
                                        enabled = !busy,
                                    ),
                                )
                            add(
                                ActionMenuItem(
                                    text = TextResource.Resource(Res.string.ui_remove),
                                    onClick = { onRemove(item) },
                                    enabled = !busy,
                                    destructive = true,
                                ),
                            )
                        },
                )
        }
    }
}
