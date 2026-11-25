package com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListIconDomain
import kotlinx.coroutines.flow.StateFlow

abstract class ShoppingScreenViewModelContract : LoomViewModel() {

    abstract val state: StateFlow<ShoppingState>

    abstract fun onCompletedSelectionChanged(selecting: Boolean)

    abstract fun onCompletedItemSelected(id: String)

    abstract fun onSelectAllCompleted()

    abstract fun onDeleteCompletedRequested()

    abstract fun onDeleteCompletedCancelled()

    abstract fun onDeleteCompletedConfirmed()

    abstract fun onInlineNameChanged(value: String)

    abstract fun onAddInlineItem()

    abstract fun onRetryEntry(id: String)

    abstract fun onDiscardEntry(id: String)

    abstract fun onDeleteListRequested(list: ShoppingListDomain)

    abstract fun onCancelDeleteList()

    abstract fun onConfirmDeleteList()

    abstract fun onDismissUndo()

    abstract fun onListSelected(id: String)

    abstract fun onAddSheetVisibilityChanged(value: Boolean)

    abstract fun onStartEditing(item: ShoppingItemDomain)

    abstract fun onEditingNameChanged(value: String)

    /** Keyboard action: keeps the edit and opens a new line directly below, as in Google Keep. */
    abstract fun onEditingNext()

    /** [itemId] is the row whose field lost focus; a newer edit elsewhere is left open. */
    abstract fun onEditingFinished(itemId: String)

    /** Backspace on an empty edited line removes the item and moves to the line above. */
    abstract fun onEditingDeleteEmpty()

    /** Backspace on an empty new line closes it and returns to the line above. */
    abstract fun onDraftDeleteEmpty()

    /** Backspace in the empty add field continues editing the last item. */
    abstract fun onEntryDeleteEmpty()

    abstract fun onDraftNameChanged(value: String)

    /** Keyboard action: adds the draft line and continues with another line below it. */
    abstract fun onSubmitDraft()

    abstract fun onDraftFinished()

    abstract fun onItemNameChanged(value: String)

    abstract fun onQuantityChanged(value: String)

    abstract fun onCategoryChanged(value: String)

    abstract fun onCheckedItemsVisibilityChanged(value: Boolean)

    abstract fun onSaveItem()

    abstract fun onToggleItem(item: ShoppingItemDomain)

    abstract fun onRemoveItem(item: ShoppingItemDomain)

    abstract fun onRemoveItemCancelled()

    abstract fun onRemoveItemConfirmed()

    abstract fun onNestItem(item: ShoppingItemDomain)

    abstract fun onUnnestItem(item: ShoppingItemDomain)

    abstract fun onToggleFavorite(item: ShoppingItemDomain)

    abstract fun onMoveRequested(item: ShoppingItemDomain)

    abstract fun onMoveDismissed()

    abstract fun onMoveItem(item: ShoppingItemDomain, listId: String)

    abstract fun onUndo()

    abstract fun onListsVisibilityChanged(value: Boolean)

    abstract fun onFavoritesVisibilityChanged(value: Boolean)

    abstract fun onOpenListForm(list: ShoppingListDomain?)

    abstract fun onCloseListForm()

    abstract fun onListNameChanged(value: String)

    abstract fun onListIconChanged(icon: ShoppingListIconDomain)

    abstract fun onSaveList()

    abstract fun onArchiveList(list: ShoppingListDomain)

    abstract fun onReuseItem(item: ShoppingItemDomain)

    abstract fun onRetry()

    abstract fun onDismissError()
}
