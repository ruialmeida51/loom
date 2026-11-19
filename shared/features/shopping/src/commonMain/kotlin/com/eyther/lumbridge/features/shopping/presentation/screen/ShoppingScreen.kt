package com.eyther.lumbridge.features.shopping.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.DestructiveConfirmation
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.SelectorButton
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.components.layout.rootContentPadding
import com.eyther.lumbridge.core.presentation.components.snackbar.SuccessSnackbar
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.model.DestructiveConfirmationUi
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.completed_delete_confirm
import com.eyther.lumbridge.features.shopping.generated.resources.completed_delete_message
import com.eyther.lumbridge.features.shopping.generated.resources.completed_delete_title
import com.eyther.lumbridge.features.shopping.generated.resources.lists_create
import com.eyther.lumbridge.features.shopping.generated.resources.lists_empty
import com.eyther.lumbridge.features.shopping.generated.resources.lists_empty_description
import com.eyther.lumbridge.features.shopping.generated.resources.ui_cancel
import com.eyther.lumbridge.features.shopping.generated.resources.ui_refresh_and_retry
import com.eyther.lumbridge.features.shopping.generated.resources.ui_shopping_lists
import com.eyther.lumbridge.features.shopping.presentation.components.CompletedShoppingActions
import com.eyther.lumbridge.features.shopping.presentation.components.CompletedShoppingSectionHeader
import com.eyther.lumbridge.features.shopping.presentation.components.PendingShoppingItemRow
import com.eyther.lumbridge.features.shopping.presentation.components.ShoppingEntryRow
import com.eyther.lumbridge.features.shopping.presentation.components.ShoppingItemRow
import com.eyther.lumbridge.features.shopping.presentation.model.ShoppingEntryUi
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import org.jetbrains.compose.resources.stringResource

private val ChecklistRowSpacing =
    2.dp // Keep checklist rows dense while the header uses the root rhythm.

@Composable
fun ShoppingScreen(viewModel: ShoppingScreenViewModelContract) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    PullToRefreshContentContainer(
        isRefreshing = state.refreshing,
        onRefresh = viewModel::onRetry,
        snackbarHost = { SuccessSnackbar(message = state.successMessage) },
    ) {
        InnerShoppingScreen(
            selectedListName = state.selectedList?.name.orEmpty(),
            uncheckedItems = state.uncheckedItems,
            nestingParentIds = state.nestingParentIds,
            indentedItemIds = state.indentedItemIds,
            completedItems = state.completedItems,
            moveTargets = state.moveTargets,
            showCompleted = state.showChecked,
            selectingCompleted = state.selectingCompleted,
            selectedCompletedIds = state.selectedCompletedIds,
            selectedCount = state.selectedCompletedItems.size,
            allCompletedSelected = state.allCompletedSelected,
            onSelectingCompleted = viewModel::onCompletedSelectionChanged,
            onSelectCompleted = viewModel::onCompletedItemSelected,
            onSelectAllCompleted = viewModel::onSelectAllCompleted,
            onDeleteCompleted = viewModel::onDeleteCompletedRequested,
            loading = state.loading,
            busy = state.busy || state.deletingCompleted,
            savingItemIds = state.savingItemIds,
            inlineName = state.inlineName,
            pendingEntries = state.visiblePendingEntries,
            entryRevision = state.entryRevision,
            hasList = state.selectedList != null,
            canAddInline = state.canAddInline,
            inlineError = state.inlineError,
            error = state.displayError,
            onName = viewModel::onInlineNameChanged,
            onAdd = viewModel::onAddInlineItem,
            onRetryEntry = viewModel::onRetryEntry,
            onDiscardEntry = viewModel::onDiscardEntry,
            onCreateList = { viewModel.onOpenListForm(null) },
            onShowLists = { viewModel.onListsVisibilityChanged(true) },
            onShowCompleted = viewModel::onCheckedItemsVisibilityChanged,
            onToggle = viewModel::onToggleItem,
            onEdit = viewModel::onEditItem,
            onRemove = viewModel::onRemoveItem,
            onFavorite = viewModel::onToggleFavorite,
            onMove = viewModel::onMoveItem,
            onNest = viewModel::onNestItem,
            onUnnest = viewModel::onUnnestItem,
            onDismiss = viewModel::onDismissError,
            onRetry = viewModel::onRetry,
        )
    }
    if (state.confirmingCompletedDeletion)
        DestructiveConfirmation(
            confirmation =
                DestructiveConfirmationUi(
                    title = TextResource.Resource(Res.string.completed_delete_title),
                    message =
                        TextResource.Resource(
                            Res.string.completed_delete_message,
                            args = listOf(state.pendingCompletedDeletion.size),
                        ),
                    confirmText = TextResource.Resource(Res.string.completed_delete_confirm),
                    cancelText = TextResource.Resource(Res.string.ui_cancel),
                ),
            busy = state.deletingCompleted,
            confirmationTag = "confirm_delete_completed",
            onConfirm = viewModel::onDeleteCompletedConfirmed,
            onDismiss = viewModel::onDeleteCompletedCancelled,
        )
    state.itemDeleteConfirmation?.let { confirmation ->
        DestructiveConfirmation(
            confirmation = confirmation,
            busy = state.deletingSingleItem,
            error = state.itemDeleteError,
            confirmationTag = "confirm_delete_grocery",
            onConfirm = viewModel::onRemoveItemConfirmed,
            onDismiss = viewModel::onRemoveItemCancelled,
        )
    }
}

@Composable
private fun InnerShoppingScreen(
    selectedListName: String,
    uncheckedItems: ImmutableList<ShoppingItemDomain>,
    nestingParentIds: ImmutableMap<String, String>,
    indentedItemIds: ImmutableSet<String>,
    completedItems: ImmutableList<ShoppingItemDomain>,
    moveTargets: ImmutableList<ShoppingListDomain>,
    showCompleted: Boolean,
    selectingCompleted: Boolean,
    selectedCompletedIds: ImmutableSet<String>,
    selectedCount: Int,
    allCompletedSelected: Boolean,
    onSelectingCompleted: (Boolean) -> Unit,
    onSelectCompleted: (String) -> Unit,
    onSelectAllCompleted: () -> Unit,
    onDeleteCompleted: () -> Unit,
    loading: Boolean,
    busy: Boolean,
    savingItemIds: ImmutableSet<String>,
    inlineName: String,
    pendingEntries: ImmutableList<ShoppingEntryUi>,
    entryRevision: Long,
    hasList: Boolean,
    canAddInline: Boolean,
    inlineError: TextResource?,
    error: TextResource?,
    onName: (String) -> Unit,
    onAdd: () -> Unit,
    onRetryEntry: (String) -> Unit,
    onDiscardEntry: (String) -> Unit,
    onCreateList: () -> Unit,
    onShowLists: () -> Unit,
    onShowCompleted: (Boolean) -> Unit,
    onToggle: (ShoppingItemDomain) -> Unit,
    onEdit: (ShoppingItemDomain) -> Unit,
    onRemove: (ShoppingItemDomain) -> Unit,
    onFavorite: (ShoppingItemDomain) -> Unit,
    onMove: (ShoppingItemDomain, String) -> Unit,
    onNest: (ShoppingItemDomain) -> Unit,
    onUnnest: (ShoppingItemDomain) -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
) {
    val listState = rememberLazyListState()
    val entryVisibility = remember { BringIntoViewRequester() }
    var entryFocused by remember { mutableStateOf(false) }
    val keyboardHeight = WindowInsets.ime.getBottom(LocalDensity.current)
    val entryIndex =
        1 +
            (if (error != null) 1 else 0) +
            (if (loading) 1 else uncheckedItems.size) +
            pendingEntries.size
    LaunchedEffect(entryRevision, keyboardHeight, entryFocused) {
        if (entryFocused && !loading && hasList) {
            listState.animateScrollToItem(entryIndex)
            withFrameNanos {}
            entryVisibility.bringIntoView()
        }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().imePadding().testTag("shopping_content"),
        contentPadding = rootContentPadding(),
        verticalArrangement = Arrangement.spacedBy(ChecklistRowSpacing),
    ) {
        item(key = "list_selector") {
            SelectorButton(
                contentPadding = PaddingValues(),
                text = selectedListName.ifBlank { stringResource(Res.string.ui_shopping_lists) },
                onClick = onShowLists,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(bottom = LoomSpacing.content - ChecklistRowSpacing)
                        .testTag("manage_lists"),
            )
        }
        error?.let { message ->
            item {
                ErrorNotice(message = message, onDismiss = onDismiss)
                OutlinedButton(onClick = onRetry) {
                    Text(stringResource(Res.string.ui_refresh_and_retry))
                }
            }
        }
        if (loading) item(key = "loading") { LoadingRows() }
        else if (!hasList && error == null)
            item(key = "empty_lists") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(Res.string.lists_empty),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        stringResource(Res.string.lists_empty_description),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LoomButton(
                        text = stringResource(Res.string.lists_create),
                        onClick = onCreateList,
                    )
                }
            }
        if (!loading)
            items(uncheckedItems, key = { it.id }, contentType = { "checklist_item" }) { item ->
                ShoppingItemRow(
                    item = item,
                    moveTargets = moveTargets,
                    busy = busy || item.id in savingItemIds,
                    onToggle = onToggle,
                    onEdit = onEdit,
                    onRemove = onRemove,
                    onFavorite = onFavorite,
                    onMove = onMove,
                    indented = item.id in indentedItemIds,
                    canNest = item.id in nestingParentIds,
                    canUnnest = item.parentId != null,
                    onNest = onNest,
                    onUnnest = onUnnest,
                )
            }
        if (hasList)
            items(pendingEntries, key = { it.item.id }, contentType = { "checklist_item" }) { entry
                ->
                PendingShoppingItemRow(
                    name = entry.item.name,
                    error = entry.error,
                    onRetry = { onRetryEntry(entry.item.id) },
                    onDiscard = { onDiscardEntry(entry.item.id) },
                )
            }
        if (hasList)
            item(key = "inline_entry") {
                ShoppingEntryRow(
                    value = inlineName,
                    enabled = !busy,
                    canSubmit = canAddInline,
                    onValue = onName,
                    onSubmit = onAdd,
                    modifier =
                        Modifier.bringIntoViewRequester(entryVisibility).onFocusChanged {
                            entryFocused = it.hasFocus
                        },
                )
                inlineError?.let { Text(it.getString(), color = MaterialTheme.colorScheme.error) }
            }
        if (!loading && completedItems.isNotEmpty()) {
            item(key = "completed_header") {
                CompletedShoppingSectionHeader(
                    count = completedItems.size,
                    expanded = showCompleted,
                    selecting = selectingCompleted,
                    busy = busy,
                    onExpanded = onShowCompleted,
                    onSelecting = onSelectingCompleted,
                )
            }
            if (selectingCompleted)
                item(key = "completed_actions") {
                    CompletedShoppingActions(
                        selectedCount = selectedCount,
                        allSelected = allCompletedSelected,
                        busy = busy,
                        onSelectAll = onSelectAllCompleted,
                        onDelete = onDeleteCompleted,
                    )
                }
            if (showCompleted)
                items(completedItems, key = { it.id }) { item ->
                    ShoppingItemRow(
                        item = item,
                        moveTargets = moveTargets,
                        busy = busy || item.id in savingItemIds,
                        selectionMode = selectingCompleted,
                        selected = item.id in selectedCompletedIds,
                        onSelect = { onSelectCompleted(item.id) },
                        onToggle = onToggle,
                        onEdit = onEdit,
                        onRemove = onRemove,
                        onFavorite = onFavorite,
                        onMove = onMove,
                        indented = item.id in indentedItemIds,
                        canNest = item.id in nestingParentIds,
                        canUnnest = item.parentId != null,
                        onNest = onNest,
                        onUnnest = onUnnest,
                    )
                }
        }
    }
}
