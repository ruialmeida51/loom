package com.eyther.lumbridge.app.navigation.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.eyther.lumbridge.app.navigation.model.sheet.ShoppingSheets
import com.eyther.lumbridge.core.presentation.components.bottomsheet.BottomSheetLayout
import com.eyther.lumbridge.core.presentation.components.bottomsheet.rememberBottomSheetController
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.shopping.presentation.components.bottomsheet.ShoppingItemBottomSheet
import com.eyther.lumbridge.features.shopping.presentation.components.bottomsheet.ShoppingListNameBottomSheet
import com.eyther.lumbridge.features.shopping.presentation.components.bottomsheet.ShoppingListsBottomSheet
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
            editing = state.editing,
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
            busy = state.busy,
            formError = state.formError,
            onShowLists = viewModel::onListsVisibilityChanged,
            onListForm = viewModel::onOpenListForm,
            onArchive = viewModel::onArchiveList,
            onDismiss = viewModel::onDismissError,
        )
    }
    controller.registerBottomSheet<ShoppingSheets.ShoppingListName> {
        ShoppingListNameBottomSheet(
            listName = state.listName,
            busy = state.busy,
            formError = state.formError,
            onCloseListForm = viewModel::onCloseListForm,
            onListName = viewModel::onListNameChanged,
            onSaveList = viewModel::onSaveList,
            onDismiss = viewModel::onDismissError,
        )
    }
    BottomSheetLayout(
        controller = controller,
        dismissEnabled = !state.busy,
        onDismissed = {
            when {
                state.showListForm -> viewModel.onCloseListForm()
                state.showLists -> viewModel.onListsVisibilityChanged(false)
                else -> viewModel.onAddSheetVisibilityChanged(false)
            }
        },
        content = content,
    )
}
