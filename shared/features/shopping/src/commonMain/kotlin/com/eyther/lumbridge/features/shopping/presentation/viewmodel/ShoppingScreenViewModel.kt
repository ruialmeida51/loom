package com.eyther.lumbridge.features.shopping.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.domain.usecase.AddShoppingItemsUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingItemUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingListUseCase
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.favorite_updated
import com.eyther.lumbridge.features.shopping.generated.resources.item_moved
import com.eyther.lumbridge.features.shopping.generated.resources.item_removed
import com.eyther.lumbridge.features.shopping.generated.resources.item_restored
import com.eyther.lumbridge.features.shopping.generated.resources.item_updated
import com.eyther.lumbridge.features.shopping.presentation.mapper.toShoppingTextResource
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingScreenViewModelContract
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.ShoppingState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingScreenViewModel(
    private val getShoppingStreamUseCase: GetShoppingStreamUseCase,
    private val addShoppingItemsUseCase: AddShoppingItemsUseCase,
    private val saveShoppingItemUseCase: SaveShoppingItemUseCase,
    private val saveShoppingListUseCase: SaveShoppingListUseCase,
) : ShoppingScreenViewModelContract() {

    private data class InnerInputState(
        val name: String = "",
        val quantity: String = "",
        val category: String = "Produce",
        val listName: String = "",
    )

    private data class UndoChange(
        val before: ShoppingItemDomain,
        val afterVersion: Long,
    )

    private val inputState = MutableStateFlow(InnerInputState())
    private val refreshRequests = MutableStateFlow(0)
    private var editingItem: ShoppingItemDomain? = null
    private var editingList: ShoppingListDomain? = null
    private var requestIds: List<String> = emptyList()
    private var newListId: String = newId()
    private var undoChange: UndoChange? = null

    private val mutableState = MutableStateFlow(ShoppingState())
        .observeWith(viewModelScope) { state ->
            inputState.updateState(state) { input ->
                copy(
                    name = input.name,
                    quantity = input.quantity,
                    category = input.category,
                    listName = input.listName,
                )
            }
        }
        .observeWith(viewModelScope) { state ->
            refreshRequests.flatMapLatest {
                getShoppingStreamUseCase()
            }.updateState(state) { result ->
                when (result) {
                    is AppResult.Success -> copy(
                        snapshot = result.value,
                        selectedListId = result.value.resolveActiveListId(selectedListId),
                        error = null,
                    )

                    is AppResult.Failure -> copy(error = result.error.toShoppingTextResource())
                }
            }
        }

    override val state = mutableState.asStateFlow()

    override fun onListSelected(id: String) {
        mutableState.update { oldState ->
            oldState.copy(selectedListId = id, showChecked = false)
        }
    }

    override fun onAddSheetVisibilityChanged(value: Boolean) {
        if (value) {
            editingItem = null
            requestIds = emptyList()
            inputState.value = InnerInputState()
        }

        mutableState.update { oldState ->
            oldState.copy(showAdd = value, editing = false, error = null)
        }
    }

    override fun onEditItem(item: ShoppingItemDomain) {
        editingItem = item
        inputState.value = InnerInputState(
            name = item.name,
            quantity = item.quantity,
            category = item.category,
        )

        mutableState.update { oldState ->
            oldState.copy(showAdd = true, editing = true, error = null)
        }
    }

    override fun onItemNameChanged(value: String) {
        if (value != inputState.value.name) requestIds = emptyList()

        inputState.update { oldState ->
            oldState.copy(name = value)
        }
    }

    override fun onQuantityChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(quantity = value)
        }
    }

    override fun onCategoryChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(category = value)
        }
    }

    override fun onCheckedItemsVisibilityChanged(value: Boolean) {
        mutableState.update { oldState ->
            oldState.copy(showChecked = value)
        }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null)
        }
    }

    override fun onRetry() {
        onDismissError()
        refreshRequests.update { oldVersion -> oldVersion + 1 }
    }

    override fun onSaveItem() {
        if (state.value.busy) return
        val input = inputState.value
        val before = editingItem

        if (before != null) {
            saveItem(
                item = before.copy(
                    name = input.name,
                    quantity = input.quantity,
                    category = input.category,
                ),
                message = TextResource.Resource(Res.string.item_updated),
                closeForm = true,
            )
            return
        }

        val names = input.name.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        if (requestIds.size != names.size) requestIds = names.map { newId() }
        val items = names.mapIndexed { index, name ->
            ShoppingItemDomain(
                id = requestIds[index],
                name = name,
                quantity = input.quantity,
                category = input.category,
                checked = false,
                listId = state.value.selectedListId,
            )
        }

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            val result = addShoppingItemsUseCase(items)

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> {
                        val addedIds = result.value.map { it.id }.toSet()

                        oldState.copy(
                            busy = false,
                            showAdd = false,
                            snapshot = oldState.snapshot.copy(
                                items = oldState.snapshot.items.filterNot { it.id in addedIds } + result.value,
                            ),
                        )
                    }

                    is AppResult.Failure -> oldState.copy(
                        busy = false,
                        error = result.error.toShoppingTextResource(),
                    )
                }
            }
        }
    }

    override fun onToggleItem(item: ShoppingItemDomain) {
        saveItem(
            item = item.copy(checked = !item.checked),
            message = TextResource.Resource(Res.string.item_updated),
        )
    }

    override fun onRemoveItem(item: ShoppingItemDomain) {
        saveItem(
            item = item.copy(removed = true),
            message = TextResource.Resource(Res.string.item_removed),
        )
    }

    override fun onToggleFavorite(item: ShoppingItemDomain) {
        saveItem(
            item = item.copy(favorite = !item.favorite),
            message = TextResource.Resource(Res.string.favorite_updated),
        )
    }

    override fun onMoveItem(item: ShoppingItemDomain, listId: String) {
        saveItem(
            item = item.copy(listId = listId),
            message = TextResource.Resource(Res.string.item_moved),
        )
    }

    override fun onUndo() {
        val undo = undoChange ?: return
        if (state.value.busy) return

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            val result = saveShoppingItemUseCase(undo.before.copy(version = undo.afterVersion))
            undoChange = null

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(
                        busy = false,
                        undoMessage = null,
                        snapshot = oldState.snapshot.copy(
                            items = oldState.snapshot.items.map { item ->
                                if (item.id == result.value.id) result.value else item
                            },
                        ),
                    )

                    is AppResult.Failure -> oldState.copy(
                        busy = false,
                        undoMessage = null,
                        error = result.error.toShoppingTextResource(),
                    )
                }
            }
        }
    }

    override fun onReuseItem(item: ShoppingItemDomain) {
        val existing = state.value.snapshot.findReusableItem(
            listId = state.value.selectedListId,
            name = item.name,
        )

        if (existing != null) {
            saveItem(
                item = existing.copy(checked = false, removed = false),
                message = TextResource.Resource(Res.string.item_restored),
            )
        } else {
            onAddSheetVisibilityChanged(true)
            inputState.value = InnerInputState(
                name = item.name,
                quantity = item.quantity,
                category = item.category,
            )
        }
    }

    override fun onListsVisibilityChanged(value: Boolean) {
        mutableState.update { oldState ->
            oldState.copy(showLists = value)
        }
    }

    override fun onOpenListForm(list: ShoppingListDomain?) {
        editingList = list
        newListId = list?.id ?: newId()

        inputState.update { oldState ->
            oldState.copy(listName = list?.name.orEmpty())
        }
        mutableState.update { oldState ->
            oldState.copy(showLists = false, showListForm = true, error = null)
        }
    }

    override fun onCloseListForm() {
        mutableState.update { oldState ->
            oldState.copy(showListForm = false)
        }
    }

    override fun onListNameChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(listName = value)
        }
    }

    override fun onSaveList() {
        val list = editingList ?: ShoppingListDomain(id = newListId, name = "")

        saveList(list.copy(name = inputState.value.listName))
    }

    override fun onArchiveList(list: ShoppingListDomain) {
        saveList(list.copy(archived = !list.archived))
    }

    private fun saveItem(
        item: ShoppingItemDomain,
        message: TextResource,
        closeForm: Boolean = false,
    ) {
        if (state.value.busy) return
        val before = state.value.snapshot.items.firstOrNull { it.id == item.id } ?: return

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            when (val result = saveShoppingItemUseCase(item)) {
                is AppResult.Success -> {
                    undoChange = UndoChange(before = before, afterVersion = result.value.version)

                    mutableState.update { oldState ->
                        oldState.copy(
                            busy = false,
                            showAdd = if (closeForm) false else oldState.showAdd,
                            undoMessage = message,
                            snapshot = oldState.snapshot.copy(
                                items = oldState.snapshot.items.map { existing ->
                                    if (existing.id == item.id) result.value else existing
                                },
                            ),
                        )
                    }
                }

                is AppResult.Failure -> mutableState.update { oldState ->
                    oldState.copy(
                        busy = false,
                        error = result.error.toShoppingTextResource(),
                    )
                }
            }
        }
    }

    private fun saveList(list: ShoppingListDomain) {
        if (state.value.busy) return

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            val result = saveShoppingListUseCase(list)

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> {
                        val snapshot = oldState.snapshot.copy(
                            lists = oldState.snapshot.lists.filterNot { it.id == list.id } + result.value,
                        )

                        oldState.copy(
                            busy = false,
                            showListForm = false,
                            selectedListId = snapshot.resolveActiveListId(preferredId = result.value.id),
                            snapshot = snapshot,
                        )
                    }

                    is AppResult.Failure -> oldState.copy(
                        busy = false,
                        error = result.error.toShoppingTextResource(),
                    )
                }
            }
        }
    }
}
