package com.eyther.lumbridge.features.shopping.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.ui_archive
import com.eyther.lumbridge.features.shopping.generated.resources.ui_done
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
    onShowLists: (Boolean) -> Unit,
    onListForm: (ShoppingListDomain?) -> Unit,
    onArchive: (ShoppingListDomain) -> Unit,
    onDismiss: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.ui_shopping_lists),
        action = {
            TextButton(onClick = { onListForm(null) }, enabled = !busy) {
                Text(stringResource(Res.string.ui_new_list))
            }
        },
        dismissAction = {
            TextButton(onClick = { onShowLists(false) }, enabled = !busy) {
                Text(stringResource(Res.string.ui_done))
            }
        },
    ) {
        lists.forEach { list ->
            TextButton(
                onClick = { onSelect(list.id) },
                enabled = !busy && !list.archived,
                modifier = Modifier.testTag("list_${list.name}"),
            ) {
                Text(list.name, style = MaterialTheme.typography.titleMedium)
                if (list.id == selectedListId) Text(" ✓")
            }
            Row {
                TextButton(
                    onClick = { onListForm(list) },
                    enabled = !busy,
                    modifier = Modifier.testTag("rename_${list.name}"),
                ) {
                    Text(stringResource(Res.string.ui_rename))
                }
                TextButton(
                    onClick = { onArchive(list) },
                    enabled = !busy && (list.archived || activeLists.size > 1),
                    modifier = Modifier.testTag("archive_${list.name}"),
                ) {
                    Text(
                        if (list.archived) stringResource(Res.string.ui_restore)
                        else stringResource(Res.string.ui_archive),
                    )
                }
            }
        }
        formError?.let { message ->
            ErrorNotice(message = message, onDismiss = onDismiss)
        }
    }
}
