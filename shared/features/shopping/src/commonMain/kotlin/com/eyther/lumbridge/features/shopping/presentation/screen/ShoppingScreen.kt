package com.eyther.lumbridge.features.shopping.presentation.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.EmptyNotice
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.LoomCard
import com.eyther.lumbridge.core.presentation.components.PageHeading
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.items_to_pick_up
import com.eyther.lumbridge.features.shopping.generated.resources.ui_add_an_item
import com.eyther.lumbridge.features.shopping.generated.resources.ui_add_item
import com.eyther.lumbridge.features.shopping.generated.resources.ui_add_to_the_list
import com.eyther.lumbridge.features.shopping.generated.resources.ui_all_items
import com.eyther.lumbridge.features.shopping.generated.resources.ui_all_picked_up
import com.eyther.lumbridge.features.shopping.generated.resources.ui_archive
import com.eyther.lumbridge.features.shopping.generated.resources.ui_buy_again
import com.eyther.lumbridge.features.shopping.generated.resources.ui_cancel
import com.eyther.lumbridge.features.shopping.generated.resources.ui_category
import com.eyther.lumbridge.features.shopping.generated.resources.ui_done
import com.eyther.lumbridge.features.shopping.generated.resources.ui_edit
import com.eyther.lumbridge.features.shopping.generated.resources.ui_edit_item
import com.eyther.lumbridge.features.shopping.generated.resources.ui_favorite_add
import com.eyther.lumbridge.features.shopping.generated.resources.ui_favorite_remove
import com.eyther.lumbridge.features.shopping.generated.resources.ui_list_name
import com.eyther.lumbridge.features.shopping.generated.resources.ui_manage_lists
import com.eyther.lumbridge.features.shopping.generated.resources.ui_move
import com.eyther.lumbridge.features.shopping.generated.resources.ui_name_your_list
import com.eyther.lumbridge.features.shopping.generated.resources.ui_new_list
import com.eyther.lumbridge.features.shopping.generated.resources.ui_one_item_per_line_for_quick_entry
import com.eyther.lumbridge.features.shopping.generated.resources.ui_one_less_thing_to_remember
import com.eyther.lumbridge.features.shopping.generated.resources.ui_quantity_or_note
import com.eyther.lumbridge.features.shopping.generated.resources.ui_refresh_and_retry
import com.eyther.lumbridge.features.shopping.generated.resources.ui_remove
import com.eyther.lumbridge.features.shopping.generated.resources.ui_rename
import com.eyther.lumbridge.features.shopping.generated.resources.ui_restore
import com.eyther.lumbridge.features.shopping.generated.resources.ui_save_item
import com.eyther.lumbridge.features.shopping.generated.resources.ui_save_list
import com.eyther.lumbridge.features.shopping.generated.resources.ui_shopping_lists
import com.eyther.lumbridge.features.shopping.generated.resources.ui_the_shopping_list
import com.eyther.lumbridge.features.shopping.generated.resources.ui_to_pick_up
import com.eyther.lumbridge.features.shopping.generated.resources.ui_undo
import com.eyther.lumbridge.features.shopping.generated.resources.ui_what_do_we_need
import com.eyther.lumbridge.features.shopping.generated.resources.ui_your_list_is_clear_add_something_whenever_you_think
import com.eyther.lumbridge.features.shopping.presentation.mapper.shoppingCategoryLabel
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingScreen(viewModel: ShoppingScreenViewModelContract) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerShoppingScreen(
        lists = state.snapshot.lists,
        items = state.items,
        suggestions = state.snapshot.suggestions(state.selectedListId),
        selectedListId = state.selectedListId,
        selectedListName = state.selectedList?.name.orEmpty(),
        name = state.name,
        quantity = state.quantity,
        category = state.category,
        editing = state.editing,
        showAdd = state.showAdd,
        showChecked = state.showChecked,
        showLists = state.showLists,
        showListForm = state.showListForm,
        listName = state.listName,
        busy = state.busy,
        error = state.error,
        undoMessage = state.undoMessage,
        onSelectList = viewModel::onListSelected,
        onShowAdd = viewModel::onAddSheetVisibilityChanged,
        onName = viewModel::onItemNameChanged,
        onQuantity = viewModel::onQuantityChanged,
        onCategory = viewModel::onCategoryChanged,
        onShowChecked = viewModel::onCheckedItemsVisibilityChanged,
        onSave = viewModel::onSaveItem,
        onToggle = viewModel::onToggleItem,
        onEdit = viewModel::onEditItem,
        onRemove = viewModel::onRemoveItem,
        onFavorite = viewModel::onToggleFavorite,
        onMove = viewModel::onMoveItem,
        onUndo = viewModel::onUndo,
        onReuse = viewModel::onReuseItem,
        onShowLists = viewModel::onListsVisibilityChanged,
        onListForm = viewModel::onOpenListForm,
        onCloseListForm = viewModel::onCloseListForm,
        onListName = viewModel::onListNameChanged,
        onSaveList = viewModel::onSaveList,
        onArchive = viewModel::onArchiveList,
        onDismiss = viewModel::onDismissError,
        onRetry = viewModel::onRetry,
    )
}

@Composable
private fun InnerShoppingScreen(
    lists: List<ShoppingListDomain>,
    items: List<ShoppingItemDomain>,
    suggestions: List<ShoppingItemDomain>,
    selectedListId: String,
    selectedListName: String,
    name: String,
    quantity: String,
    category: String,
    editing: Boolean,
    showAdd: Boolean,
    showChecked: Boolean,
    showLists: Boolean,
    showListForm: Boolean,
    listName: String,
    busy: Boolean,
    error: TextResource?,
    undoMessage: TextResource?,
    onSelectList: (String) -> Unit,
    onShowAdd: (Boolean) -> Unit,
    onName: (String) -> Unit,
    onQuantity: (String) -> Unit,
    onCategory: (String) -> Unit,
    onShowChecked: (Boolean) -> Unit,
    onSave: () -> Unit,
    onToggle: (ShoppingItemDomain) -> Unit,
    onEdit: (ShoppingItemDomain) -> Unit,
    onRemove: (ShoppingItemDomain) -> Unit,
    onFavorite: (ShoppingItemDomain) -> Unit,
    onMove: (ShoppingItemDomain, String) -> Unit,
    onUndo: () -> Unit,
    onReuse: (ShoppingItemDomain) -> Unit,
    onShowLists: (Boolean) -> Unit,
    onListForm: (ShoppingListDomain?) -> Unit,
    onCloseListForm: () -> Unit,
    onListName: (String) -> Unit,
    onSaveList: () -> Unit,
    onArchive: (ShoppingListDomain) -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
) {
    val activeLists = lists.filterNot { it.archived }.sortedBy { it.name.lowercase() }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 24.dp).testTag("shopping_content"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 28.dp),
    ) {
        item {
            PageHeading(
                stringResource(Res.string.ui_one_less_thing_to_remember),
                stringResource(Res.string.ui_the_shopping_list),
                pluralStringResource(
                    Res.plurals.items_to_pick_up,
                    items.count { !it.checked },
                    items.count { !it.checked },
                ),
            )
        }
        item {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                activeLists.forEach { list ->
                    FilterChip(
                        list.id == selectedListId,
                        { onSelectList(list.id) },
                        label = { Text(list.name) },
                        modifier = Modifier.testTag("list_${list.name}"),
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(selectedListName, style = MaterialTheme.typography.titleMedium)
                TextButton(
                    onClick = { onShowLists(true) },
                    modifier = Modifier.testTag("manage_lists"),
                ) {
                    Text(stringResource(Res.string.ui_manage_lists))
                }
            }
        }
        item {
            ErrorNotice(error, onDismiss)
            if (error != null)
                TextButton(onClick = onRetry) {
                    Text(stringResource(Res.string.ui_refresh_and_retry))
                }
            if (undoMessage != null)
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        undoMessage.getString(),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    TextButton(onClick = onUndo, enabled = !busy) {
                        Text(stringResource(Res.string.ui_undo))
                    }
                }
        }
        item {
            LoomButton(
                stringResource(Res.string.ui_add_an_item),
                { onShowAdd(true) },
                Modifier.fillMaxWidth(),
                enabled = activeLists.isNotEmpty() && !busy,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    !showChecked,
                    { onShowChecked(false) },
                    label = { Text(stringResource(Res.string.ui_to_pick_up)) },
                )
                FilterChip(
                    showChecked,
                    { onShowChecked(true) },
                    label = { Text(stringResource(Res.string.ui_all_items)) },
                )
            }
        }
        val visible = items.filter { showChecked || !it.checked }
        if (visible.isEmpty())
            item {
                EmptyNotice(
                    stringResource(Res.string.ui_all_picked_up),
                    stringResource(
                        Res.string.ui_your_list_is_clear_add_something_whenever_you_think,
                    ),
                )
            }
        visible
            .groupBy { it.category }
            .forEach { (section, rows) ->
                item {
                    Text(
                        shoppingCategoryLabel(section).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                items(rows, key = { it.id }) { item ->
                    val favoriteLabel =
                        stringResource(
                            if (item.favorite) Res.string.ui_favorite_remove
                            else Res.string.ui_favorite_add,
                        )
                    var moving by remember(item.id) { mutableStateOf(false) }
                    LoomCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                item.checked,
                                { onToggle(item) },
                                enabled = !busy,
                                modifier = Modifier.testTag("check_${item.name}"),
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    textDecoration =
                                        if (item.checked) TextDecoration.LineThrough
                                        else TextDecoration.None,
                                )
                                if (item.quantity.isNotBlank())
                                    Text(
                                        item.quantity,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                            }
                            TextButton(
                                onClick = { onFavorite(item) },
                                enabled = !busy,
                                modifier =
                                    Modifier.testTag("favorite_${item.name}").semantics {
                                        contentDescription = favoriteLabel
                                    },
                            ) {
                                Text(if (item.favorite) "★" else "☆")
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(
                                onClick = { onEdit(item) },
                                enabled = !busy,
                                modifier = Modifier.testTag("edit_${item.name}"),
                            ) {
                                Text(stringResource(Res.string.ui_edit))
                            }
                            if (activeLists.size > 1)
                                Box {
                                    TextButton(onClick = { moving = true }, enabled = !busy) {
                                        Text(stringResource(Res.string.ui_move))
                                    }
                                    DropdownMenu(moving, { moving = false }) {
                                        activeLists
                                            .filterNot { it.id == item.listId }
                                            .forEach { list ->
                                                DropdownMenuItem(
                                                    text = { Text(list.name) },
                                                    onClick = {
                                                        moving = false
                                                        onMove(item, list.id)
                                                    },
                                                )
                                            }
                                    }
                                }
                            TextButton(
                                onClick = { onRemove(item) },
                                enabled = !busy,
                                modifier = Modifier.testTag("remove_${item.name}"),
                            ) {
                                Text(stringResource(Res.string.ui_remove))
                            }
                        }
                    }
                }
            }
        if (suggestions.isNotEmpty())
            item {
                Text(
                    stringResource(Res.string.ui_buy_again),
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    suggestions.forEach { item ->
                        SuggestionChip(
                            onClick = { onReuse(item) },
                            label = { Text(item.name) },
                            enabled = !busy,
                        )
                    }
                }
            }
    }
    if (showAdd)
        AlertDialog(
            onDismissRequest = { if (!busy) onShowAdd(false) },
            title = {
                Text(
                    if (editing) stringResource(Res.string.ui_edit_item)
                    else stringResource(Res.string.ui_add_to_the_list),
                )
            },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()).widthIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        name,
                        onName,
                        label = { Text(stringResource(Res.string.ui_what_do_we_need)) },
                        singleLine = editing,
                        maxLines = if (editing) 1 else 5,
                    )
                    if (!editing)
                        Text(
                            stringResource(Res.string.ui_one_item_per_line_for_quick_entry),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    OutlinedTextField(
                        quantity,
                        onQuantity,
                        label = { Text(stringResource(Res.string.ui_quantity_or_note)) },
                        singleLine = true,
                    )
                    Text(
                        stringResource(Res.string.ui_category),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Column {
                        listOf("Produce", "Bakery", "Dairy & eggs", "Pantry", "Household")
                            .forEach { option ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        category == option,
                                        { onCategory(option) },
                                        modifier = Modifier.testTag("category_$option"),
                                    )
                                    Text(shoppingCategoryLabel(option))
                                }
                            }
                    }
                    ErrorNotice(error, onDismiss)
                }
            },
            confirmButton = {
                LoomButton(
                    if (editing) stringResource(Res.string.ui_save_item)
                    else stringResource(Res.string.ui_add_item),
                    onSave,
                    loading = busy,
                )
            },
            dismissButton = {
                TextButton(onClick = { onShowAdd(false) }, enabled = !busy) {
                    Text(stringResource(Res.string.ui_cancel))
                }
            },
        )
    if (showLists)
        AlertDialog(
            onDismissRequest = { onShowLists(false) },
            title = { Text(stringResource(Res.string.ui_shopping_lists)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    lists
                        .sortedBy { it.name.lowercase() }
                        .forEach { list ->
                            Text(list.name, style = MaterialTheme.typography.titleMedium)
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
                    ErrorNotice(error, onDismiss)
                }
            },
            confirmButton = {
                TextButton(onClick = { onListForm(null) }, enabled = !busy) {
                    Text(stringResource(Res.string.ui_new_list))
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowLists(false) }) {
                    Text(stringResource(Res.string.ui_done))
                }
            },
        )
    if (showListForm)
        AlertDialog(
            onDismissRequest = { if (!busy) onCloseListForm() },
            title = { Text(stringResource(Res.string.ui_name_your_list)) },
            text = {
                Column {
                    OutlinedTextField(
                        listName,
                        onListName,
                        label = { Text(stringResource(Res.string.ui_list_name)) },
                        singleLine = true,
                    )
                    ErrorNotice(error, onDismiss)
                }
            },
            confirmButton = {
                LoomButton(stringResource(Res.string.ui_save_list), onSaveList, loading = busy)
            },
            dismissButton = {
                TextButton(onClick = onCloseListForm, enabled = !busy) {
                    Text(stringResource(Res.string.ui_cancel))
                }
            },
        )
}
