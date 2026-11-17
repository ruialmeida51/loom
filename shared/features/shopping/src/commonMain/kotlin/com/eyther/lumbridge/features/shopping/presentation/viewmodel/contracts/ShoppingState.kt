package com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.model.DestructiveConfirmationUi
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingSnapshotDomain
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.list_delete
import com.eyther.lumbridge.features.shopping.generated.resources.list_delete_message
import com.eyther.lumbridge.features.shopping.generated.resources.list_delete_title
import com.eyther.lumbridge.features.shopping.generated.resources.ui_cancel
import com.eyther.lumbridge.features.shopping.presentation.model.ShoppingEntryUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList

data class ShoppingState(
    val snapshot: ShoppingSnapshotDomain = ShoppingSnapshotDomain(),
    val selectedListId: String = "",
    val name: String = "",
    val inlineName: String = "",
    val savingItemIds: PersistentSet<String> = persistentSetOf(),
    val pendingEntries: List<ShoppingEntryUi> = emptyList(),
    val entryRevision: Long = 0,
    val selectingCompleted: Boolean = false,
    val selectedCompletedIds: PersistentSet<String> = persistentSetOf(),
    val pendingCompletedDeletion: ImmutableList<ShoppingItemDomain> = persistentListOf(),
    val confirmingCompletedDeletion: Boolean = false,
    val deletingCompleted: Boolean = false,
    val successMessage: TextResource? = null,
    val deletingList: ShoppingListDomain? = null,
    val inlineError: TextResource? = null,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
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

    val deleteConfirmation: DestructiveConfirmationUi? = deletingList?.let { target ->
        DestructiveConfirmationUi(
            title = TextResource.Resource(Res.string.list_delete_title),
            message =
                TextResource.Resource(Res.string.list_delete_message, args = listOf(target.name)),
            confirmText = TextResource.Resource(Res.string.list_delete),
            cancelText = TextResource.Resource(Res.string.ui_cancel),
        )
    }
    val displayError: TextResource? = error ?: loadError

    val activeLists: ImmutableList<ShoppingListDomain> = snapshot.activeLists.toImmutableList()
    val orderedLists: ImmutableList<ShoppingListDomain> =
        snapshot.lists.sortedBy { it.name.lowercase() }.toImmutableList()
    val selectedList: ShoppingListDomain? = activeLists.firstOrNull { it.id == selectedListId }
    val items: ImmutableList<ShoppingItemDomain> =
        snapshot.activeItems.filter { it.listId == selectedListId }.toImmutableList()
    val uncheckedItems: ImmutableList<ShoppingItemDomain> =
        items.filterNot { it.checked }.toImmutableList()
    val completedItems: ImmutableList<ShoppingItemDomain> =
        items.filter { it.checked }.toImmutableList()
    val addingInline: Boolean = pendingEntries.any { it.saving }
    val visiblePendingEntries: ImmutableList<ShoppingEntryUi> =
        pendingEntries
            .filter { entry ->
                entry.item.listId == selectedListId &&
                    snapshot.items.none { it.id == entry.item.id }
            }
            .toImmutableList()
    val canAddInline: Boolean =
        inlineName.isNotBlank() && selectedList != null && !busy && !refreshing
    val selectedCompletedItems: ImmutableList<ShoppingItemDomain> =
        completedItems.filter { it.id in selectedCompletedIds }.toImmutableList()
    val allCompletedSelected: Boolean =
        completedItems.isNotEmpty() && selectedCompletedItems.size == completedItems.size

    val moveTargets: ImmutableList<ShoppingListDomain> =
        activeLists.filterNot { it.id == selectedListId }.toImmutableList()
}
