package com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
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
    val loadError: TextResource? = null,
    val formError: TextResource? = null,
    val undoMessage: TextResource? = null,
) {

    val displayError: TextResource? = error ?: loadError

    val activeLists: List<ShoppingListDomain> = snapshot.activeLists
    val orderedLists: List<ShoppingListDomain> = snapshot.lists.sortedBy { it.name.lowercase() }
    val selectedList: ShoppingListDomain? = activeLists.firstOrNull { it.id == selectedListId }
    val items: List<ShoppingItemDomain> = snapshot.activeItems.filter { it.listId == selectedListId }
    val remainingCount: Int = items.count { !it.checked }
    val visibleItems: List<ShoppingItemDomain> = items.filter { showChecked || !it.checked }
    val sections: Map<String, List<ShoppingItemDomain>> = visibleItems.groupBy { it.category }
    val suggestions: List<ShoppingItemDomain> = snapshot.suggestions(selectedListId)
    val moveTargets: List<ShoppingListDomain> = activeLists.filterNot { it.id == selectedListId }
}
