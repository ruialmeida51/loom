package com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingSnapshotDomain

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
    val error: TextResource? = null,
    val undoMessage: TextResource? = null,
) {

    val selectedList
        get() = snapshot.activeLists.firstOrNull { it.id == selectedListId }

    val items
        get() = snapshot.activeItems.filter { it.listId == selectedListId }
}
