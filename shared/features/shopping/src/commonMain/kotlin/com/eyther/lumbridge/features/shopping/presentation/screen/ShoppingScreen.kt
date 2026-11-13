package com.eyther.lumbridge.features.shopping.presentation.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.shopping.domain.model.*
import com.eyther.lumbridge.features.shopping.generated.resources.*
import com.eyther.lumbridge.features.shopping.presentation.localization.localizedMessage
import com.eyther.lumbridge.features.shopping.presentation.localization.localizedToken
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingScreen(vm: ShoppingScreenViewModelContract) {
    val state by vm.state.platformCollectAsStateOnStart()
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
        onSelectList = vm::selectList,
        onShowAdd = vm::showAdd,
        onName = vm::name,
        onQuantity = vm::quantity,
        onCategory = vm::category,
        onShowChecked = vm::showChecked,
        onSave = vm::add,
        onToggle = vm::toggle,
        onEdit = vm::edit,
        onRemove = vm::remove,
        onFavorite = vm::favorite,
        onMove = vm::move,
        onUndo = vm::undo,
        onReuse = vm::reuse,
        onShowLists = vm::showLists,
        onListForm = vm::listForm,
        onCloseListForm = vm::closeListForm,
        onListName = vm::listName,
        onSaveList = vm::saveList,
        onArchive = vm::archive,
        onDismiss = vm::dismiss,
        onRetry = vm::retry,
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
    error: String?,
    undoMessage: String?,
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
            ErrorNotice(localizedMessage(error), onDismiss)
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
                        localizedMessage(undoMessage).orEmpty(),
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
                        localizedToken(section).uppercase(),
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
                                    Text(localizedToken(option))
                                }
                            }
                    }
                    ErrorNotice(localizedMessage(error), onDismiss)
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
                    ErrorNotice(localizedMessage(error), onDismiss)
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
                    ErrorNotice(localizedMessage(error), onDismiss)
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
