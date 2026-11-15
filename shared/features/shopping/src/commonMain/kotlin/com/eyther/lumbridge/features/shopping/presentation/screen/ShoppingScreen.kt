package com.eyther.lumbridge.features.shopping.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.checklist_add
import com.eyther.lumbridge.features.shopping.generated.resources.checklist_add_item
import com.eyther.lumbridge.features.shopping.generated.resources.checklist_completed
import com.eyther.lumbridge.features.shopping.generated.resources.ui_refresh_and_retry
import com.eyther.lumbridge.features.shopping.generated.resources.ui_shopping_lists
import com.eyther.lumbridge.features.shopping.presentation.components.ShoppingItemRow
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingScreen(viewModel: ShoppingScreenViewModelContract) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerShoppingScreen(
        selectedListName = state.selectedList?.name.orEmpty(),
        uncheckedItems = state.uncheckedItems,
        completedItems = state.completedItems,
        moveTargets = state.moveTargets,
        showCompleted = state.showChecked,
        loading = state.loading,
        busy = state.busy,
        inlineName = state.inlineName,
        addingInline = state.addingInline,
        canAddInline = state.canAddInline,
        inlineError = state.inlineError,
        error = state.displayError,
        onName = viewModel::onInlineNameChanged,
        onAdd = viewModel::onAddInlineItem,
        onShowLists = { viewModel.onListsVisibilityChanged(true) },
        onShowCompleted = viewModel::onCheckedItemsVisibilityChanged,
        onToggle = viewModel::onToggleItem,
        onEdit = viewModel::onEditItem,
        onRemove = viewModel::onRemoveItem,
        onFavorite = viewModel::onToggleFavorite,
        onMove = viewModel::onMoveItem,
        onDismiss = viewModel::onDismissError,
        onRetry = viewModel::onRetry,
    )
}

@Composable
private fun InnerShoppingScreen(
    selectedListName: String,
    uncheckedItems: List<ShoppingItemDomain>,
    completedItems: List<ShoppingItemDomain>,
    moveTargets: List<ShoppingListDomain>,
    showCompleted: Boolean,
    loading: Boolean,
    busy: Boolean,
    inlineName: String,
    addingInline: Boolean,
    canAddInline: Boolean,
    inlineError: TextResource?,
    error: TextResource?,
    onName: (String) -> Unit,
    onAdd: () -> Unit,
    onShowLists: () -> Unit,
    onShowCompleted: (Boolean) -> Unit,
    onToggle: (ShoppingItemDomain) -> Unit,
    onEdit: (ShoppingItemDomain) -> Unit,
    onRemove: (ShoppingItemDomain) -> Unit,
    onFavorite: (ShoppingItemDomain) -> Unit,
    onMove: (ShoppingItemDomain, String) -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("shopping_content"),
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item {
            TextButton(onClick = onShowLists, modifier = Modifier.testTag("manage_lists")) {
                Text(
                    text =
                        selectedListName.ifBlank { stringResource(Res.string.ui_shopping_lists) },
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(" ⌄")
            }
        }
        error?.let { message ->
            item {
                ErrorNotice(message = message, onDismiss = onDismiss)
                TextButton(onClick = onRetry) {
                    Text(stringResource(Res.string.ui_refresh_and_retry))
                }
            }
        }
        if (loading) item { LoadingRows() }
        items(uncheckedItems, key = { it.id }) { item ->
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
        item(key = "inline_entry") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                OutlinedTextField(
                    value = inlineName,
                    onValueChange = onName,
                    modifier = Modifier.weight(1f).testTag("shopping_inline_input"),
                    placeholder = { Text(stringResource(Res.string.checklist_add_item)) },
                    singleLine = true,
                    isError = inlineError != null,
                    supportingText = inlineError?.let { message -> { Text(message.getString()) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onAdd() }),
                )
                TextButton(onClick = onAdd, enabled = canAddInline) {
                    if (addingInline) CircularProgressIndicator()
                    else Text(stringResource(Res.string.checklist_add))
                }
            }
        }
        if (completedItems.isNotEmpty()) {
            item {
                TextButton(
                    onClick = { onShowCompleted(!showCompleted) },
                    modifier = Modifier.testTag("shopping_completed"),
                ) {
                    Text(
                        text =
                            pluralStringResource(
                                Res.plurals.checklist_completed,
                                completedItems.size,
                                completedItems.size,
                            ),
                    )
                }
            }
            if (showCompleted)
                items(completedItems, key = { it.id }) { item ->
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
    }
}
