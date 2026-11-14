package com.eyther.lumbridge.features.shopping.presentation.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.EmptyNotice
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.PageHeading
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.items_to_pick_up
import com.eyther.lumbridge.features.shopping.generated.resources.ui_add_an_item
import com.eyther.lumbridge.features.shopping.generated.resources.ui_all_items
import com.eyther.lumbridge.features.shopping.generated.resources.ui_all_picked_up
import com.eyther.lumbridge.features.shopping.generated.resources.ui_buy_again
import com.eyther.lumbridge.features.shopping.generated.resources.ui_manage_lists
import com.eyther.lumbridge.features.shopping.generated.resources.ui_refresh_and_retry
import com.eyther.lumbridge.features.shopping.generated.resources.ui_to_pick_up
import com.eyther.lumbridge.features.shopping.generated.resources.ui_undo
import com.eyther.lumbridge.features.shopping.generated.resources.ui_your_list_is_clear_add_something_whenever_you_think
import com.eyther.lumbridge.features.shopping.presentation.components.ShoppingItemRow
import com.eyther.lumbridge.features.shopping.presentation.mapper.shoppingCategoryLabel
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingScreen(viewModel: ShoppingScreenViewModelContract) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerShoppingScreen(
        activeLists = state.activeLists,
        moveTargets = state.moveTargets,
        sections = state.sections,
        remainingCount = state.remainingCount,
        suggestions = state.suggestions,
        selectedListId = state.selectedListId,
        selectedListName = state.selectedList?.name.orEmpty(),
        showChecked = state.showChecked,
        busy = state.busy,
        error = state.displayError,
        undoMessage = state.undoMessage,
        onSelectList = viewModel::onListSelected,
        onShowAdd = viewModel::onAddSheetVisibilityChanged,
        onShowChecked = viewModel::onCheckedItemsVisibilityChanged,
        onToggle = viewModel::onToggleItem,
        onEdit = viewModel::onEditItem,
        onRemove = viewModel::onRemoveItem,
        onFavorite = viewModel::onToggleFavorite,
        onMove = viewModel::onMoveItem,
        onUndo = viewModel::onUndo,
        onReuse = viewModel::onReuseItem,
        onShowLists = viewModel::onListsVisibilityChanged,
        onDismiss = viewModel::onDismissError,
        onRetry = viewModel::onRetry,
    )
}

@Composable
private fun InnerShoppingScreen(
    activeLists: List<ShoppingListDomain>,
    moveTargets: List<ShoppingListDomain>,
    sections: Map<String, List<ShoppingItemDomain>>,
    remainingCount: Int,
    suggestions: List<ShoppingItemDomain>,
    selectedListId: String,
    selectedListName: String,
    showChecked: Boolean,
    busy: Boolean,
    error: TextResource?,
    undoMessage: TextResource?,
    onSelectList: (String) -> Unit,
    onShowAdd: (Boolean) -> Unit,
    onShowChecked: (Boolean) -> Unit,
    onToggle: (ShoppingItemDomain) -> Unit,
    onEdit: (ShoppingItemDomain) -> Unit,
    onRemove: (ShoppingItemDomain) -> Unit,
    onFavorite: (ShoppingItemDomain) -> Unit,
    onMove: (ShoppingItemDomain, String) -> Unit,
    onUndo: () -> Unit,
    onReuse: (ShoppingItemDomain) -> Unit,
    onShowLists: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp).testTag("shopping_content"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 28.dp),
    ) {
        item {
            PageHeading(
                title = selectedListName,
                subtitle =
                    pluralStringResource(
                        Res.plurals.items_to_pick_up,
                        remainingCount,
                        remainingCount,
                    ),
                action = {
                    TextButton(
                        onClick = { onShowLists(true) },
                        modifier = Modifier.testTag("manage_lists"),
                    ) {
                        Text(stringResource(Res.string.ui_manage_lists))
                    }
                },
            )
        }
        if (activeLists.size > 1) {
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    activeLists.forEach { list ->
                        FilterChip(
                            selected = list.id == selectedListId,
                            onClick = { onSelectList(list.id) },
                            label = { Text(list.name) },
                            modifier = Modifier.testTag("list_${list.name}"),
                        )
                    }
                }
            }
        }
        if (error != null || undoMessage != null) {
            item {
                error?.let { message ->
                    ErrorNotice(message = message, onDismiss = onDismiss)
                }
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
        if (sections.isEmpty())
            item {
                EmptyNotice(
                    stringResource(Res.string.ui_all_picked_up),
                    stringResource(
                        Res.string.ui_your_list_is_clear_add_something_whenever_you_think,
                    ),
                )
            }
        sections.forEach { (section, rows) ->
            item {
                Text(
                    shoppingCategoryLabel(section).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            items(rows, key = { it.id }) { item ->
                ShoppingItemRow(
                    item = item,
                    moveTargets = moveTargets,
                    busy = busy,
                    onToggle = onToggle,
                    onEdit = onEdit,
                    onRemove = onRemove,
                    onFavorite = onFavorite,
                    onMove = onMove,
                )
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
}
