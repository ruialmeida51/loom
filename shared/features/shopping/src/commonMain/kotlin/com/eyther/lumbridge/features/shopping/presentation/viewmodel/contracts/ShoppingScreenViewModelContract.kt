package com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.LoomViewModel
import com.eyther.lumbridge.features.shopping.domain.model.*
import kotlinx.coroutines.flow.StateFlow

data class ShoppingState(
    val snapshot: ShoppingSnapshotDomain = ShoppingSnapshotDomain(),
    val selectedListId: String = "",
    val name: String = "",
    val quantity: String = "",
    val category: String = "Produce",
    val showAdd: Boolean = false,
    val editing: Boolean = false,
    val showChecked: Boolean = false,
    val showLists: Boolean = false,
    val showListForm: Boolean = false,
    val listName: String = "",
    val busy: Boolean = false,
    val error: String? = null,
    val undoMessage: String? = null,
) {
    val selectedList
        get() = snapshot.activeLists.firstOrNull { it.id == selectedListId }

    val items
        get() = snapshot.activeItems.filter { it.listId == selectedListId }
}

abstract class ShoppingScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<ShoppingState>

    abstract fun selectList(id: String)

    abstract fun showAdd(value: Boolean)

    abstract fun edit(item: ShoppingItemDomain)

    abstract fun name(value: String)

    abstract fun quantity(value: String)

    abstract fun category(value: String)

    abstract fun showChecked(value: Boolean)

    abstract fun add()

    abstract fun toggle(item: ShoppingItemDomain)

    abstract fun remove(item: ShoppingItemDomain)

    abstract fun favorite(item: ShoppingItemDomain)

    abstract fun move(item: ShoppingItemDomain, listId: String)

    abstract fun undo()

    abstract fun showLists(value: Boolean)

    abstract fun listForm(list: ShoppingListDomain?)

    abstract fun closeListForm()

    abstract fun listName(value: String)

    abstract fun saveList()

    abstract fun archive(list: ShoppingListDomain)

    abstract fun reuse(item: ShoppingItemDomain)

    abstract fun retry()

    abstract fun dismiss()
}
