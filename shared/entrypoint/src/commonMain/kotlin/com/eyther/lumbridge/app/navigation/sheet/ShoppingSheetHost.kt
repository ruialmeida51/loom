package com.eyther.lumbridge.app.navigation.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.app.navigation.model.sheet.ShoppingSheets
import com.eyther.lumbridge.core.presentation.components.DestructiveConfirmation
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.shopping.presentation.components.bottomsheet.ShoppingFavoritesBottomSheet
import com.eyther.lumbridge.features.shopping.presentation.components.bottomsheet.ShoppingItemBottomSheet
import com.eyther.lumbridge.features.shopping.presentation.components.bottomsheet.ShoppingListNameBottomSheet
import com.eyther.lumbridge.features.shopping.presentation.components.bottomsheet.ShoppingListsBottomSheet
import com.eyther.lumbridge.features.shopping.presentation.components.bottomsheet.ShoppingMoveBottomSheet
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract

@Composable
internal fun ShoppingSheetHost(
    viewModel: ShoppingScreenViewModelContract,
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    val controller = rememberBottomSheetController()
    val requestedSheet =
        when {
            state.showListForm -> ShoppingSheets.ShoppingListName
            state.showLists -> ShoppingSheets.ShoppingLists
            state.showAdd -> ShoppingSheets.ShoppingItem
            state.showFavorites -> ShoppingSheets.ShoppingFavorites
            state.movingItem != null -> ShoppingSheets.ShoppingMove
            else -> null
        }
    LaunchedEffect(requestedSheet) {
        if (requestedSheet == null) controller.hideBottomSheet()
        else controller.showBottomSheet(requestedSheet)
    }
    controller.registerBottomSheet<ShoppingSheets.ShoppingItem> {
        ShoppingItemBottomSheet(
            name = state.name,
            quantity = state.quantity,
            category = state.category,
            busy = state.busy,
            formError = state.formError,
            onShowAdd = viewModel::onAddSheetVisibilityChanged,
            onName = viewModel::onItemNameChanged,
            onQuantity = viewModel::onQuantityChanged,
            onCategory = viewModel::onCategoryChanged,
            onSave = viewModel::onSaveItem,
            onDismiss = viewModel::onDismissError,
        )
    }
    controller.registerBottomSheet<ShoppingSheets.ShoppingLists> {
        ShoppingListsBottomSheet(
            lists = state.orderedLists,
            activeLists = state.activeLists,
            selectedListId = state.selectedListId,
            onSelect = viewModel::onListSelected,
            busy = state.busy || state.addingInline || state.savingItemIds.isNotEmpty(),
            formError = state.formError,
            onListForm = viewModel::onOpenListForm,
            onArchive = viewModel::onArchiveList,
            onDelete = viewModel::onDeleteListRequested,
            onDismiss = viewModel::onDismissError,
        )
    }
    controller.registerBottomSheet<ShoppingSheets.ShoppingListName> {
        ShoppingListNameBottomSheet(
            listName = state.listName,
            listIcon = state.listIcon,
            busy = state.busy,
            formError = state.formError,
            onCloseListForm = viewModel::onCloseListForm,
            onListName = viewModel::onListNameChanged,
            onListIcon = viewModel::onListIconChanged,
            onSaveList = viewModel::onSaveList,
            onDismiss = viewModel::onDismissError,
        )
    }
    controller.registerBottomSheet<ShoppingSheets.ShoppingFavorites> {
        ShoppingFavoritesBottomSheet(
            listName = state.selectedList?.name.orEmpty(),
            favorites = state.favorites,
            busy = state.busy || state.savingItemIds.isNotEmpty(),
            error = state.displayError,
            onReuse = viewModel::onReuseItem,
            onRemoveFavorite = viewModel::onToggleFavorite,
            onDismissError = viewModel::onDismissError,
        )
    }
    controller.registerBottomSheet<ShoppingSheets.ShoppingMove> {
        state.movingItem?.let { item ->
            ShoppingMoveBottomSheet(
                itemName = item.name,
                targets = state.moveTargets,
                busy = state.busy || item.id in state.savingItemIds,
                onMove = { listId -> viewModel.onMoveItem(item, listId) },
            )
        }
    }
    BottomSheetLayout(
        controller = controller,
        dismissEnabled = !state.busy,
        onDismissed = {
            when {
                state.showListForm -> viewModel.onCloseListForm()
                state.showLists -> viewModel.onListsVisibilityChanged(false)
                state.showFavorites -> viewModel.onFavoritesVisibilityChanged(false)
                state.movingItem != null -> viewModel.onMoveDismissed()
                else -> viewModel.onAddSheetVisibilityChanged(false)
            }
        },
        content = content,
    )
    state.deleteConfirmation?.let { confirmation ->
        DestructiveConfirmation(
            confirmation = confirmation,
            busy = state.busy,
            confirmationTag = "confirm_delete_list",
            onConfirm = viewModel::onConfirmDeleteList,
            onDismiss = viewModel::onCancelDeleteList,
        )
    }
}
