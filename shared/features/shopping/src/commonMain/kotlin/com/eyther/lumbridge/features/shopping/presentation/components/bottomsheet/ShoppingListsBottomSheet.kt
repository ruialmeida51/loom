package com.eyther.lumbridge.features.shopping.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.list_delete
import com.eyther.lumbridge.features.shopping.generated.resources.ui_archive
import com.eyther.lumbridge.features.shopping.generated.resources.ui_new_list
import com.eyther.lumbridge.features.shopping.generated.resources.ui_rename
import com.eyther.lumbridge.features.shopping.generated.resources.ui_restore
import com.eyther.lumbridge.features.shopping.generated.resources.ui_shopping_lists
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingListsBottomSheet(
    lists: List<ShoppingListDomain>,
    activeLists: List<ShoppingListDomain>,
    selectedListId: String,
    onSelect: (String) -> Unit,
    busy: Boolean,
    formError: TextResource?,
    onListForm: (ShoppingListDomain?) -> Unit,
    onArchive: (ShoppingListDomain) -> Unit,
    onDelete: (ShoppingListDomain) -> Unit,
    onDismiss: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.ui_shopping_lists),
        dismissAction = {},
        action = {
            LoomButton(
                text = stringResource(Res.string.ui_new_list),
                onClick = { onListForm(null) },
                enabled = !busy,
                modifier = Modifier.padding(top = LoomSpacing.small),
            )
        },
    ) {
        GroupedSurface {
            lists.forEachIndexed { index, list ->
                key(list.id) {
                    if (index > 0)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        NavigationRow(
                            title = list.name,
                            glyph = Glyph.SHOPPING,
                            trailingGlyph =
                                if (list.id == selectedListId) Glyph.CHECK else Glyph.ARROW,
                            description =
                                if (list.archived) stringResource(Res.string.ui_archive) else null,
                            onClick = { onSelect(list.id) },
                            enabled = !busy && !list.archived,
                            modifier =
                                Modifier.weight(1f).testTag("list_${list.name}").semantics {
                                    selected = list.id == selectedListId
                                },
                        )
                        ActionMenu(
                            modifier = Modifier.testTag("list_actions_${list.name}"),
                            items =
                                listOf(
                                    ActionMenuItem(
                                        text = TextResource.Resource(Res.string.ui_rename),
                                        onClick = { onListForm(list) },
                                        enabled = !busy,
                                    ),
                                    ActionMenuItem(
                                        text =
                                            TextResource.Resource(
                                                if (list.archived) Res.string.ui_restore
                                                else Res.string.ui_archive,
                                            ),
                                        onClick = { onArchive(list) },
                                        enabled = !busy && (list.archived || activeLists.size > 1),
                                    ),
                                    ActionMenuItem(
                                        text = TextResource.Resource(Res.string.list_delete),
                                        onClick = { onDelete(list) },
                                        enabled = !busy,
                                        destructive = true,
                                    ),
                                ),
                        )
                    }
                }
            }
        }
        formError?.let { message ->
            ErrorNotice(message = message, onDismiss = onDismiss)
        }
    }
}
