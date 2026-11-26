package com.eyther.lumbridge.features.shopping.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.model.DestructiveConfirmationUi
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListIconDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingSnapshotDomain
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.item_delete_confirm
import com.eyther.lumbridge.features.shopping.generated.resources.item_delete_message
import com.eyther.lumbridge.features.shopping.generated.resources.item_delete_title
import com.eyther.lumbridge.features.shopping.generated.resources.list_delete
import com.eyther.lumbridge.features.shopping.generated.resources.list_delete_message
import com.eyther.lumbridge.features.shopping.generated.resources.list_delete_title
import com.eyther.lumbridge.features.shopping.generated.resources.ui_cancel
import com.eyther.lumbridge.features.shopping.presentation.model.ShoppingEntryUi
import com.eyther.lumbridge.features.shopping.presentation.model.ShoppingFavoriteUi
import com.eyther.lumbridge.features.shopping.presentation.model.ShoppingRowUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentSet

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
    val deletingItem: ShoppingItemDomain? = null,
    val itemDeleteError: TextResource? = null,
    val inlineError: TextResource? = null,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val quantity: String = "",
    val category: String = "Produce",
    val showAdd: Boolean = false,
    val editingItemId: String? = null,
    val editingName: String = "",
    val draftAfterId: String? = null,
    val draftName: String = "",
    val movingItem: ShoppingItemDomain? = null,
    val showChecked: Boolean = false,
    val showLists: Boolean = false,
    val showFavorites: Boolean = false,
    val showListForm: Boolean = false,
    val listName: String = "",
    val listIcon: ShoppingListIconDomain = ShoppingListIconDomain.BAG,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val loadError: TextResource? = null,
    val formError: TextResource? = null,
    val undoMessage: TextResource? = null,
) {

    val itemDeleteConfirmation: DestructiveConfirmationUi? = deletingItem?.let { item ->
        DestructiveConfirmationUi(
            title = TextResource.Resource(Res.string.item_delete_title),
            message =
                TextResource.Resource(Res.string.item_delete_message, args = listOf(item.displayName())),
            confirmText = TextResource.Resource(Res.string.item_delete_confirm),
            cancelText = TextResource.Resource(Res.string.ui_cancel),
        )
    }
    val deletingSingleItem: Boolean = deletingItem?.id in savingItemIds

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
    val favorites: ImmutableList<ShoppingFavoriteUi> =
        snapshot
            .favorites(selectedListId)
            .map { item ->
                ShoppingFavoriteUi(
                    item = item,
                    alreadyOnList =
                        snapshot.containsUncheckedItem(listId = selectedListId, name = item.name),
                )
            }
            .toImmutableList()
    val items: ImmutableList<ShoppingItemDomain> =
        snapshot.activeItems.filter { it.listId == selectedListId }.toImmutableList()
    private val itemById = items.associateBy { it.id }

    private fun groupCompleted(item: ShoppingItemDomain): Boolean =
        itemById[item.parentId]?.checked ?: item.checked

    val uncheckedItems: ImmutableList<ShoppingItemDomain> =
        items.filterNot(::groupCompleted).toImmutableList()
    val completedItems: ImmutableList<ShoppingItemDomain> =
        items.filter(::groupCompleted).toImmutableList()
    val indentedItemIds: ImmutableSet<String> =
        items.filter { it.parentId in itemById }.map { it.id }.toPersistentSet()
    val nestingParentIds: ImmutableMap<String, String> =
        snapshot.nestingParents(selectedListId).toImmutableMap()
    val addingInline: Boolean = pendingEntries.any { it.saving }
    val visiblePendingEntries: ImmutableList<ShoppingEntryUi> =
        pendingEntries
            .filter { entry ->
                entry.item.listId == selectedListId &&
                    snapshot.items.none { it.id == entry.item.id }
            }
            .toImmutableList()
    /**
     * Unchecked rows in display order. The newest line below a row is closest to it, matching the
     * server, which always inserts directly below the anchor.
     */
    val uncheckedRows: ImmutableList<ShoppingRowUi> = buildList {
        val anchored = visiblePendingEntries.filter { it.afterId != null }.groupBy { it.afterId }
        val placed = mutableSetOf<String>()
        fun addBelow(id: String, indented: Boolean) {
            if (draftAfterId == id) add(ShoppingRowUi.Draft(indented = indented))
            anchored[id].orEmpty().asReversed().forEach { entry ->
                if (placed.add(entry.item.id)) {
                    add(ShoppingRowUi.Pending(entry = entry, indented = indented))
                    addBelow(id = entry.item.id, indented = indented)
                }
            }
        }
        uncheckedItems.forEach { item ->
            add(ShoppingRowUi.Item(item = item, indented = item.id in indentedItemIds))
            addBelow(
                id = item.id,
                indented = snapshot.placeBelow(item = item, afterId = item.id).first().parentId != null,
            )
        }
        visiblePendingEntries
            .filter { it.item.id !in placed }
            .forEach { add(ShoppingRowUi.Pending(entry = it, indented = false)) }
        if (draftAfterId != null && none { it is ShoppingRowUi.Draft })
            add(ShoppingRowUi.Draft(indented = false))
    }.toImmutableList()
    val canAddInline: Boolean =
        inlineName.isNotBlank() && selectedList != null && !busy && !refreshing
    val selectedCompletedItems: ImmutableList<ShoppingItemDomain> =
        completedItems.filter { it.id in selectedCompletedIds }.toImmutableList()
    val allCompletedSelected: Boolean =
        completedItems.isNotEmpty() && selectedCompletedItems.size == completedItems.size

    val moveTargets: ImmutableList<ShoppingListDomain> =
        activeLists.filterNot { it.id == (movingItem?.listId ?: selectedListId) }.toImmutableList()
}
