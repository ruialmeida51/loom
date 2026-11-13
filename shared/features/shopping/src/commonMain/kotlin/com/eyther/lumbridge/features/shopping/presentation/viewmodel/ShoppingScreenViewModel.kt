package com.eyther.lumbridge.features.shopping.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.*
import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.shopping.domain.model.*
import com.eyther.lumbridge.features.shopping.domain.usecase.*
import com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingScreenViewModel(
    observe: ObserveShoppingUseCase,
    private val addItems: AddShoppingItemsUseCase,
    private val saveItem: SaveShoppingItemUseCase,
    private val saveShoppingList: SaveShoppingListUseCase,
) : ShoppingScreenViewModelContract() {
    private data class InnerInputState(
        val name: String = "",
        val quantity: String = "",
        val category: String = "Produce",
        val listName: String = "",
    )

    private data class UndoChange(val before: ShoppingItemDomain, val afterVersion: Long)

    private val inputState = MutableStateFlow(InnerInputState())
    private var editingItem: ShoppingItemDomain? = null
    private var editingList: ShoppingListDomain? = null
    private var requestIds = emptyList<String>()
    private var listId = newId()
    private var undoChange: UndoChange? = null
    private val refresh = MutableStateFlow(0)
    private val mutableState =
        MutableStateFlow(ShoppingState()).observeWith(viewModelScope) { state ->
            inputState.updateState(state) {
                copy(
                    name = it.name,
                    quantity = it.quantity,
                    category = it.category,
                    listName = it.listName,
                )
            }
        }
    override val state = mutableState.asStateFlow()

    init {
        mutableState.observeWith(viewModelScope) {
            refresh
                .flatMapLatest {
                    observe().catch { error ->
                        mutableState.update {
                            it.copy(
                                error =
                                    error.message
                                        ?: "shopping.message_couldn_t_load_your_shopping_lists",
                            )
                        }
                    }
                }
                .onEach { snapshot ->
                    mutableState.update { old ->
                        old.copy(
                            snapshot = snapshot,
                            selectedListId =
                                old.selectedListId.takeIf { id ->
                                    snapshot.activeLists.any { it.id == id }
                                }
                                    ?: snapshot.activeLists
                                        .firstOrNull { it.name == "Groceries" }
                                        ?.id
                                    ?: snapshot.activeLists.firstOrNull()?.id.orEmpty(),
                        )
                    }
                }
        }
    }

    override fun selectList(id: String) {
        mutableState.update { it.copy(selectedListId = id, showChecked = false) }
    }

    override fun showAdd(value: Boolean) {
        if (value) {
            editingItem = null
            requestIds = emptyList()
            inputState.value = InnerInputState()
        }
        mutableState.update { it.copy(showAdd = value, editing = false, error = null) }
    }

    override fun edit(item: ShoppingItemDomain) {
        editingItem = item
        inputState.value = InnerInputState(item.name, item.quantity, item.category)
        mutableState.update { it.copy(showAdd = true, editing = true, error = null) }
    }

    override fun name(value: String) {
        if (value != inputState.value.name) requestIds = emptyList()
        inputState.update { it.copy(name = value) }
    }

    override fun quantity(value: String) {
        inputState.update { it.copy(quantity = value) }
    }

    override fun category(value: String) {
        inputState.update { it.copy(category = value) }
    }

    override fun showChecked(value: Boolean) {
        mutableState.update { it.copy(showChecked = value) }
    }

    override fun dismiss() {
        mutableState.update { it.copy(error = null) }
    }

    override fun retry() {
        dismiss()
        refresh.update { it + 1 }
    }

    override fun add() {
        if (state.value.busy) return
        val input = inputState.value
        val before = editingItem
        if (before != null) {
            change(
                before.copy(
                    name = input.name,
                    quantity = input.quantity,
                    category = input.category,
                ),
                "shopping.message_item_updated",
                closeForm = true,
            )
            return
        }
        val names = input.name.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        if (requestIds.size != names.size) requestIds = names.map { newId() }
        val items = names.mapIndexed { index, name ->
            ShoppingItemDomain(
                requestIds[index],
                name,
                input.quantity,
                input.category,
                false,
                state.value.selectedListId,
            )
        }
        mutableState.update { it.copy(busy = true, error = null) }
        launch {
            when (val result = addItems(items)) {
                is AppResult.Success ->
                    mutableState.update { old ->
                        old.copy(
                            busy = false,
                            showAdd = false,
                            snapshot =
                                old.snapshot.copy(
                                    items =
                                        old.snapshot.items.filterNot {
                                            it.id in result.value.map { row -> row.id }
                                        } + result.value,
                                ),
                        )
                    }
                is AppResult.Failure ->
                    mutableState.update { it.copy(busy = false, error = result.message) }
            }
        }
    }

    private fun change(item: ShoppingItemDomain, message: String, closeForm: Boolean = false) {
        if (state.value.busy) return
        val before = state.value.snapshot.items.firstOrNull { it.id == item.id } ?: return
        mutableState.update { it.copy(busy = true, error = null) }
        launch {
            when (val result = saveItem(item)) {
                is AppResult.Success -> {
                    undoChange = UndoChange(before, result.value.version)
                    mutableState.update { old ->
                        old.copy(
                            busy = false,
                            showAdd = if (closeForm) false else old.showAdd,
                            undoMessage = message,
                            snapshot =
                                old.snapshot.copy(
                                    items =
                                        old.snapshot.items.map {
                                            if (it.id == item.id) result.value else it
                                        },
                                ),
                        )
                    }
                }
                is AppResult.Failure ->
                    mutableState.update { it.copy(busy = false, error = result.message) }
            }
        }
    }

    override fun toggle(item: ShoppingItemDomain) =
        change(item.copy(checked = !item.checked), "shopping.message_item_updated")

    override fun remove(item: ShoppingItemDomain) =
        change(item.copy(removed = true), "shopping.message_item_removed")

    override fun favorite(item: ShoppingItemDomain) =
        change(item.copy(favorite = !item.favorite), "shopping.message_favorite_updated")

    override fun move(item: ShoppingItemDomain, listId: String) =
        change(item.copy(listId = listId), "shopping.message_item_moved")

    override fun undo() {
        val undo = undoChange ?: return
        if (state.value.busy) return
        mutableState.update { it.copy(busy = true, error = null) }
        launch {
            when (val result = saveItem(undo.before.copy(version = undo.afterVersion))) {
                is AppResult.Success ->
                    mutableState.update { old ->
                        old.copy(
                            busy = false,
                            undoMessage = null,
                            snapshot =
                                old.snapshot.copy(
                                    items =
                                        old.snapshot.items.map {
                                            if (it.id == result.value.id) result.value else it
                                        },
                                ),
                        )
                    }
                is AppResult.Failure ->
                    mutableState.update {
                        it.copy(busy = false, undoMessage = null, error = result.message)
                    }
            }
            undoChange = null
        }
    }

    override fun reuse(item: ShoppingItemDomain) {
        val existing =
            state.value.snapshot.items.firstOrNull {
                it.listId == state.value.selectedListId && it.name.equals(item.name, true)
            }
        if (existing != null)
            change(
                existing.copy(checked = false, removed = false),
                "shopping.message_item_restored",
            )
        else {
            showAdd(true)
            inputState.value = InnerInputState(item.name, item.quantity, item.category)
        }
    }

    override fun showLists(value: Boolean) {
        mutableState.update { it.copy(showLists = value) }
    }

    override fun listForm(list: ShoppingListDomain?) {
        editingList = list
        listId = list?.id ?: newId()
        inputState.update { it.copy(listName = list?.name.orEmpty()) }
        mutableState.update { it.copy(showLists = false, showListForm = true, error = null) }
    }

    override fun closeListForm() {
        mutableState.update { it.copy(showListForm = false) }
    }

    override fun listName(value: String) {
        inputState.update { it.copy(listName = value) }
    }

    override fun saveList() =
        persistList(
            (editingList ?: ShoppingListDomain(listId, "")).copy(name = inputState.value.listName),
        )

    override fun archive(list: ShoppingListDomain) =
        persistList(list.copy(archived = !list.archived))

    private fun persistList(list: ShoppingListDomain) {
        if (state.value.busy) return
        mutableState.update { it.copy(busy = true, error = null) }
        launch {
            when (val result = saveShoppingList(list)) {
                is AppResult.Success ->
                    mutableState.update { old ->
                        val lists = old.snapshot.lists.filterNot { it.id == list.id } + result.value
                        old.copy(
                            busy = false,
                            showListForm = false,
                            selectedListId =
                                if (!result.value.archived) result.value.id
                                else lists.firstOrNull { !it.archived }?.id.orEmpty(),
                            snapshot = old.snapshot.copy(lists = lists),
                        )
                    }
                is AppResult.Failure ->
                    mutableState.update { it.copy(busy = false, error = result.message) }
            }
        }
    }
}
