package com.eyther.lumbridge.features.shopping.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.DestructiveConfirmation
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.core.presentation.components.cardSegment
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
import com.eyther.lumbridge.features.shopping.generated.resources.action_undo
import com.eyther.lumbridge.features.shopping.generated.resources.completed_delete_confirm
import com.eyther.lumbridge.features.shopping.generated.resources.completed_delete_message
import com.eyther.lumbridge.features.shopping.generated.resources.completed_delete_title
import com.eyther.lumbridge.features.shopping.generated.resources.lists_create
import com.eyther.lumbridge.features.shopping.generated.resources.lists_empty
import com.eyther.lumbridge.features.shopping.generated.resources.lists_empty_description
import com.eyther.lumbridge.features.shopping.generated.resources.ui_cancel
import com.eyther.lumbridge.features.shopping.generated.resources.ui_refresh_and_retry
import com.eyther.lumbridge.features.shopping.presentation.components.CompletedShoppingActions
import com.eyther.lumbridge.features.shopping.presentation.components.CompletedShoppingSectionHeader
import com.eyther.lumbridge.features.shopping.presentation.components.PendingShoppingItemRow
import com.eyther.lumbridge.features.shopping.presentation.components.ShoppingDraftRow
import com.eyther.lumbridge.features.shopping.presentation.components.ShoppingEntryRow
import com.eyther.lumbridge.features.shopping.presentation.components.ShoppingItemRow
import com.eyther.lumbridge.features.shopping.presentation.components.ShoppingListChips
import com.eyther.lumbridge.features.shopping.presentation.model.ShoppingRowUi
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import org.jetbrains.compose.resources.stringResource

private val ChecklistRowSpacing = 0.dp // Rows join into one card; sections pad themselves.
private val CardInset = 8.dp

@Composable
fun ShoppingScreen(viewModel: ShoppingScreenViewModelContract) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    PullToRefreshContentContainer(
        isRefreshing = state.refreshing,
        onRefresh = viewModel::onRetry,
        snackbarHost = {
            SuccessSnackbar(
                message = state.undoMessage ?: state.successMessage,
                actionText =
                    if (state.undoMessage != null) stringResource(Res.string.action_undo) else null,
                actionEnabled = !state.busy && state.savingItemIds.isEmpty(),
                actionLoading = state.busy,
                onAction = viewModel::onUndo,
                onDismiss = viewModel::onDismissUndo,
            )
        },
    ) {
        InnerShoppingScreen(
            lists = state.activeLists,
            selectedListId = state.selectedList?.id.orEmpty(),
            onSelectList = viewModel::onListSelected,
            uncheckedRows = state.uncheckedRows,
            nestingParentIds = state.nestingParentIds,
            indentedItemIds = state.indentedItemIds,
            completedItems = state.completedItems,
            canMove = state.activeLists.size > 1,
            editingItemId = state.editingItemId,
            editingName = state.editingName,
            draftName = state.draftName,
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
            onShowFavorites = { viewModel.onFavoritesVisibilityChanged(true) },
            onShowCompleted = viewModel::onCheckedItemsVisibilityChanged,
            onToggle = viewModel::onToggleItem,
            onStartEditing = viewModel::onStartEditing,
            onEditingName = viewModel::onEditingNameChanged,
            onEditingNext = viewModel::onEditingNext,
            onEditingFinished = viewModel::onEditingFinished,
            onEditingDeleteEmpty = viewModel::onEditingDeleteEmpty,
            onDraftDeleteEmpty = viewModel::onDraftDeleteEmpty,
            onEntryDeleteEmpty = viewModel::onEntryDeleteEmpty,
            onDraftName = viewModel::onDraftNameChanged,
            onSubmitDraft = viewModel::onSubmitDraft,
            onDraftFinished = viewModel::onDraftFinished,
            onRemove = viewModel::onRemoveItem,
            onFavorite = viewModel::onToggleFavorite,
            onMove = viewModel::onMoveRequested,
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
    lists: ImmutableList<ShoppingListDomain>,
    selectedListId: String,
    onSelectList: (String) -> Unit,
    uncheckedRows: ImmutableList<ShoppingRowUi>,
    nestingParentIds: ImmutableMap<String, String>,
    indentedItemIds: ImmutableSet<String>,
    completedItems: ImmutableList<ShoppingItemDomain>,
    canMove: Boolean,
    editingItemId: String?,
    editingName: String,
    draftName: String,
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
    onShowFavorites: () -> Unit,
    onShowCompleted: (Boolean) -> Unit,
    onToggle: (ShoppingItemDomain) -> Unit,
    onStartEditing: (ShoppingItemDomain) -> Unit,
    onEditingName: (String) -> Unit,
    onEditingNext: () -> Unit,
    onEditingFinished: (String) -> Unit,
    onEditingDeleteEmpty: () -> Unit,
    onDraftDeleteEmpty: () -> Unit,
    onEntryDeleteEmpty: () -> Unit,
    onDraftName: (String) -> Unit,
    onSubmitDraft: () -> Unit,
    onDraftFinished: () -> Unit,
    onRemove: (ShoppingItemDomain) -> Unit,
    onFavorite: (ShoppingItemDomain) -> Unit,
    onMove: (ShoppingItemDomain) -> Unit,
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
            (if (loading) 1 else uncheckedRows.size)
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
            ShoppingListChips(
                lists = lists,
                selectedListId = selectedListId,
                favoritesEnabled = hasList && !busy && !loading,
                onSelect = onSelectList,
                onManage = onShowLists,
                onCreate = onCreateList,
                onFavorites = onShowFavorites,
                modifier = Modifier.fillMaxWidth().padding(bottom = LoomSpacing.content),
            )
        }
        error?.let { message ->
            item {
                ErrorNotice(message = message, onDismiss = onDismiss)
                LoomSecondaryButton(
                    text = stringResource(Res.string.ui_refresh_and_retry),
                    onClick = onRetry,
                    modifier = Modifier,
                )
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
            itemsIndexed(uncheckedRows, key = { _, row -> row.key }, contentType = { _, _ -> "checklist_item" }) { index, row ->
                Box(Modifier.cardSegment(first = index == 0, last = false).padding(horizontal = CardInset)) {
                when (row) {
                    is ShoppingRowUi.Item ->
                        ShoppingItemRow(
                            item = row.item,
                            busy = busy || row.item.id in savingItemIds,
                            canMove = canMove,
                            onToggle = onToggle,
                            onStartEditing = onStartEditing,
                            onRemove = onRemove,
                            onFavorite = onFavorite,
                            onMove = onMove,
                            editing = row.item.id == editingItemId,
                            editingName = editingName,
                            onEditingName = onEditingName,
                            onEditingNext = onEditingNext,
                            onEditingFinished = onEditingFinished,
                            onEditingDeleteEmpty = onEditingDeleteEmpty,
                            indented = row.indented,
                            canNest = row.item.id in nestingParentIds,
                            canUnnest = row.item.parentId != null,
                            onNest = onNest,
                            onUnnest = onUnnest,
                        )
                    is ShoppingRowUi.Pending ->
                        PendingShoppingItemRow(
                            name = row.entry.item.name,
                            error = row.entry.error,
                            indented = row.indented,
                            onRetry = { onRetryEntry(row.entry.item.id) },
                            onDiscard = { onDiscardEntry(row.entry.item.id) },
                        )
                    is ShoppingRowUi.Draft ->
                        ShoppingDraftRow(
                            value = draftName,
                            indented = row.indented,
                            onValue = onDraftName,
                            onNext = onSubmitDraft,
                            onFinished = onDraftFinished,
                            onDeleteEmpty = onDraftDeleteEmpty,
                        )
                }
                }
            }
        if (hasList)
            item(key = "inline_entry") {
                Column(
                    Modifier.cardSegment(first = loading || uncheckedRows.isEmpty(), last = true)
                        .padding(horizontal = CardInset)
                ) {
                ShoppingEntryRow(
                    value = inlineName,
                    enabled = !busy,
                    canSubmit = canAddInline,
                    onValue = onName,
                    onSubmit = onAdd,
                    onDeleteEmpty = onEntryDeleteEmpty,
                    modifier =
                        Modifier.bringIntoViewRequester(entryVisibility).onFocusChanged {
                            entryFocused = it.hasFocus
                        },
                )
                inlineError?.let {
                    Text(
                        it.getString(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 48.dp, bottom = 12.dp),
                    )
                }
                }
            }
        if (!loading && completedItems.isNotEmpty()) {
            item(key = "completed_header") {
                CompletedShoppingSectionHeader(
                    modifier = Modifier.padding(top = LoomSpacing.small, bottom = 4.dp),
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
                itemsIndexed(completedItems, key = { _, item -> item.id }) { index, item ->
                    Box(
                        Modifier.cardSegment(first = index == 0, last = index == completedItems.lastIndex)
                            .padding(horizontal = CardInset)
                    ) {
                    ShoppingItemRow(
                        item = item,
                        busy = busy || item.id in savingItemIds,
                        canMove = canMove,
                        selectionMode = selectingCompleted,
                        selected = item.id in selectedCompletedIds,
                        onSelect = { onSelectCompleted(item.id) },
                        onToggle = onToggle,
                        onStartEditing = onStartEditing,
                        onRemove = onRemove,
                        onFavorite = onFavorite,
                        onMove = onMove,
                        editing = item.id == editingItemId,
                        editingName = editingName,
                        onEditingName = onEditingName,
                        onEditingNext = onEditingNext,
                        onEditingFinished = onEditingFinished,
                        onEditingDeleteEmpty = onEditingDeleteEmpty,
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
}
